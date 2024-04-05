use super::scheduler::Scheduler;

pub struct TestScheduler;

impl Scheduler for TestScheduler {
    fn schedule(
        self: &mut Self,
        tasks: Vec<crate::data_model::task::Task>,
    ) -> Vec<crate::data_model::event::Event> {
        vec![]
    }
}
