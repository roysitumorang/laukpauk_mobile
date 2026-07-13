import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

class KeranjangItemInput extends StatefulWidget {
  final String label;
  final String value;
  final dynamic inputType;
  final Function callBack;
  final double height;
  final int minLines;

  KeranjangItemInput(this.label, this.value, this.inputType, this.callBack,
      {this.height = 55.0, this.minLines = 1});
  _KeranjangItemInputState createState() => _KeranjangItemInputState();
}

class _KeranjangItemInputState extends State<KeranjangItemInput> {
  TextEditingController textController = TextEditingController();

  void initState() {
    super.initState();
    textController.text = widget.value;
  }

  void dispose() {
    super.dispose();
    textController.dispose();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      margin: EdgeInsets.only(bottom: 13),
      height: widget.height,
      decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(10), color: Color(0xFFF0F8FF)),
      child: Container(
          padding: EdgeInsets.only(right: 15, left: 25),
          decoration: BoxDecoration(borderRadius: BorderRadius.circular(10)),
          child: Row(children: [
            Expanded(
              child: TextField(
                  minLines: widget.minLines,
                  maxLines: widget.minLines + 1,
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5),
                  controller: textController,
                  onChanged: (dataTyped) {
                    widget.callBack(dataTyped);
                  },
                  keyboardType: widget.inputType,
                  decoration: InputDecoration(
                      border: InputBorder.none,
                      labelText: widget.label,
                      labelStyle: TextStyle(
                          fontFamily: 'WorkSans',
                          fontWeight: FontWeight.w500,
                          color: Colors.grey))),
            )
          ])),
    );
  }
}
