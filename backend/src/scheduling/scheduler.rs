use std::cmp::min;

use super::task_for_scheduler::TaskForScheduler;
use super::unpublished_event::UnpublishedEvent;
use anyhow::{bail, Result};
use itertools::Itertools;
use protocol::graph::DiscreteGraph;
use rayon::prelude::*;

pub trait SchedulerAlgorithm {
    fn schedule(
        &self,
        graph: &mut DiscreteGraph,
        tasks: Vec<TaskForScheduler>,
    ) -> Result<Vec<UnpublishedEvent>>;
}
pub struct AllPermutationsAlgorithm;
pub struct GlobalSchedulerAlgorithm;
pub struct NaiveSchedulerAlgorithm;

#[allow(clippy::new_without_default)]
impl AllPermutationsAlgorithm {
    pub fn new() -> Self {
        AllPermutationsAlgorithm
    }
    fn weight(val: &f64) -> f64 {
        if *val < 0.0 {
            val.powi(3).abs()
        } else {
            val.powi(2)
        }
    }
}

#[allow(clippy::new_without_default)]
impl GlobalSchedulerAlgorithm {
    pub fn new() -> Self {
        GlobalSchedulerAlgorithm
    }
}
#[allow(clippy::new_without_default)]
impl NaiveSchedulerAlgorithm {
    pub fn new() -> Self {
        NaiveSchedulerAlgorithm
    }
}

// An algorithm creating all permutations of the global scheduling algorithm by changing the task order
impl SchedulerAlgorithm for AllPermutationsAlgorithm {
    fn schedule(
        &self,
        graph: &mut DiscreteGraph,
        tasks: Vec<TaskForScheduler>,
    ) -> Result<Vec<UnpublishedEvent>> {
        let scheudler = GlobalSchedulerAlgorithm::new();

        let len = tasks.len();
        let permutaions = tasks.into_iter().permutations(len);

        let (best_graph, _, best_schedule) = permutaions
            .par_bridge()
            .map(|permutation| {
                let mut temp_graph = graph.clone();
                let res = scheudler.schedule(&mut temp_graph, permutation);
                (temp_graph, res)
            })
            .map(|(graph, schedule)| {
                let graph_sum = graph
                    .get_values()
                    .iter()
                    .map(|val: &f64| Self::weight(val))
                    .sum::<f64>();
                (graph, graph_sum, schedule)
            })
            .min_by(|(_, graph1_sum, _), (_, graph2_sum, _)| {
                graph1_sum.partial_cmp(graph2_sum).unwrap()
            })
            .unwrap();

        *graph = best_graph;
        best_schedule
    }
}

impl SchedulerAlgorithm for GlobalSchedulerAlgorithm {
    fn schedule(
        &self,
        graph: &mut DiscreteGraph,
        tasks: Vec<TaskForScheduler>,
    ) -> Result<Vec<UnpublishedEvent>> {
        let mut scheduled_events: Vec<UnpublishedEvent> = Vec::new();
        for task in &tasks {
            let temp_graph = graph.clone();
            let best_event = make_unpublished_event_and_remove_from_graph(
                graph,
                task,
                find_best_event(task, &temp_graph)?,
                duration_as_timeslots(task, &temp_graph)?,
            )?;
            scheduled_events.push(best_event);
        }

        Ok(scheduled_events)
    }
}
impl SchedulerAlgorithm for NaiveSchedulerAlgorithm {
    fn schedule(
        &self,
        graph: &mut DiscreteGraph,
        tasks: Vec<TaskForScheduler>,
    ) -> Result<Vec<UnpublishedEvent>> {
        let mut scheduled_events: Vec<UnpublishedEvent> = Vec::new();
        let initial_graph = graph.clone();
        for task in &tasks {
            scheduled_events.push(make_unpublished_event_and_remove_from_graph(
                graph,
                task,
                find_best_event(task, &initial_graph)?,
                duration_as_timeslots(task, &initial_graph)?,
            )?);
        }

        Ok(scheduled_events)
    }
}

fn find_best_event(task: &TaskForScheduler, graph: &DiscreteGraph) -> Result<usize> {
    let (timeslot_start, timeslot_end, timeslot_duration) = get_task_as_timeslots(task, graph)?;

    // The set of values I for task T
    let task_interval = &graph.get_values()[timeslot_start..=timeslot_end];

    // The set P(d') created using I
    let mapped_graph = make_p_from_duration_in_timeslots(timeslot_duration, task_interval);

    // Getting the max value for P(d'),
    // then finding the timeslot in which the event should begin
    let greatest_index = mapped_graph
        .iter()
        .position_max_by(|x, y| x.total_cmp(y))
        .unwrap();

    Ok(timeslot_start + greatest_index)
}

/// # Example
/// ```ignore
/// let timeslots = 2;
/// let graph_values = [1.0..=5.0];
/// let res = make_p_from_duration_in_timeslots(timeslots, graph_values);
///
/// assert_eq!(res, [3.0, 5.0, 7.0, 9.0]);
/// ```
fn make_p_from_duration_in_timeslots(timeslots: usize, graph_values: &[f64]) -> Vec<f64> {
    assert_ne!(timeslots, 0, "Check that your duration is non-zero");
    graph_values
        .windows(timeslots)
        .map(|window| window.iter().sum())
        .collect()
}

/// Same as add_event, but removes the energy used by the event from the [DiscreteGraph].values
fn make_unpublished_event_and_remove_from_graph(
    graph: &mut DiscreteGraph,
    task: &TaskForScheduler,
    timeslot: usize,
    duration_in_timeslots: usize,
) -> Result<UnpublishedEvent> {
    graph.sub_values(timeslot, task.effect, duration_in_timeslots);

    Ok(UnpublishedEvent {
        task_id: task.id,
        start_time: graph.get_start_time() + graph.get_time_delta() * timeslot.try_into()?,
    })
}

fn get_task_as_timeslots(
    task: &TaskForScheduler,
    graph: &DiscreteGraph,
) -> Result<(usize, usize, usize)> {
    // time_delta represents delta t
    let time_delta: usize = graph.get_time_delta().num_milliseconds().try_into()?;
    // Defining ranges for the task's timespan
    let start_time = min(task.timespan.start, graph.get_start_time());
    let start_offset: usize = (task.timespan.start - start_time)
        .num_milliseconds()
        .try_into()?;

    let end_time = min(task.timespan.end, graph.get_end_time());
    let end_offset: usize = (end_time - graph.get_start_time())
        .num_milliseconds()
        .try_into()?;

    // The timeslots of when the task can start and end
    let timeslot_start: usize = start_offset.div_ceil(time_delta);
    let timeslot_end: usize = end_offset / time_delta;

    if timeslot_start >= timeslot_end {
        bail!(
            "Invalid timespan timeslot_start: {} timeslot_end: {} for task with id: {:?}",
            timeslot_start,
            timeslot_end,
            task
        );
    }

    // timeslots represent d'
    let timeslots = duration_as_timeslots(task, graph)?;
    if timeslot_end - timeslot_start < timeslots {
        bail!(
            "Unschedulable task provided, because the duration is larger than the task after truncating, task: {:?} timespan start in timeslots: {}, timespan end in timeslots: {}, duration in timeslots: {}",
            task,
            timeslot_start,
            timeslot_end,
            timeslots
        );
    }

    Ok((timeslot_start, timeslot_end, timeslots))
}

fn duration_as_timeslots(task: &TaskForScheduler, graph: &DiscreteGraph) -> Result<usize> {
    let time_delta = graph.get_time_delta().num_milliseconds() as usize;
    let duration = (i64::from(task.duration)) as usize;
    Ok(duration.div_ceil(time_delta))
}

#[cfg(test)]
mod tests {
    use super::SchedulerAlgorithm;
    use crate::scheduling::scheduler::{
        AllPermutationsAlgorithm, GlobalSchedulerAlgorithm, NaiveSchedulerAlgorithm,
    };
    use crate::scheduling::task_for_scheduler::TaskForScheduler as Task;
    use crate::scheduling::unpublished_event::UnpublishedEvent;
    use chrono::{DateTime, Duration, Utc};
    use protocol::graph::DiscreteGraph;
    use protocol::tasks::TaskId;
    use protocol::time::{Milliseconds, Timespan};

    struct TaskFactory {
        task_id: TaskId,
    }
    impl TaskFactory {
        fn get_task_id(&mut self) -> TaskId {
            let res = self.task_id;
            self.task_id += 1;
            res
        }
        pub fn new() -> Self {
            TaskFactory { task_id: 0.into() }
        }
        pub fn make_tasks(
            &mut self,
            amount: usize,
            time_fixpoint: DateTime<Utc>,
            duration: Milliseconds,
            end_offset: Duration,
            start_offset: Option<Duration>,
            effect: Option<f64>,
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
                    effect: effect.unwrap_or_default(),
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

    // Uses `Duration::hours(n)` for input
    macro_rules! make_expected_unpublished_events_hours {
        ( $start_time:expr, $($offset:expr),* ) =>  {
            {
                let mut vec = Vec::new();
                let mut __id = 0; // __ is to avoid name clash since this is code in which will be written into the file
                $(
                    vec.push(UnpublishedEvent {
                        task_id: __id.into(),
                        start_time: $start_time + Duration::hours($offset),
                    });
                    __id += 1;
                )*
                vec
            }
        }
    }

    #[test]
    fn all_permutations_scheduler_simple_reordered() {
        let scheduler = AllPermutationsAlgorithm;
        let start = Utc::now();

        let tasks = vec![
            Task {
                id: 0.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(2),
                },
                duration: Duration::seconds(2).into(),
                effect: 3.0,
            },
            Task {
                id: 1.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(2),
                },
                duration: Duration::seconds(1).into(),
                effect: 4.0,
            },
        ];

        let mut graph = DiscreteGraph::new(vec![4.0, 3.0, 3.0], Duration::seconds(1), start);

        let mut events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 1, 0);

        events.sort_by_key(|event| event.task_id);
        assert_eq!(events, expected)
    }
    #[test]
    fn all_permutations_scheduler_simple() {
        let scheduler = AllPermutationsAlgorithm;
        let start = Utc::now();

        let tasks = vec![
            Task {
                id: 0.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(3),
                },
                duration: Duration::seconds(1).into(),
                effect: 4.0,
            },
            Task {
                id: 1.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(3),
                },
                duration: Duration::seconds(2).into(),
                effect: 3.0,
            },
        ];

        let mut graph = DiscreteGraph::new(vec![4.0, 3.0, 3.0], Duration::seconds(1), start);

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 0, 1);

        assert_eq!(events, expected)
    }
    #[test]
    fn global_scheduler_simple_reorder() {
        let scheduler = GlobalSchedulerAlgorithm;
        let start = Utc::now();
        let tasks = vec![
            Task {
                id: 0.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(3),
                },
                duration: Duration::seconds(2).into(),
                effect: 3.0,
            },
            Task {
                id: 1.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(3),
                },
                duration: Duration::seconds(1).into(),
                effect: 4.0,
            },
        ];

        let mut graph = DiscreteGraph::new(vec![4.0, 3.0, 3.0], Duration::seconds(1), start);

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 0, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn global_scheduler_simple() {
        let scheduler = GlobalSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = vec![
            Task {
                id: 0.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(2),
                },
                duration: Duration::seconds(1).into(),
                effect: 4.0,
            },
            Task {
                id: 1.into(),
                timespan: Timespan {
                    start,
                    end: start + Duration::seconds(2),
                },
                duration: Duration::seconds(2).into(),
                effect: 3.0,
            },
        ];

        let mut graph = DiscreteGraph::new(vec![4.0, 3.0, 3.0], Duration::seconds(1), start);

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 0, 1);

        assert_eq!(events, expected)
    }
    #[test]
    fn global_scheduler_mutiple_tasks() {
        let scheduler = GlobalSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            5,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(6),
            None,
            Some(1.0),
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2, 2, 2, 3, 1);

        assert_eq!(events, expected)
    }
    #[test]
    fn global_scheduler_floor_or_ceil() {
        let scheduler = GlobalSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            5,
            start,
            (Duration::seconds(3) + Duration::milliseconds(600)).into(),
            Duration::seconds(6),
            None,
            Some(1.0),
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2, 1, 2, 1, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn global_scheduler_negative_graph() {
        let scheduler = GlobalSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(7),
            None,
            Some(1.0),
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 4);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_parabola_3elem() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::seconds(2).into(),
            Duration::seconds(3),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(vec![3.0, 5.0, 4.0], Duration::seconds(1), start);

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
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
            Duration::seconds(7),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
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
            Duration::seconds(7),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
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
            Duration::seconds(7),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
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
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_task_starts_before_graph() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start - Duration::seconds(1),
            Duration::seconds(3).into(),
            Duration::seconds(7),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_24_hour() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            1,
            start,
            Duration::hours(4).into(),
            Duration::hours(24),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![3.0, 7.0, 6.0, 5.0, 4.0, 8.0],
            Duration::hours(4),
            start,
        );
        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events_hours!(start, 20);

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
            Duration::seconds(7),
            None,
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 2, 2, 2);

        assert_eq!(events, expected)
    }
    #[test]
    fn naive_scheduler_start_time_offset() {
        let scheduler = NaiveSchedulerAlgorithm;
        let start = Utc::now();

        let tasks = TaskFactory::new().make_tasks(
            3,
            start,
            Duration::seconds(3).into(),
            Duration::seconds(7),
            Some(Duration::seconds(3)),
            None,
        );

        let mut graph = DiscreteGraph::new(
            vec![0.0, 5.0, 8.0, 9.0, 8.0, 5.0, 0.0],
            Duration::seconds(1),
            start,
        );

        let events = scheduler.schedule(&mut graph, tasks).unwrap();
        let expected = make_expected_unpublished_events!(start, 3, 3, 3);

        assert_eq!(events, expected)
    }
}

#[cfg(test)]
mod dependancy_tests {
    use crate::scheduling::task_for_scheduler::TaskForScheduler;
    use chrono::{DateTime, Duration, Utc};
    use protocol::graph::DiscreteGraph;
    use protocol::time::Timespan;

    use super::{get_task_as_timeslots, make_p_from_duration_in_timeslots};

    /*
    fn find_best_event(task: &TaskForScheduler, graph: &DiscreteGraph) -> Result<usize> {
        let (timeslot_start, timeslot_end, timeslot_duration) = get_task_as_timeslots(task, graph)?;

        // The set of values I for task T
        let task_interval = &graph.get_values()[timeslot_start..=timeslot_end];

        // The set P(d') created using I
        let mapped_graph = make_p_from_duration_in_timeslots(timeslot_duration, task_interval);

        // Getting the max value for P(d'),
        // then finding the timeslot in which the event should begin
        let greatest_index = mapped_graph
            .iter()
            .position_max_by(|x, y| x.total_cmp(y))
            .unwrap();

        Ok(timeslot_start + greatest_index)
    }
    */

    #[test]
    fn test_get_task_as_timeslots() {
        let start: DateTime<Utc> = "2024-05-07T12:17:31.733714688Z".parse().unwrap();

        let task = TaskForScheduler {
            id: 0.into(),
            timespan: Timespan {
                start: "2024-05-07T12:48:31.733714688Z".parse().unwrap(),
                end: "2024-05-07T13:01:31.733714688Z".parse().unwrap(),
            },
            duration: Duration::minutes(7).into(),
            effect: 912.8998498308304,
        };
        let graph = DiscreteGraph::new(
            vec![
                1541147.67998195,
                1088003.647948328,
                50484.64794832778,
                1075118.1252278527,
                1592786.6118938737,
                259537.97643355033,
                1149593.463528526,
                960643.5871385897,
                1013049.717543158,
                22491.41276946908,
                1579464.2177915303,
                872568.8148938086,
                163715.10484781698,
                806499.810535143,
                427396.35709333245,
                15800.171251045882,
                1238893.2358290735,
                609561.7269012656,
                1177272.0121892118,
                -7817.8221620834975,
                177794.22293010197,
                275117.1165836744,
                482212.2382850721,
                1394492.742329645,
                1257901.4323725847,
                543818.9171784238,
                878779.0603348233,
                870707.5318972562,
                848783.8856715219,
                1200277.7676847389,
                1401702.709918117,
                433056.43667152204,
                444512.9525232377,
                701958.557279607,
                838140.9725433062,
                716655.0426518656,
                1152432.551023369,
                760488.6983480075,
                1083676.099008391,
                1226979.033421366,
                1188384.336973002,
                1093766.9536934677,
                697539.8855571696,
                983674.677622651,
                1102372.4221992705,
                409292.2867258331,
                442977.22846594884,
                1501895.6993837866,
                14502.736883796792,
                71607.81270232261,
                524665.0110117742,
                653052.7296145279,
                620798.9069241448,
                88609.20126796105,
                716973.4244727911,
                79110.33110650051,
                206973.80291206803,
                789071.1906158836,
                466556.15668451163,
                588704.0671254967,
            ],
            Duration::minutes(1),
            start,
        );

        let (timeslot_start, timeslot_end, timeslot_duration) =
            get_task_as_timeslots(&task, &graph).unwrap();

        assert_eq!(timeslot_start, 31, "Timeslot start is computed wrong");
        assert_eq!(timeslot_end, 44, "Timeslot end is computed wrong");
        assert_eq!(timeslot_duration, 7, "Timeslot duration is computed wrong");
    }
    #[test]
    fn test_get_values() {
        let start: DateTime<Utc> = "2024-05-07T12:17:31.733714688Z".parse().unwrap();

        let task = TaskForScheduler {
            id: 0.into(),
            timespan: Timespan {
                start: "2024-05-07T12:48:31.733714688Z".parse().unwrap(),
                end: "2024-05-07T13:01:31.733714688Z".parse().unwrap(),
            },
            duration: Duration::minutes(7).into(),
            effect: 912.8998498308304,
        };
        let graph = DiscreteGraph::new(
            vec![
                1541147.67998195,
                1088003.647948328,
                50484.64794832778,
                1075118.1252278527,
                1592786.6118938737,
                259537.97643355033,
                1149593.463528526,
                960643.5871385897,
                1013049.717543158,
                22491.41276946908,
                1579464.2177915303,
                872568.8148938086,
                163715.10484781698,
                806499.810535143,
                427396.35709333245,
                15800.171251045882,
                1238893.2358290735,
                609561.7269012656,
                1177272.0121892118,
                -7817.8221620834975,
                177794.22293010197,
                275117.1165836744,
                482212.2382850721,
                1394492.742329645,
                1257901.4323725847,
                543818.9171784238,
                878779.0603348233,
                870707.5318972562,
                848783.8856715219,
                1200277.7676847389,
                1401702.709918117,
                433056.43667152204,
                444512.9525232377,
                701958.557279607,
                838140.9725433062,
                716655.0426518656,
                1152432.551023369,
                760488.6983480075,
                1083676.099008391,
                1226979.033421366,
                1188384.336973002,
                1093766.9536934677,
                697539.8855571696,
                983674.677622651,
                1102372.4221992705,
                409292.2867258331,
                442977.22846594884,
                1501895.6993837866,
                14502.736883796792,
                71607.81270232261,
                524665.0110117742,
                653052.7296145279,
                620798.9069241448,
                88609.20126796105,
                716973.4244727911,
                79110.33110650051,
                206973.80291206803,
                789071.1906158836,
                466556.15668451163,
                588704.0671254967,
            ],
            Duration::minutes(1),
            start,
        );

        let (timeslot_start, timeslot_end, _) = get_task_as_timeslots(&task, &graph).unwrap();

        let actual = &graph.get_values()[timeslot_start..=timeslot_end];

        let expected = vec![
            433056.43667152204,
            444512.9525232377,
            701958.557279607,
            838140.9725433062,
            716655.0426518656,
            1152432.551023369,
            760488.6983480075,
            1083676.099008391,
            1226979.033421366,
            1188384.336973002,
            1093766.9536934677,
            697539.8855571696,
            983674.677622651,
            1102372.4221992705,
        ];

        assert_eq!(actual, expected);
    }
    #[test]
    fn test_make_p() {
        let start: DateTime<Utc> = "2024-05-07T12:17:31.733714688Z".parse().unwrap();

        let task = TaskForScheduler {
            id: 0.into(),
            timespan: Timespan {
                start: "2024-05-07T12:48:31.733714688Z".parse().unwrap(),
                end: "2024-05-07T13:01:31.733714688Z".parse().unwrap(),
            },
            duration: Duration::minutes(7).into(),
            effect: 912.8998498308304,
        };
        let graph = DiscreteGraph::new(
            vec![
                1541147.67998195,
                1088003.647948328,
                50484.64794832778,
                1075118.1252278527,
                1592786.6118938737,
                259537.97643355033,
                1149593.463528526,
                960643.5871385897,
                1013049.717543158,
                22491.41276946908,
                1579464.2177915303,
                872568.8148938086,
                163715.10484781698,
                806499.810535143,
                427396.35709333245,
                15800.171251045882,
                1238893.2358290735,
                609561.7269012656,
                1177272.0121892118,
                -7817.8221620834975,
                177794.22293010197,
                275117.1165836744,
                482212.2382850721,
                1394492.742329645,
                1257901.4323725847,
                543818.9171784238,
                878779.0603348233,
                870707.5318972562,
                848783.8856715219,
                1200277.7676847389,
                1401702.709918117,
                433056.43667152204,
                444512.9525232377,
                701958.557279607,
                838140.9725433062,
                716655.0426518656,
                1152432.551023369,
                760488.6983480075,
                1083676.099008391,
                1226979.033421366,
                1188384.336973002,
                1093766.9536934677,
                697539.8855571696,
                983674.677622651,
                1102372.4221992705,
                409292.2867258331,
                442977.22846594884,
                1501895.6993837866,
                14502.736883796792,
                71607.81270232261,
                524665.0110117742,
                653052.7296145279,
                620798.9069241448,
                88609.20126796105,
                716973.4244727911,
                79110.33110650051,
                206973.80291206803,
                789071.1906158836,
                466556.15668451163,
                588704.0671254967,
            ],
            Duration::minutes(1),
            start,
        );

        let (timeslot_start, timeslot_end, timeslot_duration) =
            get_task_as_timeslots(&task, &graph).unwrap();

        let task_interval = &graph.get_values()[timeslot_start..=timeslot_end];

        let actual = make_p_from_duration_in_timeslots(timeslot_duration, task_interval);

        let expected = vec![
            5047245.211040915,
            5697864.873377783,
            6480330.954275912,
            6966756.733969308,
            7222382.71511947,
            7203267.558024773,
            7034509.6846240545,
            7376393.408475317,
        ];

        assert_eq!(actual, expected);
    }
}
