use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Deserialize, Serialize, sqlx::Type, Debug)]
#[sqlx(transparent)]
pub struct AuthToken(Uuid);

impl AuthToken {
    pub fn new() -> Self {
        AuthToken(Uuid::new_v4())
    }

    pub fn try_parse(input: &str) -> Result<AuthToken, uuid::Error> {
        let uuid = Uuid::try_parse(input)?;
        Ok(AuthToken(uuid))
    }
}

impl Default for AuthToken {
    fn default() -> Self {
        Self::new()
    }
}

#[derive(Deserialize, Serialize)]
pub struct RegisterOrLoginRequest {
    pub username: String,
    pub password: String,
}

#[derive(Deserialize, Serialize)]
pub struct RegisterOrLoginResponse {
    pub auth_token: AuthToken,
}
