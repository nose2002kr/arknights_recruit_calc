import 'package:arknights_calc/floating_view.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';

void main() {
  runApp(MainApp());
}

void redirect_recruit_calc_view() {
  runApp(RecruitCalcViewApp());
}

class MainApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print("build main app");

    return MaterialApp(
      title: 'Arknights recruitment calculator',
      theme: ThemeData(
        useMaterial3: true,
      ),
      initialRoute: '/',
      routes: {
        '/': (context) => HomePage(),
        '/floating_view': (context) => RecruitCalcView(),
      },
    );
  }
}

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    print("build homepage");

    return Scaffold(
      body: Center(
          child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('version:0.0.1b'),
            ElevatedButton(
                onPressed: () {
                  Navigator.of(context).pushNamed('/floating_view');
                  //ScreenCaptureService.stopScreenCapture();
                },
                child: Text('press')),
            IconButton(
                onPressed: (){
                  ScreenCaptureService.startProjectionRequest();
                  //Navigator.of(context).pushNamed('/floating_view');
                },
                icon: Image.asset('assets/sticker_9.gif', fit: BoxFit.cover),
                color: Colors.red
            ),
          ],
          ),
        )
    );
  }
}


/*

import 'package:flutter/material.dart';
import 'package:arknights_calc/src/rust/api/simple.dart';
import 'package:arknights_calc/src/rust/frb_generated.dart';

Future<void> main() async {
  await RustLib.init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('flutter_rust_bridge quickstart')),
        body: Center(
          child: Text(
              'Action: Call Rust `greet("Tom")`\nResult: `${greet(name: "Tom")}`'),
        ),
      ),
    );
  }
}
*/
