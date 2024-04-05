use serde::{Deserialize, Serialize};

use crate::data_model::{
    device::DeviceId,
    time::{Milliseconds, Timespan},
};

#[derive(Deserialize, Serialize)]
pub struct CreateTaskRequest {
    pub timespan: Timespan,
    pub duration: Milliseconds,
    pub device_id: DeviceId,
}

#[derive(Deserialize, Serialize)]
pub struct DeleteTaskRequest {
    pub id: DeviceId,
}
