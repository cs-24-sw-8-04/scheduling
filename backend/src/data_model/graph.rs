use chrono::Duration;
use protocol::time::DateTimeUtc;
pub struct DescreteGraph {
    values: Vec<f64>,
    time_delta: Duration,
    start_time: DateTimeUtc,
}

impl DescreteGraph {
    pub fn get_values(&self) -> &Vec<f64> {
        &self.values
    }
    pub fn get_time_delta(&self) -> &Duration {
        &self.time_delta
    }
    pub fn get_start_time(&self) -> &DateTimeUtc {
        &self.start_time
    }
}
