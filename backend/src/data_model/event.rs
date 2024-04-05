use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};

use super::{device::DeviceId, task::TaskId, time::DateTimeUtc};

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq)]
#[sqlx(transparent)]
struct EventId(i64);

#[derive(Serialize, Deserialize)]
struct Event {
    pub id: EventId,
    pub task_id: TaskId,
    pub start_time: DateTime<Utc>,
}
