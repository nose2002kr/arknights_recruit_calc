import 'package:arknights_calc/ad_helper.dart';
import 'package:arknights_calc/floating_view.dart';
import 'package:arknights_calc/arknights.dart';
import 'package:arknights_calc/native_channel.dart';
import 'package:arknights_calc/src/rust/frb_generated.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

// For flutter view. not using as implement ui in native code for now.
/*void redirect_recruit_calc_view() {
  runApp(RecruitCalcViewApp());
}*/

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
  var zipPath = await downloadFileToCache(
      "https://docs.google.com/spreadsheets/d/1bEbqM1mo0FFttwlw9_hOBdnzeLZhCVQJ83oR8LOYyTs/export?format=zip",
      await NativeChannelService.getAppCacheDirectory() + "/datasheets.zip");
  ArknightsService.listenToCallLookupOperator(zipPath);
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
  bool datasheetIsReady = false;

  @override 
  void initState() {
    super.initState();

    BannerAd(
      adUnitId: AdHelper.bannerAdUnitId,
      size: AdSize.banner,
      request: const AdRequest(),
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

    NativeChannelService.getAppCacheDirectory().then((dir) {
      NativeChannelService.untilFileExists(dir + "/datasheets.zip").then(
        (exists) {
          if (exists) {
            setState(() {
              datasheetIsReady = true;
            });
          }
        }
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    print("build homepage");

    return Scaffold(
      body: Column(
        children: [
          Container(
            height: 72,
            width: double.infinity,
            color: Colors.blueAccent,
            alignment: Alignment.center,
            child:
              (_ad != null) ?
                 Container(
                   width: _ad!.size.width.toDouble(),
                   height: 60,
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
                  HoldableIconButton(
                      normalStateIcon: Image.asset('assets/sticker-10.png', fit: BoxFit.cover),
                      holdingStateIcon: Image.asset('assets/sticker-07-1.png', fit: BoxFit.cover),
                  ),
                  datasheetIsReady ? Text('Ready to work!') : Text(''),
                ],
              )
            ),
          )
        ],
      )
    );
  }
}

class HoldableIconButton extends StatefulWidget {

  Image normalStateIcon;
  Image holdingStateIcon;

  HoldableIconButton(
      {
        super.key,
        required this.normalStateIcon,
        required this.holdingStateIcon
      });

  @override
  _HoldableIconButton createState() =>  _HoldableIconButton();
}

class _HoldableIconButton extends State<HoldableIconButton> {
  bool isHolding = false;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (e){setState(() {
        isHolding = true;
      });},
      onTapCancel: (){setState(() {
        isHolding = false;
      });},
      onTapUp: (e){setState(() {
        isHolding = false;
      });},
      child: IconButton(
          onPressed: (){
            ScreenCaptureService.startProjectionRequest();
          },
          icon: isHolding ? widget.holdingStateIcon : widget.normalStateIcon,

          color: Colors.red
      ),
    );
  }
}
