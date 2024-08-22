#[path="../src/api/map_operator.rs"]
mod map;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let table = map::make_operator_table()?;

    // Print the parsed operators and their tags
    for element in &table {
        println!("Operator: {} {}★", element.1.name, element.1.grade);
        for tag in element.0 {
            println!("  Tag: {}", tag.name);
        }
    }
    println!("Done");

    Ok(())
}

