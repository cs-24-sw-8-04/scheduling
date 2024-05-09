use std::ops::AddAssign;

use derive_more::{Display, From, Into};
use serde::{Deserialize, Serialize};

use crate::{
    devices::DeviceId,
    time::{Milliseconds, Timespan},
};

#[derive(
    Deserialize,
    Serialize,
    Debug,
    sqlx::Type,
    PartialEq,
    Eq,
    From,
    Into,
    Clone,
    Copy,
    Display,
    PartialOrd,
    Ord,
)]
#[sqlx(transparent)]
pub struct TaskId(i64);

impl AddAssign<i64> for TaskId {
    fn add_assign(&mut self, rhs: i64) {
        self.0 += rhs;
    }
}

#[derive(Deserialize, Serialize)]
pub struct GetTasksResponse {
    pub tasks: Vec<Task>,
}

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

#[derive(Serialize, Deserialize, Debug, PartialEq)]
pub struct Task {
    pub id: TaskId,
    pub timespan: Timespan,
    pub duration: Milliseconds,
    pub device_id: DeviceId,
}
