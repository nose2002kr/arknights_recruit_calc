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

                    if grade == 6 {
                        let tag = Tag { name: "고급특별채용".to_string() };
                        operator_tags.push(tag); 
                    } else if grade == 5 {
                        let tag = Tag { name: "특별채용".to_string() };
                        operator_tags.push(tag); 
                    }
    
                    let operator = Operator {
                        name: operator_name.clone(),
                        grade: grade,
                        tag: operator_tags.clone()
                    };

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
            normalized_tags.iter().any(|tag| {
                tags.contains(tag)
            })
        )
        .map(|(_, operator)| operator.clone())
        .collect();

    matched_operator.sort_by_key(|operator| operator.grade);
    matched_operator.reverse();
    matched_operator
}

pub fn build_combinations_internal(tag_set: &Vec<Tag>, start: u8, combi: Vec<Tag>, counts: &mut Vec<usize>, result: &mut Vec<Vec<Tag>>) {
    let mut index: usize = 0;
    for i in (combi.len()..tag_set.len()).rev() {
        index += counts[i];
    }
    //println!("index:{}, tag.len:{}, result.len:{}", index, combi.len(), result.len());
    result.insert(index+1, combi.clone());
    counts[combi.len()] = counts[combi.len()]+1;

    for i in start..combi.len() as u8 {
        let mut new_combi = combi.clone();
        new_combi.remove(i.into());
        if new_combi.len() > 0 {
            build_combinations_internal(tag_set, i, new_combi, counts, result);
        }
    }
}

pub fn build_combinations(tag_set: &Vec<Tag>, start: u8, combi: Vec<Tag>) -> Vec<Vec<Tag>> {
    let mut result: Vec<Vec<Tag>> = Vec::new();
    let mut counts: Vec<usize> = vec![0; tag_set.len()];
    result.push(combi.clone());
    for i in start..combi.len() as u8 {
        let mut new_combi = combi.clone();
        new_combi.remove(i.into());
        if new_combi.len() > 0 {
            build_combinations_internal(tag_set, i, new_combi, &mut counts, &mut result);
        }
    }
    return result;
}

pub fn lookup_operator_reasonable(table: Table, tags : Vec<Tag>) -> Vec<Operator> {
    let normalized_tags: Vec<Tag> = tags.iter().filter(|tag| list_all_tags().contains(*tag)).cloned().collect();
    if normalized_tags.is_empty() {
        return Vec::new();
    }

    let tag_combinations: Vec<Vec<Tag>> = build_combinations(&normalized_tags, 0, normalized_tags.clone());
    // tag_combinations.iter().for_each(
    //     |vv| {
    //         print!("#Tags:");
    //         vv.iter().for_each(
    //             |v| {print!("{} ", v.name);}
    //         );
    //         println!("");
    //     }
    // );

    let mut matched_operator: Vec<Operator> = table.iter()
        .filter(|(tags, _)| 
            normalized_tags.iter().any(|tag| {
                tags.contains(tag)
            })
        )
        .map(|(_, operator)| operator.clone())
        .collect();

    matched_operator.sort_by_key(|operator| operator.grade);

    let mut summarized_operator: Vec<Operator> = Vec::new();
    let mut counts: Vec<usize> = vec![0;  7];

    for tag_combi in tag_combinations {
        // find all matched operator.
        // if matched operator grade is under 3, skip 4 to 6.
        let mut i = 0;
        let mut lowest_grade = 6;

        //print!("searching by {:?}, ", tag_combi);

        i = summarized_operator.len();
        while i > 0 {
            let oper = &summarized_operator[i - 1];
            if lowest_grade >= 3 && oper.grade > 3 && lowest_grade < oper.grade {
                break;
            }
            tag_combi.iter().all(|t| oper.tag.contains(t)).then(|| {
                lowest_grade = oper.grade;
                //print!("lowest grade renewed by {}({}), ", oper.name, oper.grade);
            });
            i -= 1;
        }
        
        i = 0;
        while i < matched_operator.len() {
            let oper = matched_operator[i].clone();
            if lowest_grade >= 3 && oper.grade > 3 && lowest_grade < oper.grade {
                break;
            }

            tag_combi.iter().all(|t| oper.tag.contains(t)).then(|| {
                let mut index = 0;
                for j  in (oper.grade as usize..counts.len()).rev() {
                    index += counts[j];
                }
                //print!("and be inserted {}, ",oper.name);
                summarized_operator.insert(index, oper.clone());
                matched_operator.remove(i);
                counts[oper.grade as usize] = counts[oper.grade as usize]+1;

                lowest_grade = oper.grade;
            }).or_else(|| {
                i += 1;
                Some(())
            });
        }
        //println!("");
    }
    
    summarized_operator
}
