use http::{header::USER_AGENT, HeaderValue};
use hyper_util::{
    client::legacy::{connect::HttpConnector, Client},
    rt::TokioExecutor,
};
use tower::ServiceBuilder;
use tower_http::{
    classify::{SharedClassifier, StatusInRangeAsFailures},
    decompression::{Decompression, DecompressionLayer},
    set_header::{SetRequestHeader, SetRequestHeaderLayer},
    trace::{Trace, TraceLayer},
};

pub(crate) type HttpClient = Trace<
    SetRequestHeader<Decompression<Client<HttpConnector, String>>, HeaderValue>,
    SharedClassifier<StatusInRangeAsFailures>,
>;

#[allow(dead_code)]
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
