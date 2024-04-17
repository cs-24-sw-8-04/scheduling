use protocol::{tasks::TaskId, time::DateTimeUtc};

#[derive(PartialEq, Debug)]
pub struct UnpublishedEvent {
    pub task_id: TaskId,
    pub start_time: DateTimeUtc,
}
