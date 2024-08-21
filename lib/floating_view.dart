import 'package:flutter/material.dart';

class FloatingViewApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        initialRoute: '/',
        routes: {
          '/': (context) => FloatingView(),
        }
    );
  }
}

class FloatingView extends StatelessWidget {
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
