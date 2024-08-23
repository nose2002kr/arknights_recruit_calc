use std::{collections::HashMap, fs::File, io::{BufReader, Read}};

use scraper::{Html, Selector};
use zip::read::ZipArchive;

use crate::core::types::{Operator, Tag};

use super::list_tag::list_all_tags;

type Table = HashMap<Vec<Tag>, Operator>;

pub fn make_operator_table(zip_path : &str) -> Result<Table, Box<dyn std::error::Error>> {
    let mut zip = ZipArchive::new(BufReader::new(File::open(zip_path)?))?;

    let mut table: Table =  HashMap::new();

    let file_names: Vec<String> = zip.file_names().map(|name| name.to_string()).collect();

    for file_name in file_names {
        let grade: i8 = 
            match file_name.as_str() {
                "6성.html" => 6,
                "5성.html" => 5,
                "4성.html" => 4,
                "3성.html" => 3,
                "2성.html" => 2,
                "1성.html" => 1,
                _ => 0,
            };

        let mut html = String::new();
        let _ = zip.by_name(file_name.as_str()).unwrap().read_to_string(&mut html);
        let document = Html::parse_document(&html);

        // Selectors for the <tbody> and <tr> elements
        let tbody_selector = Selector::parse("tbody").unwrap();
        let tr_selector = Selector::parse("tr").unwrap();
        let td_selector = Selector::parse("td").unwrap();
    
        // Iterate over the rows of the table
        for tbody in document.select(&tbody_selector) {
            for tr in tbody.select(&tr_selector) {
                let mut td_iter = tr.select(&td_selector);
    
                // The first column (td) is the operator name
                if let Some(name_td) = td_iter.next() {
                    let operator_name = name_td.text().collect::<Vec<_>>().concat();
                    let operator = Operator {
                        name: operator_name.clone(),
                        grade: grade,
                    };
    
                    // Create a vector to hold the tags for this operator
                    let mut operator_tags: Vec<Tag> = Vec::new();
    
                    // The remaining columns (tds) are the tags
                    for tag_td in td_iter {
                        let tag_name = tag_td.text().collect::<Vec<_>>().concat();
                        if tag_name.contains(",") {
                            let tag_names: Vec<&str> = tag_name.split(",").collect();
                            for tag_name in tag_names {
                                let tag = Tag { name: tag_name.trim().to_string() };
                                operator_tags.push(tag);
                            }
                            continue;
                        }
                        let tag = Tag { name: tag_name };
                        operator_tags.push(tag);
                    }
    
                    // Add the operator to the list of operators
                    table.insert(operator_tags, operator);
                }
            }
        }
    };
    
    return Ok(table);
}

pub fn lookup_operator(table: Table, tags : Vec<Tag>) -> Vec<Operator> {
    let normalized_tags: Vec<Tag> = tags.iter().filter(|tag| list_all_tags().contains(*tag)).cloned().collect();
    if normalized_tags.is_empty() {
        return Vec::new();
    }
    
    let mut matched_operator: Vec<Operator> = table.iter()
        .filter(|(tags, _)| 
            normalized_tags.iter().all(|tag| {
                tags.contains(tag)
            })
        )
        .map(|(_, operator)| operator.clone())
        .collect();

    matched_operator.sort_by_key(|operator| operator.grade);
    matched_operator.reverse();
    matched_operator
}
