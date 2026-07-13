import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/Voucher/VoucherItem.dart';
import 'package:laukpauk/models/AuthModel.dart';

class Voucher extends StatefulWidget {
  final String appBarTitle;
  Voucher({this.appBarTitle = 'Voucher Saya'});
  _VoucherState createState() => _VoucherState();
}

class _VoucherState extends State<Voucher> {
  Future<List> getVoucher() async {
    String entitasVoucher =
        'id, foto, minimumBelanja, berlakuMulai, berlakuSampai, maximumUsage';

    String _params = await HTTPRequest.getQueryString(
        authToken: true, params: 'entitasVoucher=$entitasVoucher');

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/User/getVoucher?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List listVoucher = dataObject['listVoucher'];

      return listVoucher;
    }

    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool isLogin = snapshot.data!;
              if (isLogin) {
                return Scaffold(
                    appBar: AppBar(
                        title: Text(widget.appBarTitle),
                        backgroundColor: CustomColors.green),
                    backgroundColor: CustomColors.aliceBlue,
                    body: FutureBuilder<List>(
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            if (snapshot.data != null) {
                              List dataVoucher = snapshot.data!;
                              List<Widget> dataVoucherWidget = [];
                              if (dataVoucher.length >= 1) {
                                dataVoucherWidget = dataVoucher
                                    .map((voucher) => VoucherItem(voucher))
                                    .toList();
                              }
                              return RefreshIndicator(onRefresh: () async {
                                setState(() {});
                              }, child: LayoutBuilder(
                                  builder: (context, constraints) {
                                return ListView(
                                    padding: EdgeInsets.zero,
                                    children: [
                                      Container(
                                          constraints: BoxConstraints(
                                              minHeight: constraints.maxHeight),
                                          child: (dataVoucher.length >= 1)
                                              ? Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5,
                                                      vertical: 5),
                                                  child: Container(
                                                      width:
                                                          MediaQuery.of(context)
                                                              .size
                                                              .width,
                                                      child: Column(
                                                          children:
                                                              dataVoucherWidget)))
                                              : Column(
                                                  mainAxisAlignment:
                                                      MainAxisAlignment.center,
                                                  crossAxisAlignment:
                                                      CrossAxisAlignment.center,
                                                  children: [
                                                      Image.asset(
                                                          ClientPath.assetsImg +
                                                              '/empty.png',
                                                          width: SizeConfig
                                                                  .horizontalBlock *
                                                              30),
                                                      SizedBox(height: 10),
                                                      Text(
                                                          'Anda tidak memiliki voucher aktif apapun',
                                                          style: TextStyle(
                                                              color: CustomColors
                                                                  .textMuted,
                                                              fontSize: SizeConfig
                                                                      .horizontalBlock *
                                                                  3.5)),
                                                      SizedBox(height: 10),
                                                    ]))
                                    ]);
                              }));
                            }
                          }
                          return Loading(
                            showLoadingImage: true,
                            showCircularProgressIndicator: false,
                            circularProgressIndicatorSize: Size(20.0, 20.0),
                          );
                        },
                        future: getVoucher()));
              }
            }
            return Login(routeTo: Voucher());
          } else {
            return Scaffold(
                body: Loading(
                    showLoadingImage: true,
                    showCircularProgressIndicator: false));
          }
        });
  }
}
