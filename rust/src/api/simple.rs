use crate::core::types::Tag;
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
