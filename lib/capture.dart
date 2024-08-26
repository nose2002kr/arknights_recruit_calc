import 'package:flutter/services.dart';

class ScreenCaptureService {
  static const MethodChannel _channel = MethodChannel('screen_capture');

  static Future<void> stopScreenCapture() async {
    await _channel.invokeMethod('stopScreenCapture');
  }

  static Future<void> startProjectionRequest() async {
    ByteData imageData = await rootBundle.load('assets/sticker-10_small.png');
    final Uint8List bytes = imageData.buffer.asUint8List();
    await _channel.invokeMethod('startProjectionRequest', bytes);
  }
}
