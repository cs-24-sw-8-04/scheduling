mod data_model;
mod extractors;
mod handlers;
mod scheduling;

use std::error::Error;

use axum::{
    routing::{delete, get, post},
    Router,
};
use dotenv::dotenv;
use sqlx::{sqlite::SqlitePoolOptions, SqlitePool};
use tokio::net::TcpListener;

use handlers::{accounts::*, devices::*, events::*, tasks::*};
use tower_http::trace::TraceLayer;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    dotenv().ok();

    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "tower_http=debug,axum::rejection=trace".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let db_connection_string = std::env::var("DATABASE_URL")?;

    let pool = SqlitePoolOptions::new()
        .connect(&db_connection_string)
        .await?;

    sqlx::migrate!("./migrations").run(&pool).await?;

    let listener = TcpListener::bind("127.0.0.1:3000").await?;

    let app = app(pool);

    axum::serve(listener, app).await?;

    Ok(())
}

fn app(pool: SqlitePool) -> Router {
    Router::new()
        .route("/tasks/all", get(get_all_tasks))
        .route("/tasks/create", post(create_task))
        .route("/tasks/delete", delete(delete_task))
        .route("/devices/all", get(get_all_devices))
        .route("/devices/create", post(create_device))
        .route("/devices/delete", delete(delete_device))
        .route("/accounts/register", post(register_account))
        .route("/accounts/login", post(login_to_account))
        .route("/events/all", get(get_all_events))
        .route("/events/get", get(get_device_events))
        .layer(TraceLayer::new_for_http())
        .with_state(pool)
}

#[cfg(test)]
mod tests {
    use crate::scheduling::event_creation::_create_event;

    use super::*;
    use axum::{
        body::Body,
        http::{Method, Request, StatusCode},
        routing::RouterIntoService,
    };
    use chrono::{Days, Utc};
    use http_body_util::BodyExt;
    use protocol::{
        accounts::{AuthToken, RegisterOrLoginRequest, RegisterOrLoginResponse},
        devices::{CreateDeviceRequest, CreateDeviceResponse, Device, GetDevicesResponse},
        events::{GetDeviceEventsRequest, GetEventsResponse},
        tasks::{CreateTaskRequest, GetTasksResponse, Task},
        time::Timespan,
    };
    use tower::{Service, ServiceExt};
    use uuid::Uuid;

    async fn test_app() -> (Router, SqlitePool) {
        let db_connection_string = "sqlite::memory:";

        let pool = SqlitePoolOptions::new()
            .connect(db_connection_string)
            .await
            .unwrap();

        sqlx::migrate!("./migrations").run(&pool).await.unwrap();

        (app(pool.clone()), pool)
    }

    async fn get_account(app: &mut RouterIntoService<Body>) -> AuthToken {
        let request = Request::builder()
            .method(Method::POST)
            .uri("/accounts/register")
            .header("Content-Type", "application/json")
            .body(Body::from(
                serde_json::to_vec(&RegisterOrLoginRequest {
                    username: "test_user".to_string(),
                    password: "test_password".to_string(),
                })
                .unwrap(),
            ))
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let response: RegisterOrLoginResponse = serde_json::from_slice(&body).unwrap();

        response.auth_token
    }

    async fn generate_task(
        app: &mut RouterIntoService<Body>,
        auth_token: String,
        duration: i64,
        device: &Device,
    ) -> Task {
        let request = Request::builder()
            .method(Method::POST)
            .uri("/tasks/create")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::from(
                serde_json::to_vec(&CreateTaskRequest {
                    timespan: Timespan::new(
                        Utc::now(),
                        Utc::now().checked_add_days(Days::new(1)).unwrap(),
                    ),
                    duration: duration.into(),
                    device_id: device.id,
                })
                .unwrap(),
            ))
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let task: Task = serde_json::from_slice(&body).unwrap();

        task
    }

    async fn get_tasks(app: &mut RouterIntoService<Body>, auth_token: String) -> Vec<Task> {
        let request = Request::builder()
            .method(Method::GET)
            .uri("/tasks/all")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token)
            .body(Body::empty())
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let get_tasks_response: GetTasksResponse = serde_json::from_slice(&body).unwrap();

        get_tasks_response.tasks
    }

    async fn delete_task(app: &mut RouterIntoService<Body>, auth_token: String, task: Task) {
        let request = Request::builder()
            .method(Method::DELETE)
            .uri(format!("/tasks/delete?id={}", task.id))
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::empty())
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }
    }

    async fn generate_device(
        app: &mut RouterIntoService<Body>,
        auth_token: String,
        effect: f64,
    ) -> Device {
        let request = Request::builder()
            .method(Method::POST)
            .uri("/devices/create")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::from(
                serde_json::to_vec(&CreateDeviceRequest { effect }).unwrap(),
            ))
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let create_device_response: CreateDeviceResponse = serde_json::from_slice(&body).unwrap();

        create_device_response.device
    }

    async fn get_devices(app: &mut RouterIntoService<Body>, auth_token: String) -> Vec<Device> {
        let request = Request::builder()
            .method(Method::GET)
            .uri("/devices/all")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::empty())
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let get_devices_response: GetDevicesResponse = serde_json::from_slice(&body).unwrap();

        get_devices_response.devices
    }

    async fn delete_device(app: &mut RouterIntoService<Body>, auth_token: String, device: Device) {
        let request = Request::builder()
            .method(Method::DELETE)
            .uri(format!("/devices/delete?id={}", device.id))
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::empty())
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }
    }

    fn auth_token_to_uuid(auth_token: AuthToken) -> String {
        let auth_token_json = serde_json::to_string(&auth_token).unwrap();
        let uuid: Uuid = serde_json::from_str(&auth_token_json).unwrap();
        uuid.hyphenated().to_string()
    }

    #[tokio::test]
    async fn register_account() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        get_account(&mut app).await;
    }

    #[tokio::test]
    async fn login_to_account() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        get_account(&mut app).await;

        // Login to account
        let request = Request::builder()
            .method(Method::POST)
            .uri("/accounts/login")
            .header("Content-Type", "application/json")
            .body(Body::from(
                serde_json::to_vec(&RegisterOrLoginRequest {
                    username: "test_user".to_string(),
                    password: "test_password".to_string(),
                })
                .unwrap(),
            ))
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(&mut app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let _response: RegisterOrLoginResponse = serde_json::from_slice(&body).unwrap();
    }

    #[tokio::test]
    async fn create_task_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);

        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;
        let task = generate_task(&mut app, auth_token, 3600, &device).await;

        assert_ne!(task.id, (-1).into());
        assert_eq!(task.duration, 3600.into());
    }

    #[tokio::test]
    async fn get_tasks_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);

        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;
        let task = generate_task(&mut app, auth_token.clone(), 3600, &device).await;
        let all_tasks = get_tasks(&mut app, auth_token).await;

        assert_eq!(all_tasks.first().unwrap(), &task);
    }

    #[tokio::test]
    async fn delete_task_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;
        let task = generate_task(&mut app, auth_token.clone(), 3600, &device).await;
        delete_task(&mut app, auth_token.clone(), task).await;

        let all_tasks = get_tasks(&mut app, auth_token).await;

        assert!(all_tasks.is_empty());
    }

    #[tokio::test]
    async fn get_devices_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let created_devices = vec![
            generate_device(&mut app, auth_token.clone(), 1000.0).await,
            generate_device(&mut app, auth_token.clone(), 1000.0).await,
        ];

        let all_devices = get_devices(&mut app, auth_token).await;

        assert_eq!(all_devices, created_devices);
    }

    #[tokio::test]
    async fn create_device_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;

        assert_eq!(device.effect, 1000.0);
    }

    #[tokio::test]
    async fn delete_device_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;
        delete_device(&mut app, auth_token.clone(), device).await;

        let all_devices = get_devices(&mut app, auth_token).await;

        assert!(all_devices.is_empty());
    }

    #[tokio::test]
    async fn get_all_events() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();
        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let device = generate_device(&mut app, auth_token.clone(), 1000.0).await;
        let task = generate_task(&mut app, auth_token.clone(), 3600, &device).await;
        let event = _create_event(&pool, &task, Utc::now()).await.unwrap();

        let request = Request::builder()
            .method(Method::GET)
            .uri("/events/all")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token)
            .body(Body::empty())
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(&mut app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let response: GetEventsResponse = serde_json::from_slice(&body).unwrap();

        assert_eq!(response.events.first().unwrap(), &event);
    }

    #[tokio::test]
    async fn get_device_events() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();
        let auth_token = get_account(&mut app).await;
        let auth_token = auth_token_to_uuid(auth_token);
        let device = generate_device(&mut app, auth_token.clone(), 20.0).await;
        let task = generate_task(&mut app, auth_token.clone(), 20, &device).await;
        let event = _create_event(&pool, &task, Utc::now()).await.unwrap();

        let request = Request::builder()
            .method(Method::GET)
            .uri("/events/get")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token)
            .body(Body::from(
                serde_json::to_vec(&GetDeviceEventsRequest {
                    device_id: device.id,
                })
                .unwrap(),
            ))
            .unwrap();

        let response = ServiceExt::<Request<Body>>::ready(&mut app)
            .await
            .unwrap()
            .call(request)
            .await
            .unwrap();

        if response.status() != StatusCode::OK {
            let body = response.into_body().collect().await.unwrap().to_bytes();
            let body = String::from_utf8_lossy(&body);
            panic!("{}", body);
        }

        let body = response.into_body().collect().await.unwrap().to_bytes();
        let response: GetEventsResponse = serde_json::from_slice(&body).unwrap();

        assert_eq!(response.events.first().unwrap(), &event);
    }
}
