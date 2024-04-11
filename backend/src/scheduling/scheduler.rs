use crate::data_model::graph::DescreteGraph;
use anyhow::{bail, Result};
use protocol::tasks::Task;

use super::unpublished_event::UnPublishedEvent as Event;

trait SchedulerAlgorithm {
    fn schedule(&self, graph: DescreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>>;
}

struct NaiveSchedulerAlgorithm;

impl NaiveSchedulerAlgorithm {
    fn add_event(events: &mut Vec<Event>, graph: &DescreteGraph, task: &Task, timeslot: usize) {
        let Ok(scalar) = i32::try_from(timeslot) else {
            panic!("The timeslot is not convertible to i32");
        };
        events.push(Event {
            task_id: task.id,
            start_time: *graph.get_start_time() + *graph.get_time_delta() * scalar,
        });
    }

    fn adjust_graph_for_time_delta(time_delta: i64, graph_values: &[f64]) -> Option<Vec<f64>> {
        if time_delta == 0 {
            return None;
        }
        if time_delta == 1 {
            return Some(graph_values.to_owned());
        }
        let mut res = Vec::new();
        let mut i: usize = 0;
        let time_delta_usize = time_delta as usize;

        while i + time_delta_usize < graph_values.len() {
            res.push(graph_values[i..i + time_delta_usize].iter().sum());
            i += 1;
        }
        Some(res)
    }
}

impl SchedulerAlgorithm for NaiveSchedulerAlgorithm {
    fn schedule(&self, graph: DescreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>> {
        if tasks.is_empty() {
            bail!("Empty Vec for tasks provided for schedule");
        }

        let graph_values = graph.get_values();
        let mut events: Vec<Event> = Vec::new();
        let mut solutions: Vec<(i64, usize)> = Vec::new();

        'task: for task in &tasks {
            let time: i64 = task.duration.into();
            let timeslots = time / graph.get_time_delta().num_milliseconds();

            // Check for previous solution, add and continue if found
            for solution in &solutions {
                if solution.0 == timeslots {
                    Self::add_event(&mut events, &graph, task, solution.1);
                    continue 'task;
                }
            }

            // Make a new graph containing all possible time intervals to place the event
            let Some(mapped_graph) = Self::adjust_graph_for_time_delta(timeslots, graph_values)
            else {
                bail!("Invalid Time Delta");
            };

            // Mapped graph needs to contain intersection with the task's interval and self

            // Find the best interval
            let mut greatest_index = 0;
            for (i, val) in mapped_graph.iter().enumerate() {
                if mapped_graph[greatest_index] < *val {
                    greatest_index = i;
                }
            }

            Self::add_event(&mut events, &graph, task, greatest_index);
            solutions.push((timeslots, greatest_index));
        }

        Ok(events)
    }
}

#[cfg(test)]
mod scheduler_test {
    use super::{NaiveSchedulerAlgorithm, SchedulerAlgorithm};
    use crate::data_model::graph::DescreteGraph;
    use crate::scheduling::unpublished_event::UnPublishedEvent;
    use chrono::{Duration, Utc};
    use protocol::tasks::Task;
    use protocol::time::Timespan;

    #[test]
    fn test_naive_scheduler() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![Task {
            id: 1.into(),
            timespan: Timespan {
                start,
                end: start + Duration::milliseconds(20000),
            },
            duration: Duration::milliseconds(10000).into(),
            device_id: 1.into(),
        }];
        let graph = DescreteGraph::new(vec![3.0, 5.0, 4.0], Duration::milliseconds(10000), start);
        let events = scheduler.schedule(graph, tasks).unwrap();

        assert_eq!(
            events[0],
            UnPublishedEvent {
                task_id: 1.into(),
                start_time: start + Duration::milliseconds(10000),
            }
        )
    }
}
