
import 'dart:convert';
import 'dart:io';

import 'package:arknights_calc/native_channel.dart';

class Config {
  String? locale;

  int? iconXPos;
  int? iconYPos;

  int? panelWidth;
  int? panelHeight;

  bool? hideLowOperators;

  Map<String, dynamic> toJson() => {
    'locale': locale,
    'iconXPos': iconXPos,
    'iconYPos': iconYPos,
    'panelWidth': panelWidth,
    'panelHeight': panelHeight,
    'hideLowOperators': hideLowOperators,
  };

  void fromJson(Map<String, dynamic> json) {
    locale = json['locale'];
    iconXPos = json['iconXPos'];
    iconYPos = json['iconYPos'];
    panelWidth = json['panelWidth'];
    panelHeight = json['panelHeight'];
    hideLowOperators = json['hideLowOperators'];
  }

  static Future<void> saveConfig() async {
    var filePath = "${await NativeChannelService.getAppCacheDirectory()}/config.json";
    final jsonString = jsonEncode(Config().toJson());
    final file = File(filePath);
    await file.writeAsString(jsonString);
  }

  static Future<void> loadConfig() async {
    var filePath = "${await NativeChannelService.getAppCacheDirectory()}/config.json";
    final file = File(filePath);
    if (!await file.exists()) {
      return;
    }

    final jsonString = await file.readAsString();
    final jsonMap = jsonDecode(jsonString);
    Config().fromJson(jsonMap);
  }

  static final Config _instance = Config._internal();

  Config._internal();

  factory Config() {
    return _instance;
  }
}