import 'package:flutter/services.dart';
import 'package:arknights_calc/src/rust/api/simple.dart';

class ArknightsService {
  static const MethodChannel _channel = MethodChannel('arknights');

  static Future<void> sendTagList() async {
    listTags().then((tags) {
      var list = tags.map((v) {
        return v.name;
      }).toList();
      _channel.invokeMethod('listTags', list);
    });
  }
}
