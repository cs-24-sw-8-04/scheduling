use serde::{Deserialize, Serialize};

use crate::data_model::device::DeviceId;

#[derive(Deserialize, Serialize)]
pub struct CreateDeviceRequest {
    pub effect: f64,
}

#[derive(Deserialize, Serialize)]
pub struct DeleteDeviceRequest {
    pub id: DeviceId,
}
