use axum::{debug_handler, extract::State, http::StatusCode, Json};
use chrono::{TimeZone, Utc};
use protocol::{
    events::{Event, EventId, GetDeviceEventsRequest, GetEventsResponse},
    tasks::TaskId,
};

use crate::{extractors::auth::Authentication, handlers::util::internal_error, MyState};

#[debug_handler]
pub async fn get_all_events(
    State(state): State<MyState>,
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
    .fetch_all(&state.pool)
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

#[debug_handler]
pub async fn get_device_events(
    State(state): State<MyState>,
    Authentication(account_id): Authentication,
    Json(get_device_event_request): Json<GetDeviceEventsRequest>,
) -> Result<Json<GetEventsResponse>, (StatusCode, String)> {
    let events = sqlx::query!(
        r#"
        SELECT Events.id as "id: EventId", Events.task_id as "task_id: TaskId", Events.start_time
        FROM Events 
        JOIN Tasks ON Events.task_id == Tasks.id 
        JOIN Devices ON Tasks.device_id == Devices.id
        WHERE Devices.account_id = ? AND Devices.id = ?
        "#,
        account_id,
        get_device_event_request.device_id
    )
    .fetch_all(&state.pool)
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
