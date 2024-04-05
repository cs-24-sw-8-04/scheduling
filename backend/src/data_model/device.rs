use derive_more::{From, Into};
use serde::{Deserialize, Serialize};

use super::account::AccountId;

#[derive(Deserialize, Serialize, Debug, sqlx::Type, PartialEq, Eq, From, Into, Clone, Copy)]
#[sqlx(transparent)]
pub struct DeviceId(i64);

#[derive(Serialize, Deserialize)]
pub struct Device {
    pub id: DeviceId,
    pub effect: f64,
    pub account_id: AccountId,
}
