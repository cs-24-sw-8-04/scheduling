use anyhow::{anyhow, bail, Result};
use chrono::{DateTime, Duration, Utc};
use rand::Rng;

use crate::{
    data_factory::{delete_devices, generate_devices, generate_tasks, generate_users, BASE_URL},
    http_client::HttpClient,
};
use http::Request;
use http_body_util::BodyExt;
use protocol::{graph::DiscreteGraph, scheduling::SchedulingGlob};
use tower::{Service, ServiceExt};

pub fn make_discrete_graph_from_delta(
    time_now: DateTime<Utc>,
    delta: Duration,
    total_duration: Duration,
    min_available_effect: u64,
    max_available_effect: u64,
) -> DiscreteGraph {
    let mut rng = rand::thread_rng();
    DiscreteGraph::new(
        (0..(total_duration.num_seconds() / delta.num_seconds()))
            .map(|_| (rng.gen_range(min_available_effect..max_available_effect)) as f64)
            .collect::<Vec<f64>>(),
        delta,
        time_now,
    )
}

pub async fn compare(client: &mut HttpClient) -> Result<()> {
    let amount_of_users = 1;
    let amount_of_devices_per_user = 1;
    let min_amount_of_tasks_per_device = 8;
    let max_amount_of_tasks_per_device = 8;
    let min_effect = 10.0;
    let max_effect = 1000.0;
    let min_available_effect = 1000;
    let max_available_effect = 8100;
    let runs = 100;
    let time_now = Utc::now();
    let total_duration = Duration::hours(24);
    let mut naive_result: f64 = 0.0;
    let mut global_result: f64 = 0.0;
    let mut all_perm_result: f64 = 0.0;
    let auth_tokens = generate_users(amount_of_users, client).await?;

    for i in 0..runs {
        if i % 50 == 0 {
            println!("Round: {}", i);
        }
        let discrete_graph = make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        );

        let device_ownership = generate_devices(
            amount_of_devices_per_user,
            client,
            &auth_tokens,
            min_effect,
            max_effect,
        )
        .await?;

        let _ = generate_tasks(
            min_amount_of_tasks_per_device,
            max_amount_of_tasks_per_device,
            client,
            &device_ownership,
            total_duration,
            time_now,
        )
        .await?;

        let discrete_graph_naive =
            run_scheduling_algorithm(0, discrete_graph.clone(), client).await?;
        let discrete_graph_global =
            run_scheduling_algorithm(1, discrete_graph.clone(), client).await?;
        let discrete_graph_all_perm = run_scheduling_algorithm(2, discrete_graph, client).await?;

        let current_naive_result: f64 = discrete_graph_naive
            .get_values()
            .iter()
            .map(|&val| {
                if val < 0.0 {
                    val.powi(3).abs()
                } else {
                    val.powi(2)
                }
            })
            .sum();

        let current_global_result: f64 = discrete_graph_global
            .get_values()
            .iter()
            .map(|&val| {
                if val < 0.0 {
                    val.powi(3).abs()
                } else {
                    val.powi(2)
                }
            })
            .sum();

        let current_all_perm_result: f64 = discrete_graph_all_perm
            .get_values()
            .iter()
            .map(|&val| {
                if val < 0.0 {
                    val.powi(3).abs()
                } else {
                    val.powi(2)
                }
            })
            .sum();

        if current_naive_result < current_global_result {
            println!("Naive was better: {}", i);
        }
        naive_result += current_naive_result;
        global_result += current_global_result;
        all_perm_result += current_all_perm_result;

        delete_devices(device_ownership.clone(), client).await?;
    }

    println!("Naive result: {}", naive_result);
    println!("-------------------------------------------------------------");
    println!("Global result: {}", global_result);
    println!("-------------------------------------------------------------");
    println!("All perm result: {}", all_perm_result);

    if global_result < naive_result {
        println!(
            "Naive algorithm is {}% worse",
            ((naive_result - global_result) / global_result) * 100.0
        );
    } else {
        println!(
            "Global algorithm is {}% worse",
            ((global_result - naive_result) / naive_result) * 100.0
        );
    }

    if all_perm_result < global_result {
        println!(
            "Global algorithm is {}% worse",
            ((global_result - all_perm_result) / all_perm_result) * 100.0
        );
    } else {
        println!(
            "All perm algorithm is {}% worse",
            ((all_perm_result - global_result) / global_result) * 100.0
        );
    }

    Ok(())
}

async fn run_scheduling_algorithm(
    alg: u8,
    discrete_graph: DiscreteGraph,
    client: &mut HttpClient,
) -> Result<DiscreteGraph> {
    let body = serde_json::to_string(&SchedulingGlob {
        discrete_graph,
        alg,
    })?;

    let request = Request::builder()
        .uri(BASE_URL.to_owned() + "/scheduling/run")
        .method("GET")
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
    Ok(serde_json::from_slice(&body).unwrap())
}
