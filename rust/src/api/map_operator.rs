use std::{collections::HashMap, io::{Cursor, Read}};

use reqwest::blocking::Client;
use scraper::{Html, Selector};
use zip::read::ZipArchive;

mod types;
use types::{Operator, Tag};

pub fn make_operator_table() -> Result<HashMap<Vec<Tag>, Operator>, Box<dyn std::error::Error>> {
    // Send the HTTP GET request
    let url = "https://docs.google.com/spreadsheets/d/1bEbqM1mo0FFttwlw9_hOBdnzeLZhCVQJ83oR8LOYyTs/export?format=zip";
    let client = Client::new();
    let response = client.get(url).send()?.bytes()?;
    let mut zip = ZipArchive::new(Cursor::new(response))?;

    let mut table: HashMap<Vec<Tag>, Operator> =  HashMap::new();

    let file_names: Vec<String> = zip.file_names().map(|name| name.to_string()).collect();

    for file_name in file_names {
        println!("{}", file_name);
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
