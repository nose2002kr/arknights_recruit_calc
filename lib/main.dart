import 'package:arknights_calc/ad_helper.dart';
import 'package:arknights_calc/floating_view.dart';
import 'package:arknights_calc/arknights.dart';
import 'package:arknights_calc/native_channel.dart';
import 'package:arknights_calc/src/rust/api/simple.dart';
import 'package:arknights_calc/src/rust/frb_generated.dart';
import 'package:arknights_calc/translation.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:arknights_calc/capture.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'dart:io';

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

  final String defaultLocale = Platform.localeName; // Returns locale string in the form 'en_US'
  print('Current locale: ${defaultLocale}');

  String zipUrl;
  if (defaultLocale == "ko_KR") {
    zipUrl =
    "https://docs.google.com/spreadsheets/d/1RW1hc7P_EuskKgL8OndhlgmZX-sYcsuha_tuSCJ59VY/export?format=zip";
  } else {
    zipUrl =
    "https://docs.google.com/spreadsheets/d/1xpoQFVunGD4MHxaFj4A8MVqAu59OB2SXAzRLs3mWA38/export?format=zip";
  }

  String zipPath = await downloadFileToCache(
      zipUrl,
      await NativeChannelService.getAppCacheDirectory() + "/datasheets.zip");
  install(zipPath: zipPath).then((_) => ArknightsService.sendTagList());
  ArknightsService.listenToCallLookupOperator(zipPath);
  TranslationService.loadTranslatedMessage(defaultLocale).then((_) {
      TranslationService.installTranslation();
  });

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
  bool translationIsInstalled = false;

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

    TranslationService.untilInstalled().then(
            (installed) => setState(() {
              translationIsInstalled = installed;
            }));

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
                  Text('version:0.0.6'),
                  HoldableIconButton(
                      normalStateIcon: Image.asset('assets/sticker-10.png', fit: BoxFit.cover),
                      holdingStateIcon: Image.asset('assets/sticker-07-1.png', fit: BoxFit.cover),
                      onPressed: () {
                        if (datasheetIsReady && translationIsInstalled)
                          ScreenCaptureService.startProjectionRequest();
                      },
                  ),
                  datasheetIsReady && translationIsInstalled ? Text('Ready to work!') : Text(''),
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
  dynamic onPressed;

  HoldableIconButton(
      {
        super.key,
        required this.normalStateIcon,
        required this.holdingStateIcon,
        required void Function() this.onPressed
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
          onPressed: widget.onPressed,
          icon: isHolding ? widget.holdingStateIcon : widget.normalStateIcon,

          color: Colors.red
      ),
    );
  }
}
