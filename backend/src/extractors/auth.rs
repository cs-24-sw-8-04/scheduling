use axum::{
    async_trait,
    extract::FromRequestParts,
    http::{request::Parts, HeaderMap, StatusCode},
};
use serde::{Deserialize, Serialize};
use sqlx::SqlitePool;
use uuid::Uuid;

use crate::data_model::account::AccountId;

#[derive(Deserialize, Serialize, sqlx::Type)]
#[sqlx(transparent)]
pub struct AuthToken(Uuid);

impl AuthToken {
    fn new() -> Self {
        AuthToken(Uuid::new_v4())
    }

    fn try_parse(input: &str) -> Result<AuthToken, uuid::Error> {
        let uuid = Uuid::try_parse(input)?;
        Ok(AuthToken(uuid))
    }
}

// Account id
pub struct Authentication(pub AccountId);

#[async_trait]
impl FromRequestParts<SqlitePool> for Authentication {
    type Rejection = (StatusCode, String);

    async fn from_request_parts(
        parts: &mut Parts,
        pool: &SqlitePool,
    ) -> Result<Self, Self::Rejection> {
        match get_auth_token(&parts.headers) {
            Some(token) => {
                if let Some(account_id) = get_account_id_from_token(token, pool).await {
                    Ok(Authentication(account_id))
                } else {
                    Err((
                        StatusCode::UNAUTHORIZED,
                        "Auth token is not in the database".to_string(),
                    ))
                }
            }
            _ => Err((
                StatusCode::UNAUTHORIZED,
                "Auth token invalid or missing".to_string(),
            )),
        }
    }
}

fn get_auth_token(headers: &HeaderMap) -> Option<AuthToken> {
    let string = headers.get("X-Auth-Token")?.to_str().ok()?;
    AuthToken::try_parse(string).ok()
}

async fn get_account_id_from_token(token: AuthToken, pool: &SqlitePool) -> Option<AccountId> {
    sqlx::query_scalar!(
        r#"
        SELECT account_id as "id: AccountId"
        FROM AuthTokens
        WHERE id = ?
        "#,
        token
    )
    .fetch_optional(pool)
    .await
    .ok()?
}

pub async fn create_auth_token(
    account_id: AccountId,
    pool: &SqlitePool,
) -> Result<AuthToken, sqlx::Error> {
    let auth_token = AuthToken::new();

    sqlx::query!(
        r#"
        INSERT INTO AuthTokens (id, account_id)
        VALUES (?, ?)
        "#,
        auth_token,
        account_id
    )
    .execute(pool)
    .await?;

    Ok(auth_token)
}
