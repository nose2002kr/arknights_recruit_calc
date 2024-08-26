import 'package:arknights_calc/ad_helper.dart';
import 'package:arknights_calc/floating_view.dart';
import 'package:arknights_calc/arknights.dart';
import 'package:arknights_calc/src/rust/frb_generated.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';


Future<String> downloadFileToCache(String url, String savePath) async {
  try {
    // Create Dio instance
    Dio dio = Dio();

    // Download the file and save it to the cache directory
    await dio.download(
      url,
      savePath,
      onReceiveProgress: (received, total) {
        if (total != -1) {
          print("Progress: ${(received / total * 100).toStringAsFixed(0)}%");
        }
      },
    );

    print("File saved to $savePath");
    return savePath;
  } catch (e) {
    print("Error downloading file: $e");
    return "";
  }
}

Future<void> main() async {
  await RustLib.init();

  runApp(MainApp());

  ArknightsService.sendTagList();
  ArknightsService.getAppCacheDirectory().then((path) async {
    var zipPath = await downloadFileToCache("https://docs.google.com/spreadsheets/d/1bEbqM1mo0FFttwlw9_hOBdnzeLZhCVQJ83oR8LOYyTs/export?format=zip",
        '${path}/datasheets.zip');
    ArknightsService(zipPath);
  });
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

class HomePage extends StatefulWidget {
  @override
  _HomePage createState() => _HomePage();
}


class _HomePage extends State<HomePage> {

  BannerAd? _ad;

  @override 
  void initState() {
    super.initState();

    BannerAd(
      adUnitId: AdHelper.bannerAdUnitId,
      size: AdSize.banner,
      request: AdRequest(),
      listener: BannerAdListener(
        onAdLoaded: (ad) {
          setState(() {
            _ad = ad as BannerAd;
          });
        },
        onAdFailedToLoad: (ad, error) {
          // Releases an ad resource when it fails to load
          ad.dispose();
        },
      ),
    ).load();
  }

  @override
  Widget build(BuildContext context) {
    print("build homepage");

    return Scaffold(
      body: Column(
        children: [
          Container(
            height: 60,  // 바의 높이
            width: double.infinity,
            color: Colors.blueAccent,
            alignment: Alignment.center,
            child:
              (_ad != null) ?
                 Container(
                   width: _ad!.size.width.toDouble(),
                   height: 72.0,
                   alignment: Alignment.center,
                   child: AdWidget(ad: _ad!),
                 )
               :
                Text("")
          ),
          Expanded(

            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text('version:0.0.2a'),
                  IconButton(
                      onPressed: (){
                        ScreenCaptureService.startProjectionRequest();
                      },
                      icon: Image.asset('assets/sticker-10.png', fit: BoxFit.cover),
                      color: Colors.red
                  ),

                ],
              )
            ),
          )
        ],
      )
    );
  }
}
