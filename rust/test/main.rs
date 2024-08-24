#[path="../src/core/mod.rs"]
mod core;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let dir = "/path/for/test/".to_string();
    let table = core::map_operator::make_operator_table(&(dir + "datasheets.zip"))?;

    // Print the parsed operators and their tags
    for element in &table {
        println!("Operator: {} {}★", element.1.name, element.1.grade);
        for tag in element.0 {
            println!("  Tag: '{}'", tag.name);
        }
    }


    let tags = core::list_tag::list_all_tags();
    print!("Tag: ");
    for tag in tags {
        print!("{}, ", tag.name);
    }
    println!("");

    let presented_tags = vec![
        core::types::Tag { name: "폭발".to_string() },
        core::types::Tag { name: "근거리".to_string() },
        core::types::Tag { name: "뱅가드".to_string() },
        //core::types::Tag { name: "신입".to_string() },
    ];
    let operators = core::map_operator::lookup_operator(table, presented_tags.clone());
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

