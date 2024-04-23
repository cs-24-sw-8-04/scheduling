use chrono::{DateTime, Duration, NaiveDateTime, TimeZone, Utc};
use derive_more::From;
use serde::{Deserialize, Serialize};

pub type DateTimeUtc = DateTime<Utc>;

#[derive(Deserialize, Serialize, sqlx::Type, Debug, PartialEq, Eq, Clone, Copy, From)]
#[sqlx(transparent)]
pub struct Milliseconds(i64);

impl From<Milliseconds> for i64 {
    fn from(value: Milliseconds) -> Self {
        value.0
    }
}

impl From<Milliseconds> for Duration {
    fn from(val: Milliseconds) -> Self {
        Duration::try_milliseconds(val.0).unwrap()
    }
}

impl From<Duration> for Milliseconds {
    fn from(value: Duration) -> Self {
        Milliseconds(value.num_milliseconds())
    }
}

#[derive(Deserialize, Serialize, Debug, PartialEq, Eq, Clone)]
pub struct Timespan {
    pub start: DateTimeUtc,
    pub end: DateTimeUtc,
}

impl Timespan {
    pub fn new(start: DateTimeUtc, end: DateTimeUtc) -> Self {
        assert!(
            start <= end,
            "The timespans' end time is before the start time."
        );
        Timespan { start, end }
    }

    pub fn new_from_naive(start: NaiveDateTime, end: NaiveDateTime) -> Self {
        fn to_utc(date_time: NaiveDateTime) -> DateTimeUtc {
            Utc::from_utc_datetime(&Utc, &date_time)
        }
        Timespan::new(to_utc(start), to_utc(end))
    }
}
