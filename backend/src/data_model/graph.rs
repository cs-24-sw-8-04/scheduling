use chrono::Duration;
use protocol::time::DateTimeUtc;

#[derive(Clone)]
pub struct DiscreteGraph {
    values: Vec<f64>,
    time_delta: Duration,
    start_time: DateTimeUtc,
    end_time: DateTimeUtc,
}

impl DiscreteGraph {
    pub fn new(values: Vec<f64>, time_delta: Duration, start_time: DateTimeUtc) -> DiscreteGraph {
        let len = values.len() as i32;
        DiscreteGraph {
            values,
            time_delta,
            start_time,
            end_time: start_time + time_delta * len,
        }
    }
    pub fn get_values(&self) -> &Vec<f64> {
        &self.values
    }
    pub fn get_values_mut(&mut self) -> &mut Vec<f64> {
        self.values.as_mut()
    }
    pub fn get_time_delta(&self) -> Duration {
        self.time_delta
    }
    pub fn get_start_time(&self) -> DateTimeUtc {
        self.start_time
    }
    pub fn get_end_time(&self) -> DateTimeUtc {
        self.end_time
    }
    pub fn sub_value(&mut self, index: usize, effect: f64) {
        self.values[index] -= effect;
    }
    pub fn sub_values(&mut self, index: usize, effect: f64, duration: usize) {
        for n in 0..duration {
            self.sub_value(index + n, effect);
        }
    }
}
