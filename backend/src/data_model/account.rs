use derive_more::{From, Into};
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into)]
#[sqlx(transparent)]
pub struct AccountId(i64);
