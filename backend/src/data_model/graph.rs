use chrono::Duration;
use protocol::time::DateTimeUtc;
pub struct DiscreteGraph {
    values: Vec<f64>,
    time_delta: Duration,
    start_time: DateTimeUtc,
}

impl DiscreteGraph {
    #[allow(dead_code)] // Used in the test for the scheduler, but clippy does #?%!
    pub fn new(values: Vec<f64>, time_delta: Duration, start_time: DateTimeUtc) -> DiscreteGraph {
        DiscreteGraph {
            values,
            time_delta,
            start_time,
        }
    }
    pub fn get_values(&self) -> &Vec<f64> {
        &self.values
    }
    pub fn get_time_delta(&self) -> Duration {
        self.time_delta
    }
    pub fn get_start_time(&self) -> DateTimeUtc {
        self.start_time
    }
    pub fn sub_value(&mut self, index: usize, value: f64) {
        self.values[index] = self.values[index] - value;
    }
}
