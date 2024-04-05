use crate::data_model::event::Event;
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct GetEventsResponse {
    pub events: Vec<Event>,
}
