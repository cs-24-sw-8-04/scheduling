use protocol::{tasks::TaskId, time::DateTimeUtc};

#[derive(PartialEq, Debug)]
pub struct UnPublishedEvent {
    pub task_id: TaskId,
    pub start_time: DateTimeUtc,
}
