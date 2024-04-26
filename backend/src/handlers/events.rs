use axum::{debug_handler, extract::State, http::StatusCode, Json};
use chrono::{TimeZone, Utc};
use protocol::{
    events::{Event, EventId, GetDeviceEventRequest, GetEventResponse, GetEventsResponse},
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
pub async fn get_device_event(
    State(state): State<MyState>,
    Authentication(account_id): Authentication,
    Json(get_device_event_request): Json<GetDeviceEventRequest>,
) -> Result<Json<GetEventResponse>, (StatusCode, String)> {
    let current_time = Utc::now();
    let event = sqlx::query!(
        r#"
        SELECT Events.id as "id: EventId", Events.task_id as "task_id: TaskId", Events.start_time
        FROM Events 
        JOIN Tasks ON Events.task_id == Tasks.id 
        JOIN Devices ON Tasks.device_id == Devices.id
        WHERE Devices.account_id = ? AND Devices.id = ? AND Events.start_time >= ?
        LIMIT 1
        "#,
        account_id,
        get_device_event_request.device_id,
        current_time
    )
    .fetch_optional(&state.pool)
    .await
    .map_err(internal_error)?;

    let event = event.map(|e| Event {
        id: e.id,
        task_id: e.task_id,
        start_time: Utc.from_utc_datetime(&e.start_time),
    });

    Ok(Json(GetEventResponse { event }))
}
