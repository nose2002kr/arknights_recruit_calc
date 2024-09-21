import 'package:flutter/services.dart';

class ScreenCaptureService {
  static const MethodChannel _channel = MethodChannel('screen_capture');

  static Future<void> stopScreenCapture() async {
    await _channel.invokeMethod('stopScreenCapture');
  }

  static Future<void> startProjectionRequest() async {
    await _channel.invokeMethod('startProjectionRequest');
  }
}
