use axum::{
    async_trait,
    extract::FromRequestParts,
    http::{request::Parts, HeaderMap, StatusCode},
};
use protocol::accounts::AuthToken;
use sqlx::SqlitePool;

use crate::{data_model::account::AccountId, MyState};

// Account id
pub struct Authentication(pub AccountId);

#[async_trait]
impl FromRequestParts<MyState> for Authentication {
    type Rejection = (StatusCode, String);

    async fn from_request_parts(
        parts: &mut Parts,
        state: &MyState,
    ) -> Result<Self, Self::Rejection> {
        match get_auth_token(&parts.headers) {
            Some(token) => {
                if let Some(account_id) = get_account_id_from_token(token, &state.pool).await {
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
