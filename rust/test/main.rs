use core::map_operator::lookup_operator;

#[path="../src/core/mod.rs"]
mod core;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let table = core::map_operator::make_operator_table()?;

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

    let operators = lookup_operator(
        vec![
            core::types::Tag { name: "폭발".to_string() },
            core::types::Tag { name: "근거리".to_string() },
            core::types::Tag { name: "뱅가드".to_string() },
            //core::types::Tag { name: "신입".to_string() },
        ]);
    operators.iter().all(|oper| {
        println!("Operator: {} {}★", oper.name, oper.grade);
        true
    });

    println!("Done");

    Ok(())
}

