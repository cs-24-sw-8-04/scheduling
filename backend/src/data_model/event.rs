use serde::{Deserialize, Serialize};

use super::{device::DeviceId, time::DateTimeUtc};

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq)]
#[sqlx(transparent)]
struct EventId(i64);

#[derive(Serialize, Deserialize)]
struct Event {
    id: EventId,
    device_id: DeviceId,
    version_nr: i64,
    start_time: DateTimeUtc,
}
