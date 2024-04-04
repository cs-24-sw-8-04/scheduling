use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct CreateDeviceRequest {
    pub effect: f64,
}

#[derive(Deserialize, Serialize)]
pub struct DeleteDeviceRequest {
    pub id: i64,
}
