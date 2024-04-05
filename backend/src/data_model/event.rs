use serde::{Deserialize, Serialize};

use super::{task::TaskId, time::DateTimeUtc};

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, Clone, Copy)]
#[sqlx(transparent)]
pub struct EventId(i64);

#[derive(Serialize, Deserialize, Debug, PartialEq, Eq)]
pub struct Event {
    pub id: EventId,
    pub task_id: TaskId,
    pub start_time: DateTimeUtc,
}
