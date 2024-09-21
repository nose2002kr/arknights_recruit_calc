import 'package:flutter/services.dart';
import 'package:arknights_calc/src/rust/api/simple.dart';

import 'config.dart';

class ArknightsService {
  static const MethodChannel _channel = MethodChannel('arknights');

  static Future<List<Map<String, Object>>> _lookupOperator(List<String> tags) async {
    var operators = await lookupOperatorByTags(tags: tags);
    /* operatorMap:
              *        name : [String]       operator name
              *        tags : [List<String>] operator tags
              *        grade: [int]          grade
              *        [
              *          { "name": "라바", "tags": ["캐스터야"], "grade":3 }
              *        ]
              */

    List<Map<String, Object>> result = [];
    bool hideLowOperators = Config().hideLowOperators ?? false;

    operators.forEach((it) {
      if (hideLowOperators && it.grade < 4) {
        return;
      }

      Map<String, Object> operator = {};
      operator["name"] = it.name;
      operator["grade"] = it.grade;
      List<String> tags = [];
      it.tag.forEach((tag) {
        tags.add(tag.name);
      });
      operator["tags"] = tags;
      result.add(operator);
    });

    return result;
  }

  static Future<void> listenToCall(String zipPath) async {
    _channel.setMethodCallHandler(
        (methodCall) async {
          switch (methodCall.method) {
            case "lookupOperator":
              List<String> tags = methodCall.arguments
                  .where((element) => element != null) // Remove null values
                  .map<String>((element) => element.toString()) // Convert each element to a string
                  .toList();
              return await _lookupOperator(tags);
            case "amiyaLayoutIsChanged":
              List<int> pos = List<int>.from(methodCall.arguments);
              print("received amiyaLayoutIsChanged: ${pos[0]}, ${pos[1]}");
              Config().iconXPos = pos[0];
              Config().iconYPos = pos[1];
              if (pos.length > 2) {
                print("received amiyaLayoutIsChanged extend: ${pos[2]}, ${pos[3]}");
                Config().panelWidth = pos[2];
                Config().panelHeight = pos[3];
              }
              Config.saveConfig();
              break;
            case "getAmiyaLayout":
              print("received getAmiyaLayout");
              return [Config().iconXPos, Config().iconYPos,
                      Config().panelWidth, Config().panelHeight];
          }
        }
    );
  }

  static Future<void> sendTagList() async {
    listTags().then((tags) {
      var list = tags.map((v) {
        return v.name;
      }).toList();
      _channel.invokeMethod('listTags', list);
    });
  }

  static Future<void> sendIcon() async {
    ByteData imageData = await rootBundle.load('assets/sticker-10_small.png');
    final Uint8List bytes = imageData.buffer.asUint8List();
    _channel.invokeMethod('icon', bytes);
  }
}
