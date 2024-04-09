use derive_more::{Display, From, Into};
use serde::{Deserialize, Serialize};

use super::account::AccountId;

#[derive(
    Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy, Display,
)]
#[sqlx(transparent)]
pub struct DeviceId(i64);

#[derive(Serialize, Deserialize, Debug, PartialEq)]
pub struct Device {
    pub id: DeviceId,
    pub effect: f64,
    pub account_id: AccountId,
}
