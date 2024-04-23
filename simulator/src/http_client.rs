use http::HeaderValue;
use hyper_util::client::legacy::{connect::HttpConnector, Client};
use tower_http::{
    classify::{SharedClassifier, StatusInRangeAsFailures},
    decompression::Decompression,
    set_header::SetRequestHeader,
    trace::Trace,
};

pub(crate) type HttpClient = Trace<
    SetRequestHeader<Decompression<Client<HttpConnector, String>>, HeaderValue>,
    SharedClassifier<StatusInRangeAsFailures>,
>;
