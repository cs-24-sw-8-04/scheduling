use std::cmp::min;

use super::unpublished_event::UnpublishedEvent;
use crate::data_model::graph::DiscreteGraph;
use anyhow::Result;
use protocol::tasks::Task;

pub trait SchedulerAlgorithm {
    fn schedule(&self, graph: DiscreteGraph, tasks: Vec<Task>) -> Result<Vec<UnpublishedEvent>>;
}

fn make_unpublished_event(
    graph: &DiscreteGraph,
    task: &Task,
    timeslot: i32,
) -> Result<UnpublishedEvent> {
    Ok(UnpublishedEvent {
        task_id: task.id,
        start_time: graph.get_start_time() + graph.get_time_delta() * timeslot,
    })
}

fn adjust_graph_for_task_duration(timeslots: usize, graph_values: &[f64]) -> Vec<f64> {
    graph_values
        .windows(timeslots)
        .map(|window| window.iter().sum())
        .collect()
}

pub struct NaiveSchedulerAlgorithm;

impl NaiveSchedulerAlgorithm {
    pub fn new() -> NaiveSchedulerAlgorithm {
        NaiveSchedulerAlgorithm
    }

    /// Best meaning where the energy available is at max
    fn find_best_event(task: &Task, graph: &DiscreteGraph) -> Result<usize> {
        let time_delta = graph.get_time_delta().num_milliseconds() as usize;
        let duration = i64::from(task.duration) as usize;
        let timeslots = usize::div_ceil(duration, time_delta);

        // Make a new graph containing all possible time intervals to place the event
        let mapped_graph = adjust_graph_for_task_duration(timeslots, graph.get_values());

        // Defining ranges for the task's timespan
        let start_time = min(task.timespan.start, graph.get_start_time());
        let start = (task.timespan.start - start_time).num_milliseconds() as usize;
        let timeslot_start = start / time_delta;

        let end = (task.timespan.end - graph.get_start_time()).num_milliseconds() as usize;
        let timeslot_end = end / time_delta;

        assert!(
            timeslot_start <= timeslot_end,
            "Invalid timespan timeslot_start: {} timeslot_end: {} for task with id: {:?}",
            timeslot_start,
            timeslot_end,
            task
        );

        // Find the max of mapped_graph slice.
        // Slice is made form the tasks timespan
        let (greatest_index, _) = mapped_graph[timeslot_start..=timeslot_end - (timeslots - 1)]
            .iter()
            .enumerate()
            .max_by(|(_, x), (_, y)| x.total_cmp(y))
            .unwrap();

        Ok(timeslot_start + greatest_index)
    }
}

impl SchedulerAlgorithm for NaiveSchedulerAlgorithm {
    fn schedule(&self, graph: DiscreteGraph, tasks: Vec<Task>) -> Result<Vec<UnpublishedEvent>> {
        let mut events: Vec<UnpublishedEvent> = Vec::new();
        for task in &tasks {
            events.push(make_unpublished_event(
                &graph,
                task,
                NaiveSchedulerAlgorithm::find_best_event(task, &graph)?.try_into()?,
            )?);
        }

        Ok(events)
    }
}

#[cfg(test)]
mod tests {
    use super::SchedulerAlgorithm;
    use crate::data_model::graph::DiscreteGraph;
    use crate::scheduling::scheduler::NaiveSchedulerAlgorithm;
    use crate::scheduling::unpublished_event::UnpublishedEvent;
    use chrono::{DateTime, Duration, Utc};
    use protocol::devices::DeviceId;
    use protocol::tasks::{Task, TaskId};
    use protocol::time::{Milliseconds, Timespan};

    struct TaskFactory {
        task_id: TaskId,
        device_id: DeviceId,
    }
    impl TaskFactory {
        fn get_task_id(&mut self) -> TaskId {
            let res = self.task_id;
            self.task_id += 1;
            res
        }
        fn get_device_id(&mut self) -> DeviceId {
            let res = self.device_id;
            self.device_id += 1;
            res
        }
        pub fn new() -> Self {
            TaskFactory {
                task_id: 0.into(),
                device_id: 0.into(),
            }
        }
        pub fn make_tasks(
            &mut self,
            amount: usize,
            time_fixpoint: DateTime<Utc>,
            duration: Milliseconds,
            end_offset: Duration,
            start_offset: Option<Duration>,
        ) -> Vec<Task> {
            let mut res = Vec::new();
            for _ in 1..=amount {
                res.push(Task {
                    id: self.get_task_id(),
                    timespan: Timespan {
                        start: time_fixpoint + start_offset.unwrap_or_default(),
                        end: time_fixpoint + end_offset,
                    },
                    duration,
                    device_id: self.get_device_id(),
                });
            }
            res
        }
    }

    // Uses `Duration::seconds(n)` for input
    macro_rules! make_expected_unpublished_events {
        ( $start_time:expr, $($offset:expr),* ) =>  {
            {
                let mut vec = Vec::new();
                let mut __id = 0; // __ is to avoid name clash since this is code in which will be written into the file
                $(
                    vec.push(UnpublishedEvent {
                        task_id: __id.into(),
                        start_time: $start_time + Duration::seconds($offset),
                    });
                    __id += 1;
                )*
                vec
            }
        }
    }

    #[test]
    fn naive_scheduler_parabola_3elem() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(2).into(),
            Duration::seconds(2),
            None,
        );

        let graph = DiscreteGraph::new(vec![3.0, 5.0, 4.0], Duration::seconds(1), start);

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 1);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_parabola_7elem() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(6),
            None,
        );

        let graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_linear_up() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(6),
            None,
        );

        let graph = DiscreteGraph::new(
            vec![2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 4);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_linear_down() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(6),
            None,
        );

        let graph = DiscreteGraph::new(
            vec![8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 0);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_time_span() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(6),
            None,
        );

        let graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_multiple_tasks() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            3,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(5),
            None,
        );

        let graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2, 2, 2);

        assert_eq!(events, expected)
    }
}
