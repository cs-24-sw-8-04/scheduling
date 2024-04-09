use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use crate::{devices::DeviceId, tasks::TaskId};

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, Clone, Copy)]
#[sqlx(transparent)]
pub struct EventId(i64);

#[derive(Deserialize, Serialize)]
pub struct GetEventsResponse {
    pub events: Vec<Event>,
}

#[derive(Deserialize, Serialize, Debug, PartialEq, Eq)]
pub struct Event {
    pub id: EventId,
    pub task_id: TaskId,
    pub start_time: DateTime<Utc>,
}

#[derive(Deserialize, Serialize)]
pub struct GetDeviceEventsRequest {
    pub device_id: DeviceId,
}
