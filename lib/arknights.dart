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
              /* operatorMap:
              *        name : [String]       operator name
              *        tags : [List<String>] operator tags
              *        grade: [int]          grade
              *        [
              *          { "name": "라바", "tags": ["캐스터야"], "grade":3 }
              */
              List<Map<String, Object>> result = [];
              operators.forEach((it) {
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
