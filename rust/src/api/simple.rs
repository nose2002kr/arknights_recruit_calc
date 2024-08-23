use crate::core::types::Tag;
use crate::core::list_tag::list_all_tags;

#[flutter_rust_bridge::frb(sync)] // Synchronous mode for simplicity of the demo
pub fn greet(name: String) -> String {
    
    //format!("Hello, {name}!")

    let a = Tag {
        name: name.to_string(),
    };
    'a'.to_string()
}

#[flutter_rust_bridge::frb(init)]
pub fn init_app() {
    // Default utilities - feel free to customize
    flutter_rust_bridge::setup_default_user_utils();
}


#[flutter_rust_bridge::frb(mirror(Vec<Tag>))] // Synchronous mode for simplicity of the demo
pub fn newnewnewnewnewnew() -> Vec<Tag> {
    let a = list_all_tags();
    a
}
