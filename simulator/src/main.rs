use anyhow::{anyhow, bail};

use http::{header::USER_AGENT, HeaderValue, Request};
use http_body_util::BodyExt;
use hyper_util::{
    client::legacy::{connect::HttpConnector, Client},
    rt::TokioExecutor,
};
use protocol::accounts::{AuthToken, RegisterOrLoginRequest, RegisterOrLoginResponse};
use tower::{Service, ServiceBuilder, ServiceExt};
use tower_http::{
    classify::{SharedClassifier, StatusInRangeAsFailures},
    decompression::{Decompression, DecompressionLayer},
    set_header::{SetRequestHeader, SetRequestHeaderLayer},
    trace::{Trace, TraceLayer},
};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

type HttpClient = Trace<
    SetRequestHeader<Decompression<Client<HttpConnector, String>>, HeaderValue>,
    SharedClassifier<StatusInRangeAsFailures>,
>;

#[tokio::main]
async fn main() -> Result<(), anyhow::Error> {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "tower_http=debug,axum::rejection=trace".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let client = Client::builder(TokioExecutor::new()).build_http();
    let mut client = ServiceBuilder::new()
        .layer(TraceLayer::new(
            StatusInRangeAsFailures::new(400..=599).into_make_classifier(),
        ))
        .layer(SetRequestHeaderLayer::overriding(
            USER_AGENT,
            HeaderValue::from_static("scheduling-simulator"),
        ))
        .layer(DecompressionLayer::new())
        .service(client);

    let auth_token = get_account(&mut client).await?;

    println!("Registered an account. Auth Token: {:?}", auth_token);

    Ok(())
}

async fn get_account(client: &mut HttpClient) -> Result<AuthToken, anyhow::Error> {
    let body = serde_json::to_string(&RegisterOrLoginRequest {
        username: "test_user".to_string(),
        password: "test_password".to_string(),
    })?;

    let request = Request::builder()
        .uri("http://localhost:3000/accounts/register")
        .method("POST")
        .header("Content-Type", "application/json")
        .body(body)?;

    let response = client.ready().await?.call(request).await?;

    if !response.status().is_success() {
        let status = response.status();
        let body = response
            .into_body()
            .collect()
            .await
            .map_err(|e| anyhow!(e))?;
        bail!(
            "status message: {} message: {:?}",
            status,
            String::from_utf8(body.to_bytes().to_vec())
        );
    }

    let body = response.into_body().collect().await.unwrap().to_bytes();
    let response: RegisterOrLoginResponse = serde_json::from_slice(&body).unwrap();

    Ok(response.auth_token)
}
