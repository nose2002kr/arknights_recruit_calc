use std::collections::HashSet;

use crate::core::map_operator::{lookup_operator_reasonable, make_operator_table};
use crate::core::types::{Operator, Tag};
use crate::core::list_tag::list_all_tags;

#[flutter_rust_bridge::frb(init)]
pub fn init_app() {
    // Default utilities - feel free to customize
    flutter_rust_bridge::setup_default_user_utils();
}

#[flutter_rust_bridge::frb(mirror(HashSet<Tag>))]
pub fn list_tags() -> HashSet<Tag> {
    list_all_tags()
}

#[flutter_rust_bridge::frb(mirror(Vec<Operator>))]
pub fn lookup_operator_by_tags(zip_path: String, tags: Vec<String>) -> Vec<Operator> {
    // keep global memory and reuse it
    let table;
    static mut TABLE: Option<Vec<Operator>> = None;
    unsafe {
        if TABLE.is_none() {
            TABLE = Some(make_operator_table(zip_path.as_str()).unwrap());
        }
        table = TABLE.as_ref().unwrap();
    }
    
    let _tags: Vec<Tag> = tags.iter().map(|tag| Tag { name: tag.clone() }).collect();
    lookup_operator_reasonable(table, _tags)
}
