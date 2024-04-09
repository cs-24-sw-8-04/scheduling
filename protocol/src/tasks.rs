use derive_more::{Display, From, Into};
use serde::{Deserialize, Serialize};

use crate::{
    devices::DeviceId,
    time::{Milliseconds, Timespan},
};

#[derive(
    Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy, Display,
)]
#[sqlx(transparent)]
pub struct TaskId(i64);

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
