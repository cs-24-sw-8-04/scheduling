use serde::{Deserialize, Serialize};

use super::{
    device::DeviceId,
    time::{Milliseconds, Timespan},
};

use derive_more::{Display, From, Into};

#[derive(
    Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy, Display,
)]
#[sqlx(transparent)]
pub struct TaskId(i64);

#[derive(Serialize, Deserialize, Debug, PartialEq)]
pub struct Task {
    pub id: TaskId,
    pub timespan: Timespan,
    pub duration: Milliseconds,
    pub device_id: DeviceId,
}
