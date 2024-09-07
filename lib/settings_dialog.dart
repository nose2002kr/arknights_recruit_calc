
import 'package:flutter/material.dart';

class Settings extends Dialog {
  BuildContext context;
  Settings(
      {super.key, required this.context}
      );

  @override
  Widget? get child {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          const Text('SETTINGS'), // need to translate
          Table(
            defaultVerticalAlignment: TableCellVerticalAlignment.middle,
            columnWidths: const <int, TableColumnWidth>{
              0: IntrinsicColumnWidth(),
              1: FixedColumnWidth(10),
              2: IntrinsicColumnWidth(),
            },
            children: const [
              TableRow(
                  children: [
                    Text('Language', textAlign: TextAlign.right),
                    Spacer(),
                    DropdownMenuLanguage()
                  ]
              ),
              TableRow(
                  children: [
                    Text('HIDE_LOW_GRADE_OPERATOR', textAlign: TextAlign.right), // need to translate
                    Spacer(),
                    SwitchButton()
                  ]
              )
            ],
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
            },
            child: const Text('CLOSE'),  // need to translate
          ),
        ],
      ),
    );
  }
}


class SwitchButton extends StatefulWidget {
  const SwitchButton({super.key});

  @override
  State<SwitchButton> createState() => _SwitchButtonState();
}

class _SwitchButtonState extends State<SwitchButton> {
  bool light = true;

  @override
  Widget build(BuildContext context) {
    final WidgetStateProperty<Color?> trackColor =
    WidgetStateProperty.resolveWith<Color?>(
          (Set<WidgetState> states) {
        if (states.contains(WidgetState.selected)) {
          return Colors.blueAccent.shade100;
        }
        return null;
      },
    );
    final WidgetStateProperty<Color?> overlayColor =
    WidgetStateProperty.resolveWith<Color?>(
          (Set<WidgetState> states) {
        if (states.contains(WidgetState.selected)) {
          return Colors.blueAccent.shade100.withOpacity(0.54);
        }
        if (states.contains(WidgetState.disabled)) {
          return Colors.grey.shade400;
        }
        return null;
      },
    );

    return Switch(
      value: light,
      overlayColor: overlayColor,
      trackColor: trackColor,
      thumbColor: WidgetStatePropertyAll<Color>(Colors.black87),
      onChanged: (bool value) {
        setState(() {
          light = value;
        });
      },
    );
  }
}


class DropdownMenuLanguage extends StatefulWidget {
  const DropdownMenuLanguage({super.key});

  @override
  State<DropdownMenuLanguage> createState() => _DropdownMenuLanguageState();
}

const List<String> list = <String>['en_US','ko_KR'];
const Map<String, String> valueMap = {'en_US':'English', 'ko_KR':'한국어'};

class _DropdownMenuLanguageState extends State<DropdownMenuLanguage> {
  String dropdownValue = list.first;

  @override
  Widget build(BuildContext context) {
    return DropdownMenu<String>(
      initialSelection: list.first,
      onSelected: (String? value) {
        // This is called when the user selects an item.
        setState(() {
          dropdownValue = value!;
        });
      },
      dropdownMenuEntries: list.map<DropdownMenuEntry<String>>((String value) {
        return DropdownMenuEntry<String>(
          value: value,
          label: valueMap[value]!,
        );
      }).toList(),
    );
  }
}
