use chrono::{DateTime, Utc};
use sqlx::{Error, SqlitePool};

use crate::data_model::{event::Event, task::Task};

pub async fn _create_event(
    pool: &SqlitePool,
    task: Task,
    start_time: DateTime<Utc>,
) -> Result<Event, Error> {
    let event_id = sqlx::query_scalar!(
        r#"
        INSERT INTO Events (task_id, start_time) VALUES (?1, ?2)
        RETURNING id
        "#,
        task.id,
        start_time
    )
    .fetch_one(pool)
    .await?;

    Ok(Event {
        id: event_id,
        task_id: task.id,
        start_time,
    })
}
