use futures::stream::FuturesUnordered;
use futures::StreamExt;
use protocol::devices::DeviceId;
use std::collections::HashMap;
use tracing::{event, Level};
use uuid::Uuid;

use anyhow::{anyhow, bail, Result};

use crate::http_client::HttpClient;
use chrono::{DateTime, Duration, Utc};
use http::Request;
use http_body_util::BodyExt;
use protocol::time::Timespan;
use protocol::{
    accounts::{AuthToken, RegisterOrLoginRequest, RegisterOrLoginResponse},
    devices::{CreateDeviceRequest, CreateDeviceResponse, Device},
    tasks::{CreateTaskRequest, Task},
};
use rand::Rng;
use tower::{Service, ServiceExt};

pub const BASE_URL: &str = "http://localhost:3000";

pub async fn generate_users(amount: usize, client: &mut HttpClient) -> Result<Vec<AuthToken>> {
    let futures = (0..amount).map(|_| generate_user(client.clone()));

    let stream = futures::stream::iter(futures).buffer_unordered(16);

    stream
        .collect::<Vec<_>>()
        .await
        .into_iter()
        .collect::<Result<Vec<AuthToken>>>()
}

pub async fn generate_devices(
    amount: usize,
    client: &mut HttpClient,
    auth_tokens: &Vec<AuthToken>,
    min_effect: f64,
    max_effect: f64,
) -> Result<HashMap<AuthToken, Vec<Device>>> {
    let mut device_onwership: HashMap<AuthToken, Vec<Device>> = HashMap::new();
    let mut rng = rand::thread_rng();

    for auth_token in auth_tokens {
        let devices = (0..amount)
            .map(|_| {
                generate_device(
                    client.clone(),
                    auth_token.clone(),
                    rng.gen_range(min_effect..=max_effect),
                )
            })
            .collect::<FuturesUnordered<_>>()
            .collect::<Vec<_>>()
            .await
            .into_iter()
            .collect::<Result<Vec<Device>>>()?;

        device_onwership.insert(auth_token.clone(), devices);
    }

    Ok(device_onwership)
}

pub async fn generate_tasks(
    min_amount: usize,
    max_amount: usize,
    client: &mut HttpClient,
    device_ownership: &HashMap<AuthToken, Vec<Device>>,
    max_duration: Duration,
    start: DateTime<Utc>,
) -> Result<HashMap<Device, Vec<Task>>> {
    let mut task_ownership: HashMap<Device, Vec<Task>> = HashMap::new();
    let mut rng = rand::thread_rng();

    for auth_token in device_ownership.keys() {
        let devices = &device_ownership[auth_token];

        for device in devices {
            let amount = rng.gen_range(min_amount..=max_amount);

            let tasks = (0..amount)
                .map(|_| {
                    generate_task(
                        client.clone(),
                        auth_token.clone(),
                        device.clone(),
                        max_duration,
                        start,
                    )
                })
                .collect::<FuturesUnordered<_>>()
                .collect::<Vec<_>>()
                .await
                .into_iter()
                .collect::<Result<Vec<Task>>>()?;

            task_ownership.insert(device.clone(), tasks);
        }
    }

    Ok(task_ownership)
}

pub async fn delete_devices(
    device_ownership: HashMap<AuthToken, Vec<Device>>,
    client: &mut HttpClient,
) -> Result<()> {
    for (auth_token, devices) in device_ownership.clone() {
        devices
            .iter()
            .map(|device| delete_device(auth_token.clone(), device.id, client.clone()))
            .collect::<FuturesUnordered<_>>()
            .collect::<Vec<_>>()
            .await;
    }

    Ok(())
}

async fn generate_user(mut client: HttpClient) -> Result<AuthToken> {
    let body = serde_json::to_string(&RegisterOrLoginRequest {
        username: format!("test_user{}", Uuid::new_v4()).to_string(),
        password: "test_password".to_string(),
    })?;

    let request = Request::builder()
        .uri(BASE_URL.to_owned() + "/accounts/register")
        .method("POST")
        .header("Content-Type", "application/json")
        .body(body)?;

    let response = client.ready().await?.call(request).await?;

    if !response.status().is_success() {
        let status = response.status();
        let body = response
            .into_body()
            .collect()
            .await
            .map_err(|e| anyhow!(e))?;
        bail!(
            "status message: {} message: {:?}",
            status,
            String::from_utf8(body.to_bytes().to_vec())
        );
    }

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let response: RegisterOrLoginResponse = serde_json::from_slice(&body).unwrap();

    Ok(response.auth_token)
}

async fn generate_device(
    mut client: HttpClient,
    auth_token: AuthToken,
    effect: f64,
) -> Result<Device> {
    let body = serde_json::to_string(&CreateDeviceRequest {
        name: "test".into(),
        effect,
    })?;

    let request = Request::builder()
        .uri(BASE_URL.to_owned() + "/devices/create")
        .method("POST")
        .header("Content-Type", "application/json")
        .header("X-Auth-Token", auth_token.to_string())
        .body(body)?;

    let response = client.ready().await?.call(request).await?;

    if !response.status().is_success() {
        let status = response.status();
        let body = response
            .into_body()
            .collect()
            .await
            .map_err(|e| anyhow!(e))?;
        bail!(
            "status message: {} message: {:?}",
            status,
            String::from_utf8(body.to_bytes().to_vec())
        );
    }

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let response: CreateDeviceResponse = serde_json::from_slice(&body).unwrap();

    Ok(response.device)
}

async fn generate_task(
    mut client: HttpClient,
    auth_token: AuthToken,
    device: Device,
    max_duration: Duration,
    start: DateTime<Utc>,
) -> Result<Task> {
    let mut rng = rand::thread_rng();

    event!(target: "simulator", Level::TRACE, "Max duration: {}", max_duration);

    let timespan_start = Duration::minutes(0).num_minutes();
    let timespan_end = max_duration.num_minutes();

    let start_offset = Duration::minutes(rng.gen_range(timespan_start..(timespan_end - 3))); // 0=..=86396
    let end_offset = Duration::minutes(rng.gen_range(start_offset.num_minutes()..(timespan_end - 2))) // 86396=..=86397
        + Duration::minutes(2);

    let start_time = start + start_offset;
    let end_time = start + end_offset;

    let total_duration = (end_time - start_time).num_minutes();
    let duration = Duration::minutes(rng.gen_range(1..total_duration));

    let body = serde_json::to_string(&CreateTaskRequest {
        timespan: Timespan::new(start_time, end_time),
        duration: duration.into(),
        device_id: device.id,
    })?;

    let request = Request::builder()
        .uri(BASE_URL.to_owned() + "/tasks/create")
        .method("POST")
        .header("Content-Type", "application/json")
        .header("X-Auth-Token", auth_token.to_string())
        .body(body)?;

    let response = client.ready().await?.call(request).await?;

    if !response.status().is_success() {
        let status = response.status();
        let body = response
            .into_body()
            .collect()
            .await
            .map_err(|e| anyhow!(e))?;
        bail!(
            "status message: {} message: {:?}",
            status,
            String::from_utf8(body.to_bytes().to_vec())
        );
    }

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let response: Task = serde_json::from_slice(&body).unwrap();

    Ok(response)
}

async fn delete_device(auth_token: AuthToken, id: DeviceId, mut client: HttpClient) -> Result<()> {
    let request = Request::builder()
        .uri(BASE_URL.to_owned() + &format!("/devices/delete?id={}", id))
        .method("DELETE")
        .header("Content-Type", "application/json")
        .header("X-Auth-Token", auth_token.to_string())
        .body(serde_json::to_string("")?)?;

    let response = client.ready().await?.call(request).await?;

    if !response.status().is_success() {
        let status = response.status();
        let body = response
            .into_body()
            .collect()
            .await
            .map_err(|e| anyhow!(e))?;
        bail!(
            "status message: {} message: {:?}",
            status,
            String::from_utf8(body.to_bytes().to_vec())
        );
    }

    Ok(())
}
