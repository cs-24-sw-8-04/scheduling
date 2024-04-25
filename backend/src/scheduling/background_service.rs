use anyhow::Result;
use chrono::{Duration, Utc};
use protocol::{
    tasks::TaskId,
    time::{Milliseconds, Timespan},
};
use sqlx::SqlitePool;
use tokio::{select, sync::mpsc::UnboundedReceiver, time::sleep};
use tracing::{event, Level};

use crate::data_model::graph::DiscreteGraph;

use super::{scheduler::SchedulerAlgorithm, task_for_scheduler::TaskForScheduler};

pub enum BackgroundServiceMessage {
    Update,
    // Only use this in simulator mode!
    RunScheduler,
}

pub async fn background_service<F, TAlg>(
    mut receiver: UnboundedReceiver<BackgroundServiceMessage>,
    pool: SqlitePool,
    algorithm_constructor: F,
) where
    F: FnOnce() -> TAlg,
    TAlg: SchedulerAlgorithm,
{
    let mut algorithm = algorithm_constructor();
    loop {
        // Wait until we receive a message.
        let msg = receiver.recv().await;
        if msg.is_none() {
            break;
        }

        let debounce = sleep(std::time::Duration::from_secs(5 * 60));

        select! {
            _ = debounce => {
                if let Err(error) = run_algorithm(&pool, &mut algorithm).await {
                    println!("Algorithm error!: {}", error);
                }
            }
            msg = receiver.recv() => {
                if msg.is_none() {
                    break;
                }
            }
        };
    }
}

pub async fn simulator_background_service<F, TAlg>(
    mut receiver: UnboundedReceiver<BackgroundServiceMessage>,
    pool: SqlitePool,
    algorithm_constructor: F,
) where
    F: FnOnce() -> TAlg,
    TAlg: SchedulerAlgorithm,
{
    let mut algorithm = algorithm_constructor();
    loop {
        // Wait until we receive a message.
        let msg = receiver.recv().await;
        if msg.is_none() {
            break;
        }

        let msg = msg.unwrap();

        match msg {
            BackgroundServiceMessage::Update => {}
            BackgroundServiceMessage::RunScheduler => {
                if let Err(error) = run_algorithm(&pool, &mut algorithm).await {
                    println!("Algorithm error!: {}", error);
                }
            }
        }
    }
}

async fn run_algorithm(pool: &SqlitePool, algorithm: &mut impl SchedulerAlgorithm) -> Result<()> {
    // TODO: Filter out tasks based on time
    // TODO: Filter out tasks that have a started event
    let tasks = sqlx::query!(
        r#"
        SELECT Tasks.id as "id: TaskId", Tasks.timespan_start, Tasks.timespan_end, Tasks.duration as "duration: Milliseconds", Devices.effect as "effect: f64"
        FROM Tasks
        JOIN Devices ON Tasks.device_id == Devices.id
        "#)
        .fetch_all(pool)
        .await?;

    let tasks: Vec<_> = tasks
        .iter()
        .map(|t| {
            TaskForScheduler::new(
                t.id,
                Timespan::new_from_naive(t.timespan_start, t.timespan_end),
                t.duration,
                t.effect,
            )
        })
        .collect();

    event!(target: "backend", Level::INFO, "Running algorithm on {} tasks", tasks.len());

    let values = vec![
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 28.0, 200.0, 484.0, 829.0, 1186.0, 1407.0, 1475.0, 1455.0,
        1393.0, 1271.0, 1044.0, 754.0, 445.0, 154.0, 10.0, 0.0, 0.0, 0.0,
    ];

    let mut graph = DiscreteGraph::new(values, Duration::hours(1), Utc::now());

    let events = algorithm.schedule(&mut graph, tasks)?;

    // TODO: Save events (db or hashmap?)
    events.iter().for_each(|e| {
        println!("Event task: {}, start: {}", e.task_id, e.start_time);
    });

    Ok(())
}
