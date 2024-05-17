use chrono::{DateTime, Duration, Utc};
use criterion::{criterion_group, criterion_main, Criterion};

use protocol::{tasks::TaskId, time::Timespan};
use rand::Rng;
use scheduling_backend::scheduling::{
    scheduler::{
        AllPermutationsAlgorithm, GlobalSchedulerAlgorithm, NaiveSchedulerAlgorithm,
        SchedulerAlgorithm,
    },
    task_for_scheduler::TaskForScheduler,
};

use simulator::compare_algorithms::make_discrete_graph_from_delta;

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
        max_duration: Duration,
    ) -> Vec<TaskForScheduler> {
        let mut res = Vec::new();
        let mut rng = rand::thread_rng();
        for _ in 0..amount {
            let timespan_start = Duration::minutes(0).num_minutes();
            let timespan_end = max_duration.num_minutes();

            let start_offset = Duration::minutes(rng.gen_range(timespan_start..(timespan_end - 3))); // 0=..=86396
            let end_offset = Duration::minutes(rng.gen_range(start_offset.num_minutes()..(timespan_end - 2))) // 86396=..=86397
                + Duration::minutes(2);

            let start_time = start + start_offset;
            let end_time = start + end_offset;

            let total_duration = (end_time - start_time).num_minutes();
            let duration = Duration::minutes(rng.gen_range(1..total_duration));

            let effect = rng.gen_range(1.0..=max_effect);

            res.push(TaskForScheduler::new(
                self.get_new_task_id(),
                Timespan {
                    start: start_time,
                    end: end_time,
                },
                duration.into(),
                effect,
            ));
        }
        res
    }
}

fn naive_scheduling_benchmark(c: &mut Criterion) {
    c.bench_function("naive_scheduling_benchmark, 200.000 tasks", |b| {
        let amount_of_tasks = 200000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(|| naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("naive_scheduling_benchmark, 400.000 tasks", |b| {
        let amount_of_tasks = 400000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(|| naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("naive_scheduling_benchmark, 600.000 tasks", |b| {
        let amount_of_tasks = 600000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(|| naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("naive_scheduling_benchmark, 800.000 tasks", |b| {
        let amount_of_tasks = 800000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(|| naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("naive_scheduling_benchmark, 1.000.000 tasks", |b| {
        let amount_of_tasks = 1000000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let naive_scheduler_algorithm = NaiveSchedulerAlgorithm::new();

        b.iter(|| naive_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });
}

fn global_scheduling_benchmark(c: &mut Criterion) {
    c.bench_function("global_scheduling_benchmark, 200.000 tasks", |b| {
        let amount_of_tasks = 200000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

        b.iter(|| global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("global_scheduling_benchmark, 400.000 tasks", |b| {
        let amount_of_tasks = 400000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

        b.iter(|| global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("global_scheduling_benchmark, 600.000 tasks", |b| {
        let amount_of_tasks = 600000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

        b.iter(|| global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("global_scheduling_benchmark, 800.000 tasks", |b| {
        let amount_of_tasks = 800000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

        b.iter(|| global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });

    c.bench_function("global_scheduling_benchmark, 1.000.000 tasks", |b| {
        let amount_of_tasks = 1000000;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let global_scheduler_algorithm = GlobalSchedulerAlgorithm::new();

        b.iter(|| global_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone()));
    });
}

fn all_perm_scheduling_benchmark(c: &mut Criterion) {
    c.bench_function("all_perm_scheduling_benchmark, 1 task", |b| {
        let amount_of_tasks = 1;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 2 tasks", |b| {
        let amount_of_tasks = 2;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 3 tasks", |b| {
        let amount_of_tasks = 3;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 4 tasks", |b| {
        let amount_of_tasks = 4;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 5 tasks", |b| {
        let amount_of_tasks = 5;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 6 tasks", |b| {
        let amount_of_tasks = 6;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 7 tasks", |b| {
        let amount_of_tasks = 7;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm {};

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });

    c.bench_function("all_perm_scheduling_benchmark, 8 tasks", |b| {
        let amount_of_tasks = 8;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);
        let min_available_effect = 1000.0;
        let max_available_effect = 1000000000.0;

        let tasks = criterion::black_box(TaskFactory::new().make_tasks(
            amount_of_tasks,
            time_now,
            max_effect,
            total_duration,
        ));
        let discrete_graph = criterion::black_box(make_discrete_graph_from_delta(
            time_now,
            Duration::minutes(1),
            total_duration,
            min_available_effect,
            max_available_effect,
        ));

        let all_perm_scheduler_algorithm = AllPermutationsAlgorithm::new();

        b.iter(|| {
            all_perm_scheduler_algorithm.schedule(&mut discrete_graph.clone(), tasks.clone())
        });
    });
}

criterion_group!(
    benches,
    //naive_scheduling_benchmark,
    //global_scheduling_benchmark,
    all_perm_scheduling_benchmark
);
criterion_main!(benches);
