use crate::data_model::{device::DeviceId, event::Event};
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct GetEventsResponse {
    pub events: Vec<Event>,
}

#[derive(Deserialize, Serialize)]
pub struct GetDeviceEventsRequest {
    pub device_id: DeviceId,
}
