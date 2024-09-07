use std::collections::HashSet;

use crate::core::map_operator::{lookup_operator_reasonable, make_operator_table};
use crate::core::types::{Operator, Tag};
use crate::core::list_tag::list_all_tags;

#[flutter_rust_bridge::frb(init)]
pub fn init_app() {
    // Default utilities - feel free to customize
    flutter_rust_bridge::setup_default_user_utils();
}

static mut TABLE: Option<Vec<Operator>> = None;

#[flutter_rust_bridge::frb(dart_async)]
pub fn install(zip_path: String) {
    unsafe {
        if TABLE.is_none() {
            TABLE = Some(make_operator_table(zip_path.as_str()).unwrap());
        }
    }
}

#[flutter_rust_bridge::frb(mirror(HashSet<Tag>))]
pub fn list_tags() -> HashSet<Tag> {
    unsafe {
        if TABLE.is_none() {
            panic!("Operator table is not initialized");
        }
    }
    list_all_tags()
}

#[flutter_rust_bridge::frb(mirror(Vec<Operator>))]
pub fn lookup_operator_by_tags(tags: Vec<String>) -> Vec<Operator> {
    // keep global memory and reuse it
    let table;
    unsafe {
        if TABLE.is_none() {
            panic!("Operator table is not initialized");
        }
        table = TABLE.as_ref().unwrap();
    }
    let _tags: Vec<Tag> = tags.iter().map(|tag| Tag { name: tag.clone() }).collect();
    lookup_operator_reasonable(table, _tags)
}
