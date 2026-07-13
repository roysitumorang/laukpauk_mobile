import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/Environment.dart';
import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/libraries/Routing.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';
import 'package:firebase_dynamic_links/firebase_dynamic_links.dart';

import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/views/pembeli/components/Notifikasi/DetailNotifikasiGlobal.dart';

import 'package:laukpauk/models/TransaksiModel.dart';

class MainModel {
  static Future<void> initOneSignal(
      GlobalKey? globalKey, BuildContext buildContext) async {
    BuildContext _currentContext = (globalKey != null)
        ? globalKey.currentContext ?? buildContext
        : buildContext;

    String _oneSignalKey;
    if (Environment.environment == 'production') {
      _oneSignalKey = '7314cba5-1b0d-455a-916b-700a566b207f';
    } else {
      _oneSignalKey = 'f8ffb130-45e4-413f-87d2-cd75f5dfc97b';
    }

    OneSignal.shared.setLogLevel(OSLogLevel.verbose, OSLogLevel.none);
    OneSignal.shared.setRequiresUserPrivacyConsent(false);

    OneSignal.shared.setNotificationOpenedHandler(
        (OSNotificationOpenedResult openedResult) async {
      OSNotification _notification = openedResult.notification;
      Map<String, dynamic>? _additionalData = _notification.additionalData;

      if (_additionalData != null) {
        String notifikasi = _additionalData['notifikasi'];
        String notifikasiLC = notifikasi.toLowerCase();

        if (notifikasiLC == 'khususuntukkamu') {
          String _routeName = _additionalData['routeName'];
          String _routeParams = _additionalData['routeParams'];

          RouteSettings _routeSettings =
              RouteSettings(name: _routeName, arguments: _routeParams);
          Widget _widgetToRoute = Routing.routeWidget(_routeSettings);

          await Navigator.of(_currentContext).pushAndRemoveUntil(
              MaterialPageRoute(builder: (_) => _widgetToRoute),
              (route) => false);
        }

        if (notifikasiLC == 'update') {
          String idNotifikasi = _additionalData['idNotifikasi'].toString();
          await Navigator.of(_currentContext).push(MaterialPageRoute(
              builder: (_) =>
                  SplashScreen(routeTo: DetailNotifikasiGlobal(idNotifikasi))));
        }
      }
    });

    await OneSignal.shared.setAppId(_oneSignalKey);
  }

  static Future<void> firebaseOnLink(
      GlobalKey? globalKey, BuildContext buildContext) async {
    BuildContext _currentContext = (globalKey != null)
        ? globalKey.currentContext ?? buildContext
        : buildContext;

    PendingDynamicLinkData? data =
        await FirebaseDynamicLinks.instance.getInitialLink();
    if (data != null) {
      Uri deepLink = data.link;

      String _path = deepLink.path;
      Map<String, dynamic> _decodedArguments = deepLink.queryParameters;

      RouteSettings _routeSettings =
          RouteSettings(name: _path, arguments: _decodedArguments);
      Widget _widgetToRoute = Routing.routeWidget(_routeSettings);

      await Navigator.of(_currentContext).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => _widgetToRoute), (route) => false);
    }

    FirebaseDynamicLinks.instance.onLink.listen((dynamicLinkData) {
      String _path = dynamicLinkData.link.path;
      Map<String, dynamic>? _decodedArguments;
      if (dynamicLinkData.link.hasQuery) {
        _decodedArguments = dynamicLinkData.link.queryParameters;
      }

      RouteSettings _routeSettings =
          RouteSettings(name: _path, arguments: _decodedArguments);
      Widget _widgetToRoute = Routing.routeWidget(_routeSettings);

      Navigator.of(_currentContext).pushAndRemoveUntil(
          MaterialPageRoute(builder: (_) => _widgetToRoute), (route) => false);
    }).onError((error) {
      print('terjadi error');
      print(error.message);
      return Future.error(error);
    });
  }

  static Future<void> initCallbackFromNativeCode(BuildContext context) async {
    MethodChannel _methodChannel = MethodChannel('native.code');
    _methodChannel
        .setMethodCallHandler((call) => _nativeCodeCallHandler(context, call));
  }

  static Future<dynamic> _nativeCodeCallHandler(
      BuildContext context, MethodCall call) async {
    String _calledMethod = call.method;
    // String _arguments = call.arguments;

    if (_calledMethod == 'jadwalDeliveryKadaluarsa') {
      // _arguments = null;
      await TransaksiModel.removeTransactionSession(context);
      await HeaderModel.alertADJDKadaluarsa(context);
    }
    if (_calledMethod == 'updateAvailable') {
      bool? _wannaUpdate = await askToUpdate(context);
      if (_wannaUpdate != null && _wannaUpdate) {
        Uri _url = Uri.parse(
            'https://play.google.com/store/apps/details?id=id.laukpauk');
        if (await canLaunchUrl(_url)) {
          await launchUrl(_url);
        }
      } else {
        exit(0);
      }
    }
  }

  static Future<bool?> askToUpdate(BuildContext context) async {
    Widget _icon =
        Image.asset(ClientPath.assetsImg + '/laukpauk-icon.png', width: 50.0);
    String _subTitle =
        'Update Yuk! Banyak fitur-fitur baru menanti kamu nih...';

    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      CustomButton.button(context, 'Tidak', () {
        Navigator.pop(context, false);
      }, outline: true, color: CustomColors.textMuted),
      SizedBox(width: 5.0),
      CustomButton.button(context, 'Update', () {
        Navigator.pop(context, true);
      }, color: CustomColors.green)
    ]);

    return await CustomAlert.textComponent(context,
        icon: _icon,
        title: 'Update Aplikasi Terbaru',
        subTitle: _subTitle,
        actions: _actions);
  }
}