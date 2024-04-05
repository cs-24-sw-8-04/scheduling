use std::time::Duration;

use axum::async_trait;
use tokio::sync::mpsc;

use crate::data_model::{event::Event, task::Task};

pub trait Scheduler {
    fn schedule(self: &mut Self, tasks: Vec<Task>) -> Vec<Event>;
}

pub enum SchedulerMessage {
    Task,
}

pub struct SchedulerTask {
    receiver: mpsc::UnboundedReceiver<SchedulerMessage>,
}

impl SchedulerTask {
    pub fn new(receiver: mpsc::UnboundedReceiver<SchedulerMessage>) -> Self {
        SchedulerTask { receiver }
    }
    pub async fn run_scheduler<TScheduler, TSchedulerCreator>(
        mut self: Self,
        scheduler_creator: TSchedulerCreator,
    ) where
        TScheduler: Scheduler,
        TSchedulerCreator: FnOnce() -> TScheduler,
    {
        let mut scheduler = scheduler_creator();

        loop {
            println!("Start of loop");
            let message = self.receiver.recv().await;
            match message {
                Some(message) => {
                    println!("Received a message. Starting the timer...");
                    let timer = tokio::time::sleep(Duration::from_secs(1 * 60));

                    tokio::select! {
                        // We have received a message
                        val = self.receiver.recv() => {
                            println!("Received a message");
                        }

                        // The timer has completed
                        val = timer => {
                            println!("Timer has completed");
                            // Collect all tasks in system
                            let tasks: Vec<Task> = vec![];

                            // Run scheduler
                            let events = scheduler.schedule(tasks);

                            // Propogate events
                            dbg!(events);
                        }
                    }
                }
                None => break,
            }
        }
    }
}
