import 'package:flutter/services.dart';

class NativeChannelService {
  static const MethodChannel _channel = MethodChannel('native_channel');

  static Future<String> getAppCacheDirectory() async {
    var result = await _channel.invokeMethod('getCacheDir');
    return result;
  }

  static Future<bool> untilFileExists(String path) async {
    bool result = false;
    while (!result) {
      result = await _channel.invokeMethod('isFileExists', path);
    }

    return true;
  }
}