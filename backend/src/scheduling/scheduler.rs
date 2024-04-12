use super::unpublished_event::UnPublishedEvent as Event;
use crate::data_model::graph::DiscreteGraph;
use anyhow::Result;
use chrono::Duration;
use protocol::{tasks::Task, time::Milliseconds};

trait SchedulerAlgorithm {
    fn schedule(&self, graph: DiscreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>>;
}

fn add_event(
    events: &mut Vec<Event>,
    graph: &DiscreteGraph,
    task: &Task,
    timeslot: usize,
) -> Result<()> {
    events.push(Event {
        task_id: task.id,
        start_time: graph.get_start_time() + graph.get_time_delta() * i32::try_from(timeslot)?,
    });
    Ok(())
}

fn adjust_graph_for_time_delta(timeslots: usize, graph_values: &[f64]) -> Vec<f64> {
    graph_values
        .windows(timeslots)
        .map(|window| window.iter().sum())
        .collect()
}

fn checked_duration_to_i64(duration: Duration) -> i64 {
    i64::from(Milliseconds::from(duration))
}

struct NaiveSchedulerAlgorithm;

impl NaiveSchedulerAlgorithm {
    fn find_best_event(task: &Task, graph: &DiscreteGraph) -> Result<usize> {
        let duration: i64 = task.duration.into();
        let time_delta = graph.get_time_delta().num_milliseconds();
        let timeslots = usize::try_from(duration / time_delta)?;

        // Make a new graph containing all possible time intervals to place the event
        let mapped_graph = adjust_graph_for_time_delta(timeslots, graph.get_values());

        // Defining ranges for the task's timespan
        let timeslot_start: usize =
            (checked_duration_to_i64(task.timespan.start - graph.get_start_time()) / time_delta)
                .try_into()?;
        let timeslot_end: usize =
            (checked_duration_to_i64(task.timespan.end - graph.get_start_time()) / time_delta)
                .try_into()?;

        // Find the best interval
        let (greatest_index, _) = mapped_graph[timeslot_start..=timeslot_end - (timeslots - 1)]
            .iter()
            .enumerate()
            .max_by(|(_, x), (_, y)| x.total_cmp(y))
            .expect("Task timespan is invalid");

        Ok(greatest_index + timeslot_start)
    }
}

impl SchedulerAlgorithm for NaiveSchedulerAlgorithm {
    fn schedule(&self, graph: DiscreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>> {
        let mut events: Vec<Event> = Vec::new();
        for task in &tasks {
            add_event(
                &mut events,
                &graph,
                task,
                NaiveSchedulerAlgorithm::find_best_event(task, &graph)?,
            )?;
        }

        Ok(events)
    }
}

#[cfg(test)]
mod scheduler_test {
    use super::SchedulerAlgorithm;
    use crate::data_model::graph::DiscreteGraph;
    use crate::scheduling::scheduler::NaiveSchedulerAlgorithm;
    use crate::scheduling::unpublished_event::UnPublishedEvent;
    use chrono::{Duration, Utc};
    use protocol::tasks::Task;
    use protocol::time::Timespan;

    #[test]
    fn naive_scheduler_parabola_3elem() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start,
                end: start + Duration::seconds(2),
            },
            duration: Duration::seconds(2).into(),
            device_id: 1.into(),
        }];
        let graph = DiscreteGraph::new(vec![3.0, 5.0, 4.0], Duration::seconds(1), start);
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start + Duration::seconds(1),
            }
        )
    }
    #[test]
    fn naive_scheduler_parabola_7elem() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start,
                end: start + Duration::seconds(6),
            },
            duration: Duration::seconds(3).into(),
            device_id: 1.into(),
        }];
        let graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start + Duration::seconds(2),
            }
        )
    }
    #[test]
    fn naive_scheduler_linear_up() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start,
                end: start + Duration::seconds(6),
            },
            duration: Duration::seconds(3).into(),
            device_id: 1.into(),
        }];
        let graph = DiscreteGraph::new(
            vec![2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0],
            Duration::seconds(1),
            start,
        );
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start + Duration::seconds(4),
            }
        )
    }
    #[test]
    fn naive_scheduler_linear_down() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start,
                end: start + Duration::seconds(6),
            },
            duration: Duration::seconds(3).into(),
            device_id: 1.into(),
        }];
        let graph = DiscreteGraph::new(
            vec![8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0],
            Duration::seconds(1),
            start,
        );
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start,
            }
        )
    }
    #[test]
    fn naive_scheduler_time_span() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start: start + Duration::seconds(2),
                end: start + Duration::seconds(6),
            },
            duration: Duration::seconds(3).into(),
            device_id: 1.into(),
        }];
        let graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start + Duration::seconds(2),
            }
        )
    }
}
