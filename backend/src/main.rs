mod data_model;
mod extractors;
mod handlers;
mod scheduling;

use std::error::Error;

use axum::{
    debug_handler,
    extract::State,
    http::StatusCode,
    routing::{delete, get, post},
    Json, Router,
};
use clap::Parser;
use dotenv::dotenv;
use protocol::graph::DiscreteGraph;
use scheduling::{
    background_service::{
        background_service, simulator_background_service, BackgroundServiceMessage,
    },
    scheduler::{AllPermutationsAlgorithm, GlobalSchedulerAlgorithm, NaiveSchedulerAlgorithm},
};
use sqlx::{sqlite::SqlitePoolOptions, SqlitePool};
use tokio::{
    net::TcpListener,
    sync::mpsc::{error::SendError, unbounded_channel, UnboundedSender},
};

use handlers::{accounts::*, devices::*, events::*, tasks::*};
use tower_http::trace::TraceLayer;
use tracing::{event, Level};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

use protocol::scheduling::SchedulingGlob;

use crate::scheduling::background_service::run_algorithm;

#[derive(Clone)]
pub struct MyState {
    pool: SqlitePool,
    sender: UnboundedSender<BackgroundServiceMessage>,
}

impl MyState {
    pub fn update_schedule(&self) -> Result<(), SendError<BackgroundServiceMessage>> {
        self.sender.send(BackgroundServiceMessage::Update)?;
        Ok(())
    }
}

#[derive(Parser, Debug)]
#[command(version, about)]
struct Args {
    // Whether or not to run in simulator mode
    #[arg(long)]
    simulator: bool,
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    dotenv().ok();

    let args = Args::parse();

    let simulator_mode = args.simulator;

    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend=trace,tower_http=debug,axum::rejection=trace".into()),
        )
        .with(tracing_subscriber::fmt::layer().pretty())
        .init();

    let db_connection_string = std::env::var("DATABASE_URL")?;

    let pool = SqlitePoolOptions::new()
        .connect(&db_connection_string)
        .await?;

    sqlx::migrate!("./migrations").run(&pool).await?;

    let listener = TcpListener::bind("127.0.0.1:3000").await?;

    let (sender, receiver) = unbounded_channel();

    let state = MyState {
        pool: pool.clone(),
        sender,
    };

    let app = app(state, simulator_mode);

    let background_task = if simulator_mode {
        event!(target: "backend", Level::INFO, "Running in simulator mode");
        tokio::spawn(simulator_background_service(
            receiver,
            pool,
            NaiveSchedulerAlgorithm::new,
        ))
    } else {
        tokio::spawn(background_service(
            receiver,
            pool,
            NaiveSchedulerAlgorithm::new,
        ))
    };

    axum::serve(listener, app).await?;

    background_task.await?;

    Ok(())
}

fn app(state: MyState, simulator_mode: bool) -> Router {
    let mut router = Router::new()
        .route("/tasks/all", get(get_all_tasks))
        .route("/tasks/create", post(create_task))
        .route("/tasks/delete", delete(delete_task))
        .route("/devices/all", get(get_all_devices))
        .route("/devices/create", post(create_device))
        .route("/devices/delete", delete(delete_device))
        .route("/accounts/register", post(register_account))
        .route("/accounts/login", post(login_to_account))
        .route("/events/all", get(get_all_events))
        .route("/events/get", get(get_device_event));

    if simulator_mode {
        router = router.route("/scheduling/run", get(run_scheduling));
    }

    router.layer(TraceLayer::new_for_http()).with_state(state)
}

#[debug_handler]
async fn run_scheduling(
    State(state): State<MyState>,
    Json(scheduling_glob): Json<SchedulingGlob>,
) -> Result<Json<DiscreteGraph>, (StatusCode, String)> {
    let mut discrete_graph = scheduling_glob.get_discrete_graph().clone();
    let _ = match scheduling_glob.get_alg() {
        0 => {
            run_algorithm(
                &state.pool,
                &mut NaiveSchedulerAlgorithm::new(),
                &mut discrete_graph,
            )
            .await
        }
        1 => {
            run_algorithm(
                &state.pool,
                &mut GlobalSchedulerAlgorithm::new(),
                &mut discrete_graph,
            )
            .await
        }
        2 => {
            run_algorithm(
                &state.pool,
                &mut AllPermutationsAlgorithm::new(),
                &mut discrete_graph,
            )
            .await
        }
        _ => Ok(()), // Return error instead
    };
    Ok(Json(discrete_graph))
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
    use chrono::{Days, Duration, Utc};
    use http_body_util::BodyExt;
    use protocol::{
        accounts::{AuthToken, RegisterOrLoginRequest, RegisterOrLoginResponse},
        devices::{CreateDeviceRequest, CreateDeviceResponse, Device, GetDevicesResponse},
        events::{GetDeviceEventRequest, GetEventResponse, GetEventsResponse},
        tasks::{CreateTaskRequest, GetTasksResponse, Task},
        time::{DateTimeUtc, Timespan},
    };
    use tower::{Service, ServiceExt};

    async fn test_app() -> (Router, SqlitePool) {
        let db_connection_string = "sqlite::memory:";

        let pool = SqlitePoolOptions::new()
            .connect(db_connection_string)
            .await
            .unwrap();

        let (sender, receiver) = unbounded_channel();

        // Leak the receiver to keep the channel open
        let receiver = Box::new(receiver);
        Box::leak(receiver);

        let state = MyState {
            pool: pool.clone(),
            sender,
        };

        sqlx::migrate!("./migrations").run(&pool).await.unwrap();

        (app(state, false), pool)
    }

    async fn get_account(app: &mut RouterIntoService<Body>, username: Option<String>) -> AuthToken {
        let username = username.unwrap_or("test_user".to_string());
        let request = Request::builder()
            .method(Method::POST)
            .uri("/accounts/register")
            .header("Content-Type", "application/json")
            .body(Body::from(
                serde_json::to_vec(&RegisterOrLoginRequest {
                    username,
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
        duration: Duration,
        device: &Device,
        start: DateTimeUtc,
        end: DateTimeUtc,
    ) -> Task {
        let request = Request::builder()
            .method(Method::POST)
            .uri("/tasks/create")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::from(
                serde_json::to_vec(&CreateTaskRequest {
                    timespan: Timespan::new(start, end),
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
        name: String,
        effect: f64,
    ) -> Device {
        let request = Request::builder()
            .method(Method::POST)
            .uri("/devices/create")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token.clone())
            .body(Body::from(
                serde_json::to_vec(&CreateDeviceRequest { name, effect }).unwrap(),
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

    #[tokio::test]
    async fn register_account() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        get_account(&mut app, None).await;
    }

    #[tokio::test]
    async fn login_to_account() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        get_account(&mut app, None).await;

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
        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        let task = generate_task(
            &mut app,
            auth_token,
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;

        assert_ne!(task.id, (-1).into());
        assert_eq!(task.duration, Duration::hours(1).into());
    }

    #[tokio::test]
    async fn create_task_fails_with_invalid_device() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Register an account
        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

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
                    duration: 3600.into(),
                    device_id: 99999.into(),
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

        // Cannot register task to non-existant device.
        assert_eq!(response.status(), StatusCode::INTERNAL_SERVER_ERROR);

        let device = generate_device(&mut app, auth_token, "test".into(), 1234.0).await;

        // Register a new account
        let auth_token = get_account(&mut app, Some("test_user_2".to_string())).await;
        let auth_token = auth_token.to_string();

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
                    duration: 3600.into(),
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

        // Cannot register task to a device not owned by the account.
        assert_eq!(response.status(), StatusCode::INTERNAL_SERVER_ERROR);
    }

    #[tokio::test]
    async fn get_tasks_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;
        let all_tasks = get_tasks(&mut app, auth_token).await;

        assert_eq!(all_tasks.first().unwrap(), &task);
    }

    #[tokio::test]
    async fn delete_task_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;
        delete_task(&mut app, auth_token.clone(), task).await;

        let all_tasks = get_tasks(&mut app, auth_token).await;

        assert!(all_tasks.is_empty());
    }

    #[tokio::test]
    async fn get_devices_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let created_devices = vec![
            generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await,
            generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await,
        ];

        let all_devices = get_devices(&mut app, auth_token).await;

        assert_eq!(all_devices, created_devices);
    }

    #[tokio::test]
    async fn create_device_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        // Registers an account
        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;

        assert_eq!(device.effect, 1000.0);
    }

    #[tokio::test]
    async fn delete_device_test() {
        let (router, _) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        delete_device(&mut app, auth_token.clone(), device).await;

        let all_devices = get_devices(&mut app, auth_token).await;

        assert!(all_devices.is_empty());
    }

    #[tokio::test]
    async fn get_all_events() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;
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
    async fn get_all_valid_events() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 1000.0).await;
        let start = Utc::now() - Duration::hours(1);
        let end = Utc::now() + Duration::hours(2);
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            start,
            end,
        )
        .await;
        let event1 = _create_event(&pool, &task, Utc::now() - Duration::minutes(30))
            .await
            .unwrap();

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

        assert_eq!(response.events.first().unwrap(), &event1);
    }

    #[tokio::test]
    async fn get_device_event() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 20.0).await;
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;
        let event_start = Utc::now() + Duration::seconds(10);
        let event = _create_event(&pool, &task, event_start).await.unwrap();

        let request = Request::builder()
            .method(Method::GET)
            .uri("/events/get")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token)
            .body(Body::from(
                serde_json::to_vec(&GetDeviceEventRequest {
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
        let response: GetEventResponse = serde_json::from_slice(&body).unwrap();

        assert_eq!(response.event, event.into());
    }

    #[tokio::test]
    async fn get_device_event_none() {
        let (router, pool) = test_app().await;
        let mut app = router.into_service();

        let auth_token = get_account(&mut app, None).await;
        let auth_token = auth_token.to_string();

        let device = generate_device(&mut app, auth_token.clone(), "test".into(), 20.0).await;
        let task = generate_task(
            &mut app,
            auth_token.clone(),
            Duration::hours(1),
            &device,
            Utc::now(),
            Utc::now().checked_add_days(Days::new(1)).unwrap(),
        )
        .await;
        let event_start = Utc::now() - Duration::seconds(10);
        let _ = _create_event(&pool, &task, event_start).await.unwrap();

        let request = Request::builder()
            .method(Method::GET)
            .uri("/events/get")
            .header("Content-Type", "application/json")
            .header("X-Auth-Token", auth_token)
            .body(Body::from(
                serde_json::to_vec(&GetDeviceEventRequest {
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
        let response: GetEventResponse = serde_json::from_slice(&body).unwrap();

        assert!(response.event.is_none());
    }
}
