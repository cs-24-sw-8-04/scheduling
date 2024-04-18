use std::ops::AddAssign;

use derive_more::{Display, From, Into};
use serde::{Deserialize, Serialize};
use std::hash::{Hash, Hasher};

#[derive(
    Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy, Display, Hash,
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
    pub name: String,
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

#[derive(Deserialize, Serialize, Debug, Clone)]
pub struct Device {
    pub id: DeviceId,
    pub name: String,
    pub effect: f64,
}

impl Hash for Device {
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.id.hash(state);
    }
}

impl PartialEq for Device {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id
    }
}

impl Eq for Device {}
