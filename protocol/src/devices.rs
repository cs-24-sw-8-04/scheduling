use std::ops::AddAssign;

use derive_more::{Display, From, Into};
use serde::{Deserialize, Serialize};

#[derive(
    Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy, Display,
)]
#[sqlx(transparent)]
pub struct DeviceId(i64);

impl AddAssign<i64> for DeviceId {
    fn add_assign(&mut self, rhs: i64) {
        self.0 += rhs;
    }
}

#[derive(Deserialize, Serialize)]
pub struct GetDevicesResponse {
    pub devices: Vec<Device>,
}

#[derive(Deserialize, Serialize)]
pub struct CreateDeviceRequest {
    pub effect: f64,
}

#[derive(Deserialize, Serialize)]
pub struct CreateDeviceResponse {
    pub device: Device,
}

#[derive(Deserialize, Serialize)]
pub struct DeleteDeviceRequest {
    pub id: DeviceId,
}

#[derive(Deserialize, Serialize, Debug, PartialEq)]
pub struct Device {
    pub id: DeviceId,
    pub effect: f64,
}
