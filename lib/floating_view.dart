import 'package:flutter/material.dart';

class RecruitCalcViewApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print("build recruit app");

    return MaterialApp(
        initialRoute: '/',
        routes: {
          '/': (context) => RecruitCalcView(),
        });
  }
}

class RecruitCalcView extends StatelessWidget {
  List<String> tagList = ['신입', '특별채용', '고급특별채용',
    '근거리', '원거리',
    '가드', '디펜더', '메딕', '뱅가드', '서포터', '스나이퍼', '스페셜리스트', '캐스터',
    '감속', '강제이동', '누커', '디버프', '딜러', '로봇', '방어형', '범위공격', '생존형', '소환', '제어형', '지원', '코스트+', '쾌속부활', '힐링'];
  late Iterable<ToggleTextButton> buttons;
  List<String> getEnabledTag() {
    return buttons.where((b) => b.isPressed).map((b) => b.text).toList();
  }

  @override
  Widget build(BuildContext context) {
    buttons = tagList.map((text) => ToggleTextButton(text: text));

    return Container(
      color: Colors.white10,
      child: SingleChildScrollView(
        child: Wrap(
          spacing: 3, // Space between buttons horizontally
          children: buttons.toList(),
        ),
      )
    );
  }
}

class ToggleTextButton extends StatefulWidget {
  final String text;
  bool isPressed;

  @override
  _ToggleTextButtonState createState() => _ToggleTextButtonState();

  ToggleTextButton({
    super.key,
    required this.text,
    this.isPressed = false,
  });
}

class _ToggleTextButtonState extends State<ToggleTextButton> {
  void toggle() {
    setState(() {
      widget.isPressed = !widget.isPressed;
    });
  }

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      style: ElevatedButton.styleFrom(
        foregroundColor: widget.isPressed ? Colors.black : Colors.black54,
        backgroundColor: widget.isPressed ? Colors.white : Colors.white30,
      ),
      onPressed: toggle,
      child: Text(widget.text),
    );
  }
}
