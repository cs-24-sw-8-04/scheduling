use chrono::{DateTime, Duration, TimeDelta, Utc};
use criterion::{criterion_group, criterion_main, Criterion};

use rand::Rng;
use scheduling_backend::data_model::graph::DiscreteGraph;
use scheduling_backend::scheduling::scheduler::{GlobalSchedulerAlgorithm, SchedulerAlgorithm};
use scheduling_backend::scheduling::{
    scheduler::NaiveSchedulerAlgorithm, task_for_scheduler::TaskForScheduler,
};

use protocol::{tasks::TaskId, time::Timespan};

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
    pub fn make_tasks(&mut self, amount: usize, start: DateTime<Utc>) -> Vec<TaskForScheduler> {
        let mut res = Vec::new();
        for _ in 0..amount {
            let mut rng = rand::thread_rng();
            let timespan_start = Duration::seconds(0).num_seconds();
            let timespan_end = Duration::hours(24).num_seconds();

            let start_offset = Duration::seconds(rng.gen_range(timespan_start..timespan_end));
            let end_offset =
                Duration::seconds(rng.gen_range(start_offset.num_seconds()..timespan_end))
                    + Duration::seconds(1);

            let start_time = start + start_offset;
            let end_time = start + end_offset;

            let total_duration = (end_time - start_time).num_seconds();
            let duration = rng.gen_range(1..=total_duration);

            let max_effect = 10000.0;
            let effect = rng.gen_range(1.0..=max_effect);

            res.push(TaskForScheduler {
                id: self.get_task_id(),
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

fn naive_scheduling_benchmark(c: &mut Criterion) {
    // One-time setup code goes here
    let amount_of_tasks = 10000;
    let time_now = Utc::now();
    let tasks = TaskFactory::new().make_tasks(amount_of_tasks, time_now);
    let discrete_graph = DiscreteGraph::new(
        [0.0, 2.0, 4.0, 6.0, 5.0, 2.0, 0.0].to_vec(),
        TimeDelta::hours(4),
        time_now,
    );
    let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

    c.bench_function("naive_scheduling_benchmark", |b| {
        // Per-sample (note that a sample can be many iterations) setup goes here
        b.iter(||
            // Measured code goes here
            naive_scheduler_algorithm.schedule(discrete_graph.clone(), tasks.clone()));
    });
}

fn global_scheduling_benchmark(c: &mut Criterion) {
    // One-time setup code goes here
    let amount_of_tasks = 10000;
    let time_now = Utc::now();
    let tasks = TaskFactory::new().make_tasks(amount_of_tasks, time_now);
    let discrete_graph = DiscreteGraph::new(
        [0.0, 2.0, 4.0, 6.0, 5.0, 2.0, 0.0].to_vec(),
        TimeDelta::hours(4),
        time_now,
    );
    let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

    c.bench_function("global_scheduling_benchmark", |b| {
        // Per-sample (note that a sample can be many iterations) setup goes here
        b.iter(||
            // Measured code goes here
            global_scheduler_algorithm.schedule(discrete_graph.clone(), tasks.clone()));
    });
}

criterion_group!(
    benches,
    naive_scheduling_benchmark,
    global_scheduling_benchmark
);
criterion_main!(benches);
