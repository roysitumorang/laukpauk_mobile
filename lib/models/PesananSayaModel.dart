import 'dart:convert';

import 'package:http/http.dart';
import 'package:flutter/material.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class PesananSayaModel {
  static Future<bool> pesananDiterima(String idTransaksi) async {
    String _params = await HTTPRequest.getQueryString(authToken: true);
    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/pesananDiterima/$idTransaksi?' +
        _params);

    Response httpResponse = await post(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool pesananDiterima = dataObject['pesananDiterima'];

      return pesananDiterima;
    }

    return false;
  }

  static Future<bool?> konfirmasii(
      BuildContext context, String title, String content,
      {String? noText, String? yesText}) async {
    SizeConfig().initSize(context);

    return showDialog<bool?>(
        context: context,
        builder: (context) => AlertDialog(
                title: Text(title,
                    style: TextStyle(fontSize: SizeConfig.horizontalBlock * 4)),
                content: Text(content,
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
                actions: <TextButton>[
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context, false);
                      },
                      child: Text((noText != null) ? noText : 'Tidak')),
                  TextButton(
                      onPressed: () {
                        Navigator.pop(context, true);
                      },
                      child:
                          Text((yesText != null) ? yesText : 'Ya, lanjutkan !'))
                ]));
  }

  static String readAbleStatusForUser(String status) {
    String statusLC = status.toLowerCase();
    String readAbleStatusForUserVariable = '';
    if (statusLC == 'pending') {
      readAbleStatusForUserVariable = 'Sudah Diterima Penjual';
    }
    if (statusLC == 'processing') {
      readAbleStatusForUserVariable = 'Sedang diproses';
    }
    if (statusLC == 'otw') {
      readAbleStatusForUserVariable = 'Dalam Perjalanan';
    }
    if (statusLC == 'received') {
      readAbleStatusForUserVariable = 'Diterima';
    }
    if (statusLC == 'rejected') {
      readAbleStatusForUserVariable = 'Dibatalkan';
    }

    return readAbleStatusForUserVariable;
  }
}
