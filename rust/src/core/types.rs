use flutter_rust_bridge::frb;
use serde::{Serialize, Deserialize};

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct Operator {
    pub name: String,
    pub grade: i8,
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct Tag {
    pub name: String,
}


#[frb(mirror(Tag))]
impl Tag {
    pub fn new(name: String) -> Tag {
        Tag { name }
    }
}