use protocol::{
    tasks::TaskId,
    time::{Milliseconds, Timespan},
};

#[derive(Debug)]
pub(crate) struct TaskForScheduler {
    pub id: TaskId,
    pub timespan: Timespan,
    pub duration: Milliseconds,
    pub effect: f64,
}
