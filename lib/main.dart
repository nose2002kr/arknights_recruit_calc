import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';
import 'package:screenshot/screenshot.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Arknights recruitment calculator',
      theme: ThemeData(
        useMaterial3: true,
      ),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  CaptureController captureController = CaptureController();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Screenshot(
          controller: captureController.controller(),
          child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            IconButton(
                onPressed: captureController.captureScreen,
                icon: Image.asset('assets/sticker_9.gif', fit: BoxFit.cover),
                color: Colors.red
            ),
          ],
          ),
        )
      ),
    );
  }
}
