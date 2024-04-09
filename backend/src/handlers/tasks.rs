use axum::{
    debug_handler,
    extract::{Query, State},
    http::StatusCode,
    Json,
};
use sqlx::SqlitePool;

use crate::{
    data_model::{
        device::DeviceId,
        task::{Task, TaskId},
        time::{Milliseconds, Timespan},
    },
    extractors::auth::Authentication,
    handlers::util::internal_error,
    protocol::tasks::{CreateTaskRequest, DeleteTaskRequest},
};

#[debug_handler]
pub async fn get_tasks(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
) -> Result<Json<Vec<Task>>, (StatusCode, String)> {
    let tasks = sqlx::query!(
        r#"
        SELECT Tasks.id as "id: TaskId", Tasks.timespan_start, Tasks.timespan_end, Tasks.duration as "duration: Milliseconds", Tasks.device_id as "device_id: DeviceId"
        FROM Tasks
        JOIN Devices ON Tasks.device_id == Devices.id
        WHERE Devices.account_id = ?
        "#,
        account_id
    )
    .fetch_all(&pool)
    .await
    .map_err(internal_error)?;

    let my_tasks = tasks
        .iter()
        .map(|t| Task {
            id: t.id,
            timespan: Timespan::new_from_naive(t.timespan_start, t.timespan_end),
            duration: t.duration,
            device_id: t.device_id,
        })
        .collect();

    Ok(Json(my_tasks))
}

#[debug_handler]
pub async fn create_task(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
    Json(create_task_request): Json<CreateTaskRequest>,
) -> Result<Json<Task>, (StatusCode, String)> {
    let mut transaction = pool.begin().await.map_err(internal_error)?;

    let id = match sqlx::query_scalar!(
        r#"
        SELECT id
        FROM Devices
        WHERE account_id == ? AND id == ?
        "#,
        account_id,
        create_task_request.device_id
    )
    .fetch_optional(&mut *transaction)
    .await
    .map_err(internal_error)?
    {
        Some(_) => sqlx::query_scalar!(
            r#"
            INSERT INTO Tasks (timespan_start, timespan_end, duration, device_id)
            VALUES (?, ?, ?, ?)
            RETURNING id as "id: TaskId"
            "#,
            create_task_request.timespan.start,
            create_task_request.timespan.end,
            create_task_request.duration,
            create_task_request.device_id
        )
        .fetch_one(&mut *transaction)
        .await
        .map_err(internal_error)?,
        None => {
            return Err((
                StatusCode::UNAUTHORIZED,
                "Account does not own device".into(),
            ))
        }
    };

    transaction.commit().await.map_err(internal_error)?;

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
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
    Query(delete_task_request): Query<DeleteTaskRequest>,
) -> Result<(), (StatusCode, String)> {
    sqlx::query!(
        r#"
        DELETE FROM Tasks
        WHERE id == ? AND EXISTS (
            SELECT * 
            FROM Tasks 
            JOIN Devices ON Tasks.device_id == Devices.id 
            AND Devices.account_id == ?
        )
        "#,
        delete_task_request.id,
        account_id
    )
    .execute(&pool)
    .await
    .map_err(internal_error)?;

    Ok(())
}
