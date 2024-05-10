use anyhow::Result;
use compare_algorithms::compare;
use http::{header::USER_AGENT, HeaderValue};
use hyper_util::client::legacy::Client;
use hyper_util::rt::TokioExecutor;
use tower::ServiceBuilder;
use tower_http::{
    classify::StatusInRangeAsFailures, decompression::DecompressionLayer,
    set_header::SetRequestHeaderLayer, trace::TraceLayer,
};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod compare_algorithms;
mod data_factory;
mod http_client;

#[tokio::main]
async fn main() -> Result<()> {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env().unwrap_or_else(|_| {
                "simulator=trace,tower_http=debug,axum::rejection=trace".into()
            }),
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

    compare(&mut client).await?;

    Ok(())
}

#[cfg(test)]
mod tests {
    use crate::data_factory;
    use crate::http_client::HttpClient;
    use chrono::{Duration, Utc};

    use http::{header::USER_AGENT, HeaderValue};
    use hyper_util::{client::legacy::Client, rt::TokioExecutor};
    use tower::ServiceBuilder;
    use tower_http::{
        classify::StatusInRangeAsFailures, decompression::DecompressionLayer,
        set_header::SetRequestHeaderLayer, trace::TraceLayer,
    };

    pub fn make_client() -> HttpClient {
        let client = Client::builder(TokioExecutor::new()).build_http();
        ServiceBuilder::new()
            .layer(TraceLayer::new(
                StatusInRangeAsFailures::new(400..=599).into_make_classifier(),
            ))
            .layer(SetRequestHeaderLayer::overriding(
                USER_AGENT,
                HeaderValue::from_static("scheduling-simulator"),
            ))
            .layer(DecompressionLayer::new())
            .service(client)
    }

    #[tokio::test]
    async fn generate_users_test() {
        let client = &mut make_client();
        let amount_of_users = 10;

        let auth_tokens = data_factory::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users");
        assert!(auth_tokens.len() == amount_of_users);
    }

    #[tokio::test]
    async fn generate_devices_test() {
        let client = &mut make_client();
        let amount_of_users = 10;
        let amount_of_devices_per_user = 3;
        let min_effect = 10.0;
        let max_effect = 10000.0;

        let auth_tokens = data_factory::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users for devices");

        let device_ownership = data_factory::generate_devices(
            amount_of_devices_per_user,
            client,
            &auth_tokens,
            min_effect,
            max_effect,
        )
        .await
        .expect("Could not create devices");

        assert!(device_ownership.keys().count() == amount_of_users);
        device_ownership
            .values()
            .for_each(|value| assert!(value.len() == amount_of_devices_per_user));
    }

    #[tokio::test]
    async fn generate_tasks_test() {
        let client = &mut make_client();
        let amount_of_users = 10;
        let amount_of_devices_per_user = 3;
        let min_amount_of_tasks_per_device = 1;
        let max_amount_of_tasks_per_device = 3;
        let min_effect = 10.0;
        let max_effect = 10000.0;
        let time_now = Utc::now();
        let total_duration = Duration::hours(24);

        let auth_tokens = data_factory::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users for devices");
        let device_ownership = data_factory::generate_devices(
            amount_of_devices_per_user,
            client,
            &auth_tokens,
            min_effect,
            max_effect,
        )
        .await
        .expect("Could not create devices");

        let task_ownership = data_factory::generate_tasks(
            min_amount_of_tasks_per_device,
            max_amount_of_tasks_per_device,
            client,
            &device_ownership,
            total_duration,
            time_now,
        )
        .await
        .expect("Could not create tasks");

        let amount_of_generated_devices = device_ownership
            .values()
            .fold(0, |acc, values| acc + values.len());

        assert!(amount_of_generated_devices == task_ownership.keys().count());
        task_ownership
            .values()
            .for_each(|value| assert!(value.len() <= max_amount_of_tasks_per_device));
    }
}
