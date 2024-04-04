use protocol::{tasks::TaskId, time::DateTimeUtc};

pub struct UnPublishedEvent {
    pub task_id: TaskId,
    pub start_time: DateTimeUtc,
}
