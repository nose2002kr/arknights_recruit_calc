import 'package:flutter/services.dart';
import 'package:yaml/yaml.dart';

class TranslationService {
  static const MethodChannel _channel = MethodChannel('translation');
  static bool _isInstalled = false;
  static Future<Map<String, dynamic>> _loadYamlAsset(String path) async {
    final yamlString = await rootBundle.loadString(path);
    final yamlMap = loadYaml(yamlString);

    // Convert the YamlMap to a standard Map<String, dynamic>
    return Map<String, dynamic>.from(yamlMap).map((key, value) {
      value = value.toString().replaceAll("\\n", "\n").trim();
      return MapEntry(key,value.toString());
    });
  }

  static var _translatedMessage = {};
  static Future<void> loadTranslatedMessage(String langSet) async {
    switch (langSet) {
      case "ko_KR": _translatedMessage = await _loadYamlAsset("assets/messages/ko_KR.yml");
      default: _translatedMessage = await _loadYamlAsset("assets/messages/default.yml");
    }
  }

  static Future<void> installTranslation() async {
    await _channel.invokeMethod("installTranslation", _translatedMessage);
    _isInstalled = true;
  }

  static Future<bool> untilInstalled() async {
    while (!_isInstalled) {
      await Future.delayed(Duration(milliseconds: 100));
    }
    return _isInstalled;
  }
}