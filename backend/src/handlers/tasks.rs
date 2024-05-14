use axum::{
    debug_handler,
    extract::{Query, State},
    http::StatusCode,
    Json,
};
use chrono::Utc;
use protocol::{
    devices::DeviceId,
    tasks::{CreateTaskRequest, DeleteTaskRequest, GetTasksResponse, Task, TaskId},
    time::{Milliseconds, Timespan},
};

use crate::{extractors::auth::Authentication, handlers::util::internal_error, MyState};

#[debug_handler]
pub async fn get_all_tasks(
    State(state): State<MyState>,
    Authentication(account_id): Authentication,
) -> Result<Json<GetTasksResponse>, (StatusCode, String)> {
    let current_time = Utc::now();
    let tasks = sqlx::query!(
        r#"
        SELECT Tasks.id as "id: TaskId", Tasks.timespan_start, Tasks.timespan_end, Tasks.duration as "duration: Milliseconds", Tasks.device_id as "device_id: DeviceId"
        FROM Tasks
        JOIN Devices ON Tasks.device_id == Devices.id
        WHERE Devices.account_id = ? AND (julianday(Tasks.timespan_end, 'utc') >= julianday(?, 'utc'))
        "#,
        account_id,
        current_time
    )
    .fetch_all(&state.pool)
    .await
    .map_err(internal_error)?;

    let tasks = tasks
        .iter()
        .map(|t| Task {
            id: t.id,
            timespan: Timespan::new_from_naive(t.timespan_start, t.timespan_end),
            duration: t.duration,
            device_id: t.device_id,
        })
        .collect();

    Ok(Json(GetTasksResponse { tasks }))
}

#[debug_handler]
pub async fn create_task(
    State(state): State<MyState>,
    Authentication(account_id): Authentication,
    Json(create_task_request): Json<CreateTaskRequest>,
) -> Result<Json<Task>, (StatusCode, String)> {
    let id = sqlx::query_scalar!(
        r#"
        INSERT INTO Tasks (timespan_start, timespan_end, duration, device_id)
        VALUES (?, ?, ?, (SELECT id FROM Devices WHERE account_id == ? AND id == ?))
        RETURNING id as "id: TaskId"
        "#,
        create_task_request.timespan.start,
        create_task_request.timespan.end,
        create_task_request.duration,
        account_id,
        create_task_request.device_id
    )
    .fetch_one(&state.pool)
    .await
    .map_err(internal_error)?;

    state.update_schedule().map_err(internal_error)?;

    let task = Task {
        id,
        timespan: Timespan {
            start: create_task_request.timespan.start,
            end: create_task_request.timespan.end,
        },
        duration: create_task_request.duration,
        device_id: create_task_request.device_id,
    };

    Ok(Json(task))
}

#[debug_handler]
pub async fn delete_task(
    State(state): State<MyState>,
    Authentication(account_id): Authentication,
    Query(delete_task_request): Query<DeleteTaskRequest>,
) -> Result<(), (StatusCode, String)> {
    let affected_rows = sqlx::query!(
        r#"
        DELETE FROM Tasks
        WHERE id == ? AND EXISTS (
            SELECT * 
            FROM Tasks 
            JOIN Devices ON Tasks.device_id == Devices.id 
            AND Devices.account_id == ?
        )
        RETURNING id
        "#,
        delete_task_request.id,
        account_id
    )
    .fetch_all(&state.pool)
    .await
    .map_err(internal_error)?;

    if affected_rows.len() != 1 {
        return Err((
            StatusCode::UNAUTHORIZED,
            "No associated task found".to_owned(),
        ));
    }

    state.update_schedule().map_err(internal_error)?;

    Ok(())
}
