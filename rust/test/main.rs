#[path="../src/core/mod.rs"]
mod core;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let table = core::map_operator::make_operator_table()?;

    // Print the parsed operators and their tags
    for element in &table {
        println!("Operator: {} {}★", element.1.name, element.1.grade);
        for tag in element.0 {
            println!("  Tag: {}", tag.name);
        }
    }


    let tags = core::list_tag::list_all_tags();
    print!("Tag: ");
    for tag in tags {
        print!("{}, ", tag.name);
    }
    println!("");

    println!("Done");

    Ok(())
}

