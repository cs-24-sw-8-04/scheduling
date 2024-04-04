use crate::data_model::graph::DescreteGraph;
use protocol::{tasks::Task, events::Event};
use crate::data_model::{graph::DescreteGraph, task::Task};

use super::unpub_event::UnPublishedEvent as Event;

trait SchedulerAlgorithm {
    fn schedule(&mut self, graph: DescreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>, &str>;
}

struct NaiveSchedulerAlgorithm;

impl NaiveSchedulerAlgorithm {
    fn add_event(
        events: &mut Vec<Event>,
        graph: &DescreteGraph,
        id: i64,
        task: &Task,
        timeslot: usize,
    ) {
        let Ok(scalar) = i32::try_from(timeslot) else {
            panic!("The timeslot is not convertible to i32");
        };
        events.push(Event {
            task_id: task.id,
            start_time: *graph.get_start_time() + *graph.get_time_delta() * scalar,
        });
    }

    fn adjust_graph_for_time_delta(time_delta: i64, graph_values: &Vec<f64>) -> Option<Vec<f64>> {
        if time_delta == 0 {
            return None;
        }
        if time_delta == 1 {
            return Some(graph_values.clone());
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
    fn schedule(&mut self, graph: DescreteGraph, tasks: Vec<Task>) -> Result<Vec<Event>, &str> {
        if tasks.is_empty() {
            return Err("No tasks to schedule");
        }

        let graph_values = graph.get_values();
        let mut events: Vec<Event> = Vec::new();
        let mut solutions: Vec<(i64, usize)> = Vec::new();
        let mut i = 0;

        'task: for task in &tasks {
            let time: i64 = task.duration.into();
            let timeslots = time / graph.get_time_delta().num_milliseconds();

            // Check for previous solution, add and continue if found
            for solution in &solutions {
                if solution.0 == timeslots {
                    Self::add_event(&mut events, &graph, i, task, solution.1);
                    continue 'task;
                }
            }

            // Make a new graph containing all possible time intervals to place the event
            let Some(mapped_graph) = Self::adjust_graph_for_time_delta(timeslots, &graph_values)
            else {
                return Err("Invalid Time Delta");
            };

            // Find the best interval
            let mut greatest_index = 0;
            for (i, val) in mapped_graph.iter().enumerate() {
                if mapped_graph[greatest_index] < *val {
                    greatest_index = i;
                }
            }

            Self::add_event(&mut events, &graph, i, task, greatest_index);
            solutions.push((timeslots, greatest_index));
            i += 1;
        }

        Ok(events)
    }
}
