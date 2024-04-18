use std::collections::HashMap;

use anyhow::Result;
use criterion::{
    async_executor::FuturesExecutor, black_box, criterion_group, criterion_main, Criterion,
};
use tokio::runtime::Runtime;

use protocol::accounts::AuthToken;
use protocol::devices::Device;
use protocol::tasks::Task;
use simulator::generate_data;
use simulator::http_client::make_client;

const AMOUNT_OF_USERS: usize = 100;
const MAX_AMOUNT_OF_DEVICES_PER_USER: usize = 3;
const MAX_AMOUNT_OF_TASKS_PER_DEVICE: usize = 3;

async fn make_data() -> Result<(
    Vec<AuthToken>,
    HashMap<AuthToken, Vec<Device>>,
    HashMap<Device, Vec<Task>>,
)> {
    let client = &mut make_client();

    let auth_tokens = generate_data::generate_users(AMOUNT_OF_USERS, client).await?;
    let device_ownership =
        generate_data::generate_devices(MAX_AMOUNT_OF_DEVICES_PER_USER, client, &auth_tokens)
            .await?;
    let task_onwership =
        generate_data::generate_tasks(MAX_AMOUNT_OF_TASKS_PER_DEVICE, client, &device_ownership)
            .await?;

    Ok((auth_tokens, device_ownership, task_onwership))
}

async fn add(n: usize) -> usize {
    n + n
}

fn add_bench(c: &mut Criterion) {
    // One-time setup code goes here
    let (_auth_tokens, _device_ownership, _task_onwership) =
        match Runtime::new().unwrap().block_on(make_data()) {
            Ok((at, dos, tos)) => (at, dos, tos),
            Err(err) => panic!("ERROR: Could not create data for simulation, {}", err),
        };

    c.bench_function("my_bench", |b| {
        // Per-sample (note that a sample can be many iterations) setup goes here
        b.to_async(FuturesExecutor).iter(||
            // Measured code goes here
            add(black_box(20)));
    });
}

criterion_group!(benches, add_bench);
criterion_main!(benches);
