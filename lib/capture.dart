import 'package:image_gallery_saver/image_gallery_saver.dart';
import 'package:screenshot/screenshot.dart';
import 'dart:typed_data';

class CaptureController  {
  ScreenshotController screenshotController = ScreenshotController();

  void captureScreen() async {
    Uint8List? capturedImage = await screenshotController.capture();
    if (capturedImage != null) {
      final result = await ImageGallerySaver.saveImage(capturedImage);
      print("File saved to gallery: $result");

    }
  }

  controller() {
    return screenshotController;
  }
}