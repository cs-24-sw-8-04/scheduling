use protocol::{
    tasks::TaskId,
    time::{Milliseconds, Timespan},
};

#[derive(Debug, Clone)]
pub(crate) struct TaskForScheduler {
    pub id: TaskId,
    pub timespan: Timespan,
    pub duration: Milliseconds,
    pub effect: f64,
}

impl TaskForScheduler {
    pub fn new(id: TaskId, timespan: Timespan, duration: Milliseconds, effect: f64) -> Self {
        assert!(
            chrono::Duration::from(duration) <= timespan.end - timespan.start,
            "The task duration is longer than the task timespan."
        );

        TaskForScheduler {
            id,
            timespan,
            duration,
            effect,
        }
    }
}
