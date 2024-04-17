use http::{header::USER_AGENT, HeaderValue};
use hyper_util::client::legacy::Client;
use hyper_util::rt::TokioExecutor;
use tower::ServiceBuilder;
use tower_http::{
    classify::StatusInRangeAsFailures, decompression::DecompressionLayer,
    set_header::SetRequestHeaderLayer, trace::TraceLayer,
};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

mod generate_data;
mod http_client;

const AMOUNT_OF_USERS: usize = 100;
const MAX_AMOUNT_OF_DEVICES_PER_USER: usize = 3;
const MAX_AMOUNT_OF_TASKS_PER_DEVICE: usize = 3;

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

    let auth_tokens = generate_data::generate_users(AMOUNT_OF_USERS, &mut client).await?;
    let device_ownership =
        generate_data::generate_devices(MAX_AMOUNT_OF_DEVICES_PER_USER, &mut client, &auth_tokens)
            .await?;
    let task_onwership = generate_data::generate_tasks(
        MAX_AMOUNT_OF_TASKS_PER_DEVICE,
        &mut client,
        &device_ownership,
    )
    .await?;

    println!("-------------------------------------------------------------");
    println!("Auth tokens: {:?}", auth_tokens);
    println!("-------------------------------------------------------------");
    println!("Device ownership: {:?}", device_ownership);
    println!("-------------------------------------------------------------");
    println!("Task ownership: {:?}", task_onwership);

    Ok(())
}

#[cfg(test)]
mod tests {
    use http::{header::USER_AGENT, HeaderValue};
    use hyper_util::client::legacy::Client;
    use hyper_util::rt::TokioExecutor;
    use tower::ServiceBuilder;
    use tower_http::{
        classify::StatusInRangeAsFailures,
        decompression::DecompressionLayer,
        set_header::SetRequestHeaderLayer,
        trace::TraceLayer,
    };

    use crate::generate_data;
    use crate::http_client::HttpClient;

    fn make_client() -> HttpClient {
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

        let auth_tokens = generate_data::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users");
        assert!(auth_tokens.len() == amount_of_users);
    }

    #[tokio::test]
    async fn generate_devices_test() {
        let client = &mut make_client();
        let amount_of_users = 10;
        let max_amount_of_devices_per_user = 3;

        let auth_tokens = generate_data::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users for devices");

        let device_ownership =
            generate_data::generate_devices(max_amount_of_devices_per_user, client, &auth_tokens)
                .await
                .expect("Could not create devices");

        assert!(device_ownership.keys().count() == amount_of_users);
        device_ownership
            .values()
            .for_each(|value| assert!(value.len() <= max_amount_of_devices_per_user));
    }

    #[tokio::test]
    async fn generate_tasks_test() {
        let client = &mut make_client();
        let amount_of_users = 10;
        let max_amount_of_devices_per_user = 3;
        let max_amount_of_tasks_per_device = 3;

        let auth_tokens = generate_data::generate_users(amount_of_users, client)
            .await
            .expect("Could not create users for devices");
        let device_ownership =
            generate_data::generate_devices(max_amount_of_devices_per_user, client, &auth_tokens)
                .await
                .expect("Could not create devices");

        let task_onwership = generate_data::generate_tasks(
            max_amount_of_tasks_per_device,
            client,
            &device_ownership,
        )
        .await
        .expect("Could not create tasks");

        let amount_of_generated_devices = device_ownership
            .values()
            .fold(0, |acc, values| acc + values.len());

        assert!(amount_of_generated_devices == task_onwership.keys().count());
        task_onwership
            .values()
            .for_each(|value| assert!(value.len() <= max_amount_of_tasks_per_device));
    }
}
