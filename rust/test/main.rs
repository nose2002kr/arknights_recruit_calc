#[path="../src/api/map_operator.rs"]
mod seq1;

#[path="../src/api/list_tag.rs"]
mod seq2;

pub fn main() -> Result<(), Box<dyn std::error::Error>> {
    let table = seq1::make_operator_table()?;

    // Print the parsed operators and their tags
    for element in &table {
        println!("Operator: {} {}★", element.1.name, element.1.grade);
        for tag in element.0 {
            println!("  Tag: {}", tag.name);
        }
    }


    let tags = seq2::list_all_tags();
    print!("Tag: ");
    for tag in tags {
        print!("{}, ", tag.name);
    }
    println!("");

    println!("Done");

    Ok(())
}

