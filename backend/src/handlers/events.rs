use axum::{debug_handler, extract::State, http::StatusCode, Json};
use chrono::{TimeZone, Utc};
use sqlx::SqlitePool;

use crate::{
    data_model::{
        event::{Event, EventId},
        task::TaskId,
    },
    extractors::auth::Authentication,
    handlers::util::internal_error,
    protocol::events::GetEventsResponse,
};

#[debug_handler]
pub async fn get_events(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
) -> Result<Json<GetEventsResponse>, (StatusCode, String)> {
    let events = sqlx::query!(
        r#"
        SELECT Events.id as "id: EventId", Events.task_id as "task_id: TaskId", Events.start_time
        FROM Events 
        JOIN Tasks ON Events.task_id == Tasks.id 
        JOIN Devices ON Tasks.device_id == Devices.id
        WHERE Devices.account_id = ?
        "#,
        account_id
    )
    .fetch_all(&pool)
    .await
    .map_err(internal_error)?;

    let events = events
        .iter()
        .map(|e| Event {
            id: e.id,
            task_id: e.task_id,
            start_time: Utc.from_utc_datetime(&e.start_time),
        })
        .collect();

    Ok(Json(GetEventsResponse { events }))
}
