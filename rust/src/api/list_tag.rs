mod types;
use types::Tag;

pub fn list_all_tags() -> Vec<Tag> {
    let tag_dictionary: Vec<Tag> = Vec::from(
        ["신입", "특별채용", "고급특별채용",
        "근거리", "원거리",
        "가드", "디펜더", "메딕", "뱅가드", "서포터", "스나이퍼", "스페셜리스트", "캐스터",
        "감속", "강제이동", "누커", "디버프", "딜러", "로봇", "방어형", "범위공격", "생존형", "소환", "제어형", "지원", "코스트+", "쾌속부활", "힐링"])
        .iter()
        .map(|x| Tag { name: x.to_string() })
        .collect();

    tag_dictionary
}
