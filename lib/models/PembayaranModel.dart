import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/views/pembeli/DoPayment.dart';

import 'package:laukpauk/views/SplashScreen.dart';

class PembayaranModel {
  static Future<Map<String, dynamic>> buatOrder(
      {required Map<String, dynamic> metodePembayaranTerpilihMap,
      required Map<String, dynamic> checkoutData,
      required String moreData}) async {
    try {
      String _channelCode = metodePembayaranTerpilihMap['channel_code'];
      String _namaMetodePembayaran = metodePembayaranTerpilihMap['name'];

      Map<String, dynamic> _delivery =
          await LocalStorage.getSavedLatitudeAndLongitude();
      String _alamatDelivery = _delivery['alamat'];

      Map<String, dynamic> _httpBody = {
        ...checkoutData,
        'metodePembayaran': _channelCode,
        'namaMetodePembayaran': _namaMetodePembayaran,
        'moreDataMetodePembayaran': moreData,
        'alamatDelivery': _alamatDelivery
      };

      String _params = await HTTPRequest.getQueryString(authToken: true);
      Map<String, dynamic> _body = await HTTPRequest.postBody(body: _httpBody);

      Uri _url = Uri.parse(
          ServerPath.baseURL + '/api-v2/Transaksi/checkout?' + _params);

      Response httpResponse = await post(_url, body: _body);

      print(httpResponse.body);

      if (httpResponse.statusCode == 200) {
        Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

        return dataObject;
      }
    } catch (e) {}

    return {'statusCheckout': false, 'message': 'Nande'};
  }

  static Future<void> _urlTidakValid(BuildContext context) async {
    String _title = 'Laukpauk';
    String _subTitle = 'URL Tidak Valid! Silahkan coba lagi!';
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      TextButton(
        child: Text('Coba Lagi!'),
        onPressed: () {
          Navigator.pop(context);
        },
      )
    ]);

    CustomAlert.textComponent(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }

  static Future<void> buatOrderNextStep(
      {required BuildContext context,
      required String channelCodeTerpilihLC,
      required Map<String, dynamic> returnedData,
      required Map<String, dynamic> checkoutData,
      required Map<String, dynamic> metodePembayaranTerpilihMap,
      required String moreData,
      required void Function(Map<String, dynamic> data)
          checkoutCallback}) async {
    Map<String, dynamic> dataCheckout = {
      ...checkoutData,
      'metodePembayaran': metodePembayaranTerpilihMap['channel_code'],
      'namaMetodePembayaran': metodePembayaranTerpilihMap['name'],
      'moreDataMetodePembayaran': moreData
    };

    bool _statusCheckout = returnedData['statusCheckout'];

    dynamic _paymentResponse = returnedData['paymentResponse'];

    if (channelCodeTerpilihLC == 'dana' || channelCodeTerpilihLC == 'linkaja') {
      if (_statusCheckout) {
        //payment response untuk diluar dana dan link aja adalah idtransaksi

        Uri checkoutURL = Uri.parse(_paymentResponse['checkout_url']);
        if (await canLaunchUrl(checkoutURL)) {
          Navigator.pushAndRemoveUntil(
              context,
              MaterialPageRoute(
                  builder: (context) =>
                      SplashScreen(removeTransactionSession: true)),
              (route) => false);
          launchUrl(checkoutURL);
        } else {
          _urlTidakValid(context);
        }
      } else {
        _checkoutFailedAlert(context,
            channelCodeLC: channelCodeTerpilihLC,
            paymentResponse: _paymentResponse);
      }
    } else {
      if (channelCodeTerpilihLC == 'shopeepay') {
        if (_statusCheckout) {
          String _urlString = _paymentResponse['actions']['mobile_deeplink_checkout_url'];

          Uri _actionsLink = Uri.parse(
              _urlString);
          if (await canLaunchUrl(_actionsLink)) {
            Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(
                    builder: (context) =>
                        SplashScreen(removeTransactionSession: true)),
                (route) => false);

            launchUrl(_actionsLink, mode: LaunchMode.externalApplication);
          } else {
            _urlTidakValid(context);
          }
        } else {
          _checkoutFailedAlert(context,
              channelCodeLC: channelCodeTerpilihLC,
              paymentResponse: _paymentResponse);
        }
      } else {
        if (_statusCheckout) {
          Map<String, dynamic> returnedDataCheckoutCallback = {
            'responseCheckout': returnedData,
            'dataCheckout': dataCheckout
          };
          if (channelCodeTerpilihLC == 'ovo') {
            await Navigator.pushAndRemoveUntil(
                context,
                MaterialPageRoute(builder: (_) => DoPayment()),
                (route) => false);
          } else {
            //cod dan tf_bca dan kartu_kredit
            checkoutCallback(returnedDataCheckoutCallback);
          }
        } else {
          _checkoutFailedAlert(context,
              channelCodeLC: channelCodeTerpilihLC,
              paymentResponse: _paymentResponse);
        }
      }
    }
  }

  static Future<void> _checkoutFailedAlert(BuildContext context,
      {required String channelCodeLC,
      required Map<String, dynamic> paymentResponse}) async {
    SizeConfig().initSize(context);

    String _title = 'Checkout';
    String _content = 'Checkout gagal ! Coba lagi ya bun...';

    List<String> _allowedShowMessagePaymentChannel = [
      'shopeepay',
      'dana',
      'linkaja',
      'credit_card'
    ];

    if (_allowedShowMessagePaymentChannel.contains(channelCodeLC)) {
      _content = paymentResponse['message'];
    }

    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      CustomButton.button(context, 'Coba Lagi', () {
        Navigator.pop(context);
      }, color: CustomColors.green)
    ]);

    await CustomAlert.textComponent(context,
        title: _title, subTitle: _content, actions: _actions);
  }
}
