use chrono::{DateTime, Utc};
use protocol::{
    events::{Event, EventId},
    tasks::Task,
};
use sqlx::{Error, SqlitePool};

pub async fn _create_event(
    pool: &SqlitePool,
    task: &Task,
    start_time: DateTime<Utc>,
) -> Result<Event, Error> {
    let event_id = sqlx::query_scalar!(
        r#"
        INSERT INTO Events (task_id, start_time) VALUES (?, ?)
        RETURNING id as "id: EventId"
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
