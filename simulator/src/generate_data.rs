use futures::stream::FuturesUnordered;
use futures::StreamExt;
use std::collections::HashMap;
use tracing::{event, Level};
use uuid::Uuid;

use anyhow::{anyhow, bail, Result};

use crate::http_client::HttpClient;
use chrono::{Duration, Utc};
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
const MIN_EFFECT: f64 = 10.0;
const MAX_EFFECT: f64 = 5000.0;

pub async fn generate_users(amount: usize, client: &mut HttpClient) -> Result<Vec<AuthToken>> {
    (0..amount)
        .map(|_| generate_user(client.clone()))
        .collect::<FuturesUnordered<_>>()
        .collect::<Vec<_>>()
        .await
        .into_iter()
        .collect::<Result<Vec<AuthToken>>>()
}

pub async fn generate_devices(
    max_amount: usize,
    client: &mut HttpClient,
    auth_tokens: &Vec<AuthToken>,
) -> Result<HashMap<AuthToken, Vec<Device>>> {
    let mut device_onwership: HashMap<AuthToken, Vec<Device>> = HashMap::new();
    let mut rng = rand::thread_rng();

    for auth_token in auth_tokens {
        let amount = rng.gen_range(0..=max_amount);

        let devices = (0..amount)
            .map(|_| generate_device(client.clone(), auth_token.clone()))
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
    max_amount: usize,
    client: &mut HttpClient,
    device_ownership: &HashMap<AuthToken, Vec<Device>>,
) -> Result<HashMap<Device, Vec<Task>>> {
    let mut task_ownership: HashMap<Device, Vec<Task>> = HashMap::new();
    let mut rng = rand::thread_rng();

    for auth_token in device_ownership.keys() {
        let devices = &device_ownership[auth_token];

        for device in devices {
            let amount = rng.gen_range(0..=max_amount);
            let mut tasks: Vec<Task> = vec![];

            for _ in 0..amount {
                tasks.push(generate_task(client, auth_token.clone(), device.clone()).await?);
            }
            task_ownership.insert(device.clone(), tasks);
        }
    }

    Ok(task_ownership)
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

async fn generate_device(mut client: HttpClient, auth_token: AuthToken) -> Result<Device> {
    let mut rng = rand::thread_rng();

    let body = serde_json::to_string(&CreateDeviceRequest {
        name: "test".into(),
        effect: rng.gen_range(MIN_EFFECT..=MAX_EFFECT),
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
    client: &mut HttpClient,
    auth_token: AuthToken,
    device: Device,
) -> Result<Task> {
    let mut rng = rand::thread_rng();
    let start = Utc::now() + Duration::hours(rng.gen_range(0..10));
    let end = start + Duration::hours(rng.gen_range(1..10));

    let min_dur: i64 = Duration::minutes(10).num_milliseconds();
    let max_dur: i64 = (end - start).num_milliseconds();
    event!(target: "simulator", Level::TRACE, "Min duration: {}, Max duration: {}", min_dur, max_dur);
    let duration = rng.gen_range(min_dur..=max_dur);

    let body = serde_json::to_string(&CreateTaskRequest {
        timespan: Timespan::new(start, end),
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
