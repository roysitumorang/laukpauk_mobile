import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

const double _borderRadiusCircular = 15.0;
Widget _defaultAlertIcon =
    Image.asset(ClientPath.assetsImg + '/laukpauk-icon.png', width: 50.0);

class CustomAlert {
  static void urlCantBeLaunched(BuildContext context, {String url = ''}) {
    String _title = 'URL Tidak Dapat Diakses';
    String _subTitle =
        'URL $url tidak dapat dijalankan, kemungkinan karena alamat url tidak ditemukan atau format url yang tidak valid!';
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      CustomButton.button(context, 'OK', () {
        Navigator.pop(context);
      }, color: CustomColors.textDanger)
    ]);
    textComponent(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }

  static textComponent<T>(BuildContext _alertBuildContext,
      {Widget? icon,
      required String title,
      required String subTitle,
      Widget? actions,
      bool isDismissible = false,
      bool useRouteNavigator = false,
      Widget? moreWidgets}) {
    return showDialog<T>(
        barrierDismissible: isDismissible,
        context: _alertBuildContext,
        useRootNavigator: useRouteNavigator,
        builder: (_showDialogContext) {
          SizeConfig().initSize(_showDialogContext);

          double _titleSize = SizeConfig.horizontalBlock * 4.5;
          double _contentSize = SizeConfig.horizontalBlock * 3.25;

          return Dialog(
              elevation: 0.0,
              insetPadding: EdgeInsets.symmetric(horizontal: 16.0),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(_borderRadiusCircular)),
              child: Container(
                  width: double.infinity,
                  padding:
                      EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
                  decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius:
                          BorderRadius.circular(_borderRadiusCircular)),
                  child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.center,
                      children: [
                        (icon != null) ? icon : _defaultAlertIcon,
                        SizedBox(height: 15.0),
                        Text(title,
                            style: TextStyle(
                                fontSize: _titleSize,
                                fontWeight: FontWeight.w700)),
                        SizedBox(height: 15.0),
                        Text(subTitle,
                            textAlign: TextAlign.center,
                            style: TextStyle(fontSize: _contentSize)),
                        (moreWidgets != null)
                            ? Padding(
                                padding: EdgeInsets.only(top: 10.0),
                                child: moreWidgets)
                            : SizedBox(),
                        (actions != null)
                            ? Padding(
                                padding: EdgeInsets.only(top: 16.0),
                                child: actions,
                              )
                            : SizedBox()
                      ])));
        });
  }

  static Future<bool?> konfirmasi(BuildContext context,
      {required String title,
      required String subTitle,
      Widget? moreWidget}) async {
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      TextButton(
          onPressed: () => Navigator.pop(context, false), child: Text('Tidak')),
      TextButton(
          onPressed: () => Navigator.pop(context, true), child: Text('Ya'))
    ]);
    bool? _konfirmasi = await textComponent<bool?>(context,
        title: title,
        subTitle: subTitle,
        actions: _actions,
        moreWidgets: moreWidget);

    return _konfirmasi;
  }
}
