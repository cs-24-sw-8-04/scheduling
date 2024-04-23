use chrono::{DateTime, TimeDelta, Utc};
use criterion::{criterion_group, criterion_main, Criterion};

use rand::Rng;
use scheduling_backend::data_model::graph::DiscreteGraph;
use scheduling_backend::scheduling::scheduler::{GlobalSchedulerAlgorithm, SchedulerAlgorithm};
use scheduling_backend::scheduling::{
    scheduler::NaiveSchedulerAlgorithm, task_for_scheduler::TaskForScheduler,
};

use either::*;
use protocol::{tasks::TaskId, time::Timespan};

struct TaskFactory {
    task_id: TaskId,
}
impl TaskFactory {
    fn get_new_task_id(&mut self) -> TaskId {
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
        start: DateTime<Utc>,
        max_effect: f64,
        total_duration: TimeDelta,
    ) -> Vec<TaskForScheduler> {
        let mut res = Vec::new();
        let mut rng = rand::thread_rng();
        for _ in 0..amount {
            let timespan_start = TimeDelta::seconds(0).num_seconds();
            let timespan_end = total_duration.num_seconds();

            let start_offset = TimeDelta::seconds(rng.gen_range(timespan_start..timespan_end));
            let end_offset =
                TimeDelta::seconds(rng.gen_range(start_offset.num_seconds()..timespan_end))
                    + TimeDelta::seconds(1);

            let start_time = start + start_offset;
            let end_time = start + end_offset;

            let total_duration = (end_time - start_time).num_seconds();
            let duration = rng.gen_range(1..=total_duration);

            let effect = rng.gen_range(1.0..=max_effect);

            res.push(TaskForScheduler {
                id: self.get_new_task_id(),
                timespan: Timespan {
                    start: start_time,
                    end: end_time,
                },
                duration: duration.into(),
                effect,
            });
        }
        res
    }
}

fn make_discrete_graph(
    time_now: DateTime<Utc>,
    either: Either<Vec<f64>, TimeDelta>,
    total_duration: TimeDelta,
) -> DiscreteGraph {
    match either {
        Either::Left(vec) => DiscreteGraph::new(
            vec.clone(),
            TimeDelta::seconds(total_duration.num_seconds() / vec.len() as i64),
            time_now,
        ),
        Either::Right(delta) => DiscreteGraph::new(
            (0..(total_duration.num_seconds() / delta.num_seconds()))
                .map(|num| (num * 2) as f64)
                .collect::<Vec<f64>>(),
            delta,
            time_now,
        ),
    }
}

fn naive_scheduling_benchmark(c: &mut Criterion) {
    // One-time setup code goes here
    c.bench_function("naive_scheduling_benchmark", |b| {
        // Per-sample (note that a sample can be many iterations) setup goes here
        let amount_of_tasks = 10000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = TimeDelta::hours(24);

        let tasks =
            TaskFactory::new().make_tasks(amount_of_tasks, time_now, max_effect, total_duration);
        let discrete_graph =
            make_discrete_graph(time_now, either::Right(TimeDelta::hours(4)), total_duration);

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(||
            // Measured code goes here
            naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });
}

fn global_scheduling_benchmark(c: &mut Criterion) {
    // One-time setup code goes here
    let amount_of_tasks = 10000;
    let max_effect = 10000.0;
    let time_now = Utc::now();
    let total_duration = TimeDelta::hours(24);

    let tasks =
        TaskFactory::new().make_tasks(amount_of_tasks, time_now, max_effect, total_duration);
    let discrete_graph =
        make_discrete_graph(time_now, either::Right(TimeDelta::hours(4)), total_duration);

    let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

    c.bench_function("global_scheduling_benchmark", |b| {
        // Per-sample (note that a sample can be many iterations) setup goes here
        b.iter(||
            // Measured code goes here
            global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });
}

criterion_group!(
    benches,
    naive_scheduling_benchmark,
    global_scheduling_benchmark
);
criterion_main!(benches);
