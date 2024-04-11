use axum::{
    debug_handler,
    extract::{Query, State},
    http::StatusCode,
    Json,
};
use protocol::devices::{
    CreateDeviceRequest, CreateDeviceResponse, DeleteDeviceRequest, Device, DeviceId,
    GetDevicesResponse,
};
use sqlx::SqlitePool;

use crate::{extractors::auth::Authentication, handlers::util::internal_error};

#[debug_handler]
pub async fn get_all_devices(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
) -> Result<Json<GetDevicesResponse>, (StatusCode, String)> {
    let devices = sqlx::query_as!(
        Device,
        r#"
        SELECT id, effect
        FROM Devices
        WHERE account_id = ?
        "#,
        account_id
    )
    .fetch_all(&pool)
    .await
    .map_err(internal_error)?;

    Ok(Json(GetDevicesResponse { devices }))
}

#[debug_handler]
pub async fn create_device(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
    Json(create_device_request): Json<CreateDeviceRequest>,
) -> Result<Json<CreateDeviceResponse>, (StatusCode, String)> {
    let id = sqlx::query_scalar!(
        r#"
        INSERT INTO Devices (effect, account_id)
        VALUES (?, ?)
        RETURNING id as "id: DeviceId"
        "#,
        create_device_request.effect,
        account_id
    )
    .fetch_one(&pool)
    .await
    .map_err(internal_error)?;

    let device = Device {
        id,
        effect: create_device_request.effect,
    };

    Ok(Json(CreateDeviceResponse { device }))
}

#[debug_handler]
pub async fn delete_device(
    State(pool): State<SqlitePool>,
    Authentication(account_id): Authentication,
    Query(delete_device_request): Query<DeleteDeviceRequest>,
) -> Result<(), (StatusCode, String)> {
    sqlx::query!(
        r#"
        DELETE FROM Devices
        WHERE id == ? AND account_id == ?
        "#,
        delete_device_request.id,
        account_id
    )
    .execute(&pool)
    .await
    .map_err(internal_error)?;

    Ok(())
}
