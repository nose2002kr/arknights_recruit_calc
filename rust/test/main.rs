#[path="../src/core/mod.rs"]
mod core;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let dir = "/path/for/test/".to_string();
    let table = core::map_operator::make_operator_table(&(dir + "datasheets.zip"))?;

    // Print the parsed operators and their tags
    let mut counts: Vec<usize> = vec![0;  7];
    for element in &table {
        counts[element.grade as usize] += 1;
        if element.grade == 2 {
            println!("Operator: {} {}★", element.name, element.grade);
            for tag in element.tag.iter() {
                println!("  Tag: '{}'", tag.name);
            }
        }
    }

    // 2024.08.25
    assert!(counts[0] == 0);
    assert!(counts[1] == 5);
    assert!(counts[2] == 5);
    assert!(counts[3] == 16);
    assert!(counts[4] == 31);
    assert!(counts[5] == 37);
    assert!(counts[6] == 23);

    let tags = core::list_tag::list_all_tags();
    print!("Tag: ");
    for tag in tags.iter() {
        print!("{}, ", tag.name);
    }
    println!("");
    assert!(tags.len() == 28);

    let presented_tags = vec![
        core::types::Tag { name: "스나이퍼".to_string() },
        core::types::Tag { name: "뱅가드".to_string() },
        core::types::Tag { name: "코스트+".to_string() },
        core::types::Tag { name: "범위공격".to_string() },
        core::types::Tag { name: "힐링".to_string() },
    ];
    
    let operators = core::map_operator::lookup_operator_reasonable(table.as_ref(), presented_tags.clone());
    operators.iter().all(|oper| {
        print!("Operator: {} {}★ \t", oper.name, oper.grade);
        // print the tag with the green text color
        oper.tag.iter().for_each(|tag| {
            if presented_tags.contains(tag) {
                print!("\x1b[91m{}\x1b[0m ", tag.name);
            } else {
                print!("{} ", tag.name);
            }
        });

        println!("");
        true
    });

    println!("Done");

    Ok(())
}

