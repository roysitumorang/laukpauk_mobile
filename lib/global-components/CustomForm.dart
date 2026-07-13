import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class SelectBoxItem {
  dynamic value;
  String text;

  SelectBoxItem(this.value, this.text);
}

class CustomForm {
  static Widget label(BuildContext parentContext,
      {required String label,
      double? letterSpacing,
      double? fontSize,
      double? bottomPadding,
      Color? color,
      FontWeight? fontWeight}) {
    SizeConfig().initSize(parentContext);

    double _letterSpacing = (letterSpacing != null) ? letterSpacing : 1.5;
    double _fontSize =
        (fontSize != null) ? fontSize : SizeConfig.horizontalBlock * 3.25;
    double _bottomPadding = (bottomPadding != null) ? bottomPadding : 10.0;
    Color _color = (color != null) ? color : CustomColors.green;
    FontWeight _fontWeight =
        (fontWeight != null) ? fontWeight : FontWeight.w700;

    return Padding(
      padding: EdgeInsets.only(bottom: _bottomPadding),
      child: Text(label.toUpperCase(),
          style: TextStyle(
              letterSpacing: _letterSpacing,
              color: _color,
              fontSize: _fontSize,
              fontWeight: _fontWeight)),
    );
  }

  static Widget readOnlyTextField(BuildContext parentContext,
      {required dynamic value,
      Widget? leading,
      double leadingAndHintTextSpacing = 0.0,
      Color? borderColor,
      double borderWidth = 1.0,
      Widget? trailing,
      bool obsecureText = false}) {
    SizeConfig().initSize(parentContext);

    double _horizontalPadding = SizeConfig.horizontalBlock * 3.75;
    double _verticalPadding = SizeConfig.horizontalBlock * 3.25;

    return Container(
        padding: EdgeInsets.symmetric(
            horizontal: _horizontalPadding, vertical: _verticalPadding),
        decoration: BoxDecoration(
            color: CustomColors.grey,
            border: (borderColor != null)
                ? Border.all(color: borderColor, width: borderWidth)
                : null,
            borderRadius: BorderRadius.circular(8.0)),
        child: Row(children: [
          (leading != null) ? leading : SizedBox(),
          SizedBox(width: leadingAndHintTextSpacing),
          Expanded(
              child: TextFormField(
                  initialValue: value.toString(),
                  obscureText: obsecureText,
                  readOnly: true,
                  decoration: InputDecoration(
                      counterText: '',
                      isDense: true,
                      contentPadding: EdgeInsets.zero,
                      border: InputBorder.none))),
          (trailing != null) ? trailing : SizedBox(),
        ]));
  }

  static Widget textField(BuildContext parentContext,
      {required String hintText,
      required TextEditingController controller,
      TextStyle? hintStyle,
      Widget? leading,
      double leadingAndHintTextSpacing = 0.0,
      TextInputType keyboardType = TextInputType.text,
      bool obsecureText = false,
      Widget? trailing,
      Color? borderColor,
      double borderWidth = 1.0,
      bool enabled = true,
      int? maxLength,
      String counterText = '',
      void Function()? onEditingComplete,
      void Function(String value)? onChange,
      List<TextInputFormatter>? inputFormatters,
      FocusNode? focusNode,
      Color? backgroundColor,
      bool useLabel = false}) {
    SizeConfig().initSize(parentContext);

    TextStyle _hintStyle = (hintStyle != null)
        ? hintStyle
        : TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5);
    double _horizontalPadding = SizeConfig.horizontalBlock * 3.75;
    double _verticalPadding = SizeConfig.horizontalBlock * 3.25;

    return Container(
      padding: EdgeInsets.symmetric(
          horizontal: _horizontalPadding, vertical: _verticalPadding),
      decoration: BoxDecoration(
          color: (enabled)
              ? (backgroundColor != null)
                  ? backgroundColor
                  : CustomColors.nearlyWhite
              : CustomColors.grey,
          border: (borderColor != null)
              ? Border.all(color: borderColor, width: borderWidth)
              : null,
          borderRadius: BorderRadius.circular(8.0)),
      child: Row(
        children: [
          (leading != null) ? leading : SizedBox(),
          SizedBox(width: leadingAndHintTextSpacing),
          Expanded(
            child: TextField(
                focusNode: focusNode,
                onChanged: (onChange != null) ? onChange : null,
                onEditingComplete:
                    (onEditingComplete != null) ? onEditingComplete : null,
                maxLength: maxLength,
                maxLengthEnforcement:
                    (maxLength != null) ? MaxLengthEnforcement.enforced : null,
                controller: controller,
                style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5),
                keyboardType: keyboardType,
                obscureText: obsecureText,
                inputFormatters: inputFormatters,
                decoration: InputDecoration(
                    labelText: (useLabel) ? hintText : null,
                    labelStyle: (useLabel) ? _hintStyle : null,
                    counterText: counterText,
                    isCollapsed: false,
                    enabled: enabled,
                    hintText: (useLabel) ? null : hintText,
                    hintStyle: (useLabel) ? null : _hintStyle,
                    isDense: true,
                    contentPadding: EdgeInsets.zero,
                    border: InputBorder.none)),
          ),
          (trailing != null) ? trailing : SizedBox(),
        ],
      ),
    );
  }

  static Widget selectBox(BuildContext parentContext,
      {required List<SelectBoxItem> items,
      required dynamic value,
      required void Function(dynamic selectedValue) onChanged,
      required String defaultItemText,
      required dynamic defaultItemValue,
      Widget? leading,
      double leadingAndHintTextSpacing = 0.0,
      TextInputType keyboardType = TextInputType.text,
      bool obsecureText = false,
      Widget? trailing,
      Color? borderColor,
      double borderWidth = 1.0,
      bool enabled = true,
      TextStyle? itemTextStyle}) {
    SizeConfig().initSize(parentContext);

    double _horizontalPadding = SizeConfig.horizontalBlock * 3.75;
    double _verticalPadding = SizeConfig.horizontalBlock * 3.0;

    TextEditingController _controller =
        TextEditingController(text: value.toString());
    if (!enabled) {
      //khusus untuk selectBox yang disabled, nilai trailing ditimpa
      trailing = Icon(Icons.arrow_drop_down);
    }

    TextStyle _itemTextStyle = (itemTextStyle != null)
        ? itemTextStyle
        : TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5);

    List<DropdownMenuItem> _items = [];
    if (items.length >= 1) {
      _items = items
          .map((item) => DropdownMenuItem(
              value: item.value, child: Text(item.text, style: _itemTextStyle)))
          .toList();
    }

    return Container(
      padding: EdgeInsets.symmetric(
          horizontal: _horizontalPadding, vertical: _verticalPadding),
      decoration: BoxDecoration(
          color: (enabled) ? CustomColors.nearlyWhite : CustomColors.grey,
          border: (borderColor != null)
              ? Border.all(color: borderColor, width: borderWidth)
              : null,
          borderRadius: BorderRadius.circular(8.0)),
      child: Row(
        children: [
          (leading != null) ? leading : SizedBox(),
          SizedBox(width: leadingAndHintTextSpacing),
          Expanded(
            child: (enabled)
                ? DropdownButton<dynamic>(
                    onChanged: (enabled) ? onChanged : (selectedValue) => null,
                    items: [
                      DropdownMenuItem(
                          child: Text(defaultItemText, style: _itemTextStyle),
                          value: defaultItemValue),
                      ..._items
                    ],
                    isDense: true,
                    isExpanded: true,
                    value: value,
                    underline: SizedBox(),
                  )
                : TextField(
                    controller: _controller,
                    keyboardType: keyboardType,
                    obscureText: obsecureText,
                    decoration: InputDecoration(
                        enabled: enabled,
                        isDense: true,
                        contentPadding: EdgeInsets.zero,
                        border: InputBorder.none)),
          ),
          (trailing != null) ? trailing : SizedBox(),
        ],
      ),
    );
  }
}
