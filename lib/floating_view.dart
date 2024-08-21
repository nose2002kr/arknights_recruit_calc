import 'package:flutter/material.dart';

class RecruitCalcViewApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print("build recruit app");

    return MaterialApp(
        initialRoute: '/',
        routes: {
          '/': (context) => RecruitCalcView(),
        }
    );
  }
}

class RecruitCalcView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      color: Colors.blue,
        child: SingleChildScrollView(
          child:Center(
            child:Text(
              'Hello!',
              style: TextStyle(fontSize: 24, color: Colors.white),
            ),
          )
        )
    );
  }
}
