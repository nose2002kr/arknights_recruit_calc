import 'package:flutter/services.dart';
import 'package:yaml/yaml.dart';

class TranslationService {
  static const MethodChannel _channel = MethodChannel('translation');
  static Future<Map<String, dynamic>> _loadYamlAsset(String path) async {
    final yamlString = await rootBundle.loadString(path);
    final yamlMap = loadYaml(yamlString);

    // Convert the YamlMap to a standard Map<String, dynamic>
    return Map<String, dynamic>.from(yamlMap);
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
  }
}