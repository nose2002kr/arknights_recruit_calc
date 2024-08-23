import 'package:arknights_calc/floating_view.dart';
import 'package:arknights_calc/arknights.dart';
import 'package:arknights_calc/src/rust/frb_generated.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';

Future<void> main() async {
  await RustLib.init();
  ArknightsService.sendTagList();

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
            Text('version:0.0.1d'),
            ElevatedButton(
                onPressed: () {
                  print('pressed');
                  //Navigator.of(context).pushNamed('/floating_view');
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
