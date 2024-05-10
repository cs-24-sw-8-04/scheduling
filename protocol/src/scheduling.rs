use crate::graph::DiscreteGraph;
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
pub struct SchedulingGlob {
    pub discrete_graph: DiscreteGraph,
    pub alg: u8,
}

impl SchedulingGlob {
    pub fn get_discrete_graph(&self) -> &DiscreteGraph {
        &self.discrete_graph
    }

    pub fn get_alg(&self) -> u8 {
        self.alg
    }
}
