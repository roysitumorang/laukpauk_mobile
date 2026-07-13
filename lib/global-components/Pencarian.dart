import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class Pencarian extends StatelessWidget {
  final String hint, text;
  final void Function(String string) onSearch;
  final void Function() onClose;
  Pencarian(
      {required this.onSearch,
      required this.onClose,
      this.hint = 'Type Here',
      this.text = ''});

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    TextEditingController pencarianController =
        TextEditingController(text: text);

    bool pencarianStringExist = pencarianController.text.length >= 1;

    return Container(
      decoration: BoxDecoration(
          border: Border.all(width: 1, color: CustomColors.green),
          borderRadius: BorderRadius.circular(50)),
      child: Material(
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(50)),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Padding(
                  padding: EdgeInsets.symmetric(
                      horizontal: SizeConfig.horizontalBlock * 5.0),
                  child: TextField(
                      onSubmitted: (String string) {
                        onSearch(pencarianController.text);
                      },
                      controller: pencarianController,
                      decoration: InputDecoration(
                          isDense: true,
                          contentPadding: EdgeInsets.symmetric(
                              vertical: SizeConfig.horizontalBlock * 3.25),
                          hintStyle: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 3.5),
                          hintText: hint,
                          border: InputBorder.none)),
                ),
              ),
              InkWell(
                onTap: () {
                  onSearch(pencarianController.text);
                },
                child: Container(
                  width: (pencarianStringExist)
                      ? SizeConfig.horizontalBlock * 7.5
                      : SizeConfig.horizontalBlock * 12.5,
                  child: Icon(FontAwesomeIcons.search,
                      size: SizeConfig.horizontalBlock * 3.75,
                      color: CustomColors.green),
                ),
              ),
              (pencarianStringExist)
                  ? InkWell(
                      onTap: () {
                        onClose();
                      },
                      child: Container(
                        width: SizeConfig.horizontalBlock * 12.5,
                        child: Icon(FontAwesomeIcons.times,
                            size: SizeConfig.horizontalBlock * 3.75,
                            color: CustomColors.textDanger),
                      ),
                    )
                  : SizedBox()
            ],
          )),
    );
  }
}
