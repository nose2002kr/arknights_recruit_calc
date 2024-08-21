import 'package:arknights_calc/floating_view.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

void redirect_recruit_calc_view() {
  runApp(RecruitCalcViewApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static const platform = MethodChannel('com.example/navigation');

  @override
  void initState() {
    super.initState();
    print("init state main app");

    platform.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'navigate') {
        String route = call.arguments as String;
        print("accept navigate " + route);
        Navigator.of(context).pushNamed(route);
      }
    });
  }

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
        '/': (context) => MyHomePage(),
        '/floating_view': (context) => RecruitCalcViewApp(),
      },
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  ScreenCaptureService captureService = ScreenCaptureService();

  @override
  Widget build(BuildContext context) {
    print("build homepage");

    return Scaffold(
      body: Center(
          child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('version:0.0.1b'),
            ElevatedButton(onPressed: (){Navigator.of(context).pushNamed('/floating_view');}, child: Text('press')),
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
