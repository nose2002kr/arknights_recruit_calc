import 'package:flutter/services.dart';
import 'package:arknights_calc/src/rust/api/simple.dart';

class ArknightsService {
  static const MethodChannel _channel = MethodChannel('arknights');

  ArknightsService(zipPath) {
    _channel.setMethodCallHandler(
        (methodCall) async {
          switch (methodCall.method) {
            case "lookupOperator":
              List<String> tags = methodCall.arguments
                  .where((element) => element != null) // Remove null values
                  .map<String>((element) => element.toString()) // Convert each element to a string
                  .toList();

              var operators = await lookupOperatorByTags(zipPath: zipPath, tags: tags);
              // grade   oper-name    oper-tags
              //   |         |           |
              Map<int, Map<String, List<String>>> operatorMap = {};
              operators.forEach((it) {
                if (operatorMap[it.grade] == null) {
                  operatorMap[it.grade] = {};
                }
                Map<String, List<String>> operator = operatorMap[it.grade]!;
                List<String> tags = [];
                it.tag.forEach((tag) {
                  tags.add(tag.name);
                });
                operator[it.name] = tags;

                operatorMap[it.grade] = operator;
              });

              return operatorMap;
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

  static Future<String> getAppCacheDirectory() async {
    var result = await _channel.invokeMethod('getCacheDir');
    print(result);
    return result;
  }
}
