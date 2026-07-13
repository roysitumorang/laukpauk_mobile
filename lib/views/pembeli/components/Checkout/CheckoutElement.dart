import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

class CheckoutElement extends StatefulWidget {
  final String label;
  final String? initialValue;
  final dynamic inputType;
  final Function callBack;

  CheckoutElement(this.label, this.inputType, this.callBack,
      {this.initialValue});
  _CheckoutElementState createState() => _CheckoutElementState();
}

class _CheckoutElementState extends State<CheckoutElement> {
  TextEditingController controller = TextEditingController();

  void initialization() async {
    String? initialValue = widget.initialValue;
    if (initialValue != null) {
      controller.text = initialValue;
    }
  }

  void initState() {
    super.initState();
    initialization();
  }

  void disposeAction() async {
    controller.dispose();
  }

  void dispose() {
    super.dispose();
    disposeAction();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      margin: EdgeInsets.only(bottom: 7.5),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Container(
            height: SizeConfig.horizontalBlock * 15,
            padding: EdgeInsets.only(right: 15, left: 25),
            decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                color: Color(0xFFF0F8FF)),
            child: Row(children: [
              Expanded(
                child: TextField(
                    controller: controller,
                    maxLines: null,
                    expands: true,
                    onChanged: (dataTyped) {
                      widget.callBack(dataTyped);
                    },
                    keyboardType: widget.inputType,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5),
                    decoration: InputDecoration(
                        border: InputBorder.none,
                        labelText: widget.label,
                        contentPadding: EdgeInsets.only(
                            top: SizeConfig.horizontalBlock * 3.25,
                            bottom: SizeConfig.horizontalBlock * 3.25),
                        labelStyle: TextStyle(
                            fontFamily: 'WorkSans',
                            fontWeight: FontWeight.w500,
                            color: Colors.grey))),
              )
            ]))
      ]),
    );
  }
}
