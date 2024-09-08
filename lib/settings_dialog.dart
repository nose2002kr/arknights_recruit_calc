
import 'package:arknights_calc/config.dart';
import 'package:arknights_calc/translation.dart';
import 'package:flutter/material.dart';

class Settings extends Dialog {
  BuildContext context;
  SmallDropdownButton languageDropDown = SmallDropdownButton();
  SwitchButton hideLowOperatorsSwitch = SwitchButton();

  Settings(
      {super.key, required this.context}
      );

  Map<String, Object> makeDialogResult() {
    return {
      'locale': languageDropDown.selectedValue,
      'hideLowOperators': hideLowOperatorsSwitch.value
    };
  }

  @override
  Widget? get child {
    return PopScope(
        canPop: false,
        onPopInvokedWithResult: (bool didPop, result) {
          if (didPop) {
            return;
          }
          Navigator.pop(context, makeDialogResult());
        },
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(Tr('SETTINGS'), style: const TextStyle(fontSize: 24)),
            Table(
              defaultVerticalAlignment: TableCellVerticalAlignment.middle,
              columnWidths: const <int, TableColumnWidth>{
                0: IntrinsicColumnWidth(),
                1: FixedColumnWidth(10),
                2: IntrinsicColumnWidth(),
              },
              children: [
                TableRow(
                    children: [
                      const Text('Language:', textAlign: TextAlign.right),
                      const Spacer(),
                      languageDropDown
                    ]
                ),
                TableRow(
                    children: [
                      Text('${Tr('HIDE_LOW_GRADE_OPERATOR')}:', textAlign: TextAlign.right), // need to translate
                      Spacer(),
                      hideLowOperatorsSwitch
                    ]
                )
              ],
            ),
            TextButton(
              onPressed: () {
                Navigator.pop(context, makeDialogResult());
              },
              child: Text(Tr('CLOSE')),  // need to translate
            ),
          ],
        ),
      )
    );
  }
}


class SwitchButton extends StatefulWidget {
  bool value = Config().hideLowOperators?? false;

  @override
  State<SwitchButton> createState() => _SwitchButtonState();
}

class _SwitchButtonState extends State<SwitchButton> {

  @override
  Widget build(BuildContext context) {
    final WidgetStateProperty<Color?> trackColor =
    WidgetStateProperty.resolveWith<Color?>(
          (Set<WidgetState> states) {
        if (states.contains(WidgetState.selected)) {
          return const Color(0xFFE8F5FF);
        }
        return null;
      },
    );
    final WidgetStateProperty<Color?> trackOutlineColor =
    WidgetStateProperty.resolveWith<Color?>(
          (Set<WidgetState> states) {
        if (states.contains(WidgetState.selected)) {
          return const Color(0xFF6E9FED);
        }
        return null;
      },
    );
    final WidgetStateProperty<Color?> overlayColor =
    WidgetStateProperty.resolveWith<Color?>(
          (Set<WidgetState> states) {
        if (states.contains(WidgetState.selected)) {
          return const Color(0xFFE8F5FF).withOpacity(0.54);
        }
        if (states.contains(WidgetState.disabled)) {
          return Colors.grey.shade400;
        }
        return null;
      },
    );

    return Switch(
      value: widget.value,
      overlayColor: overlayColor,
      trackColor: trackColor,
      trackOutlineColor: trackOutlineColor,
      thumbColor: WidgetStatePropertyAll<Color>(Colors.black87),
      onChanged: (bool value) {
        setState(() {
          widget.value = value;
        });
      },
    );
  }
}

const List<String> list = <String>['en_US', 'ko_KR'];
const Map<String, String> valueMap = {'en_US':'English', 'ko_KR':'한국어'};

class SmallDropdownButton extends StatefulWidget {
  //String selectedValue = Config().locale != null ? valueMap[Config().locale]! : list[0];
  String selectedValue = Config().locale ?? list[0];

  @override
  _SmallDropdownButtonState createState() => _SmallDropdownButtonState();
}

class _SmallDropdownButtonState extends State<SmallDropdownButton> {

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 90, // Set width to make it smaller
      height: 40, // Set height to make it smaller
      padding: EdgeInsets.symmetric(horizontal: 10), // Add padding if needed
      decoration: BoxDecoration(
        border: Border.all(color: Colors.grey),
        borderRadius: BorderRadius.circular(8),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: widget.selectedValue,
          iconSize: 16, // Make the icon smaller
          isDense: true,
          onChanged: (String? newValue) {
            if (newValue != null) {
              setState(() {
                widget.selectedValue = newValue;
              });
            }
          },
          items: list
              .map<DropdownMenuItem<String>>((String value) {
            return DropdownMenuItem<String>(
              value: value,
              child: Text(
                valueMap[value]!,
                style: TextStyle(fontSize: 12), // Make the text smaller
              ),
            );
          }).toList(),
        ),
      ),
    );
  }
}