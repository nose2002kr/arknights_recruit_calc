import 'dart:io';

import 'package:flutter/foundation.dart';

class AdHelper {

  static String get bannerAdUnitId {
    if (Platform.isAndroid) {
      if (kReleaseMode) {
        return 'ca-app-pub-4133634045586474/3250054959';
      } else {
        return 'ca-app-pub-3940256099942544/6300978111';
      }
    } else if (Platform.isIOS) {
      return 'ca-app-pub-3940256099942544/2934735716';
    } else {
      throw new UnsupportedError('Unsupported platform');
    }
  }
}