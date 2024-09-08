use std::collections::HashSet;

use crate::core::types::Tag;

pub static mut TAG_DICTIONARY: Option<HashSet<Tag>> = None;

pub fn list_all_tags() -> HashSet<Tag> {
    if unsafe { TAG_DICTIONARY.is_none() } {
        panic!("Must call 'make_operator_table' first");
    }
    return unsafe { TAG_DICTIONARY.clone().unwrap() };
}

pub fn find_advanced_special_recruitment() -> Tag {
    list_all_tags().iter().find(|tag| tag.name == "고급특별채용" || tag.name == "Top Operator").unwrap().clone()
}

#[allow(dead_code)]
pub fn find_special_recruitment() -> Tag {
    list_all_tags().iter().find(|tag| tag.name == "특별채용" || tag.name == "Senior Operator").unwrap().clone()
}

pub fn advanced_special_recruitment(kr: bool) -> Tag {
    if kr {
        Tag { name: "고급특별채용".to_string() }
    } else {
        Tag { name: "Top Operator".to_string() }
    }
}

pub fn special_recruitment(kr: bool) -> Tag {
    if kr {
        Tag { name: "특별채용".to_string() }
    } else {
        Tag { name: "Senior Operator".to_string() }
    }
}
