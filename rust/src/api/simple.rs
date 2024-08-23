use crate::core::map_operator::lookup_operator;
use crate::core::types::{Operator, Tag};
use crate::core::list_tag::list_all_tags;

#[flutter_rust_bridge::frb(init)]
pub fn init_app() {
    // Default utilities - feel free to customize
    flutter_rust_bridge::setup_default_user_utils();
}

#[flutter_rust_bridge::frb(mirror(Vec<Tag>))]
pub fn list_tags() -> Vec<Tag> {
    list_all_tags()
}

#[flutter_rust_bridge::frb(mirror(Vec<Operator>))]
pub fn lookup_operator_by_tags(tags: Vec<Tag>) -> Vec<Operator> {
    lookup_operator(tags)
}
