import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/AuthModel.dart';
import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/views/pembeli/components/Keranjang/ListKeranjang.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/views/pembeli/Checkout.dart';
import 'package:laukpauk/models/CheckoutModel.dart';

class Keranjang extends StatefulWidget {
  _KeranjangState createState() => _KeranjangState();
}

class _KeranjangState extends State<Keranjang> {
  String removedItem = '';
  bool isAnyCart = false;
  int updatedItemInt = 0;

  bool _isUserLoggedInState = false;

  Future<List> getKeranjangPerPenjual() async {
    String cartCode = await LocalStorage.getCartCode();

    String _params =
        await HTTPRequest.getQueryString(params: 'cartCode=' + cartCode);

String _urlString = 
        ServerPath.apiBaseURL + '/User/getCartDividedBySeller?' + _params;
    Uri _url = Uri.parse(_urlString);

    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = json.decode(httpResponse.body);
      List keranjang = dataObject['listCart'];

      return keranjang;
    }

    return [];
  }

  Future<int> getTotalKeranjang() async {
    int totalKeranjang = await CheckoutModel.getTotalKeranjang();

    return totalKeranjang;
  }

  void initializationState() async {
    bool _isUserLoggedIn = await AuthModel.isLogin();
    int jumlahKeranjang = await HeaderModel.getJumlahCart();

    setState(() {
      isAnyCart = (jumlahKeranjang >= 1);
      _isUserLoggedInState = _isUserLoggedIn;
    });
  }

  void initState() {
    super.initState();
    this.initializationState();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        backgroundColor: CustomColors.lightGrey,
        appBar: AppBar(
          backgroundColor: CustomColors.green,
          title: Text('Keranjang'),
          leading: IconButton(
              icon: Icon(FontAwesomeIcons.arrowLeft),
              color: Colors.white,
              onPressed: () {
                Navigator.pop(context);
              }),
        ),
        body: Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
          child: Column(
            children: [
              (isAnyCart)
                  ? Card(
                      elevation: 8,
                      shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10)),
                      child: Padding(
                        padding:
                            EdgeInsets.symmetric(horizontal: 15, vertical: 20),
                        child: Column(
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Text('SUBTOTAL',
                                          style: TextStyle(
                                              color: Colors.black,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      5,
                                              fontWeight: FontWeight.w500),
                                          textAlign: TextAlign.left),
                                      FutureBuilder<int>(
                                          future: getTotalKeranjang(),
                                          builder: (context, snapshot) {
                                            if (snapshot.hasData) {
                                              if (snapshot.data != null) {
                                                int totalKeranjang =
                                                    snapshot.data!;
                                                return Text(
                                                    Currency.formatRupiah(
                                                        totalKeranjang, 0),
                                                    style: TextStyle(
                                                        color:
                                                            CustomColors.green,
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            4,
                                                        fontWeight:
                                                            FontWeight.w700));
                                              }
                                            }
                                            return Center(
                                                child: SizedBox(
                                                    child:
                                                        CircularProgressIndicator(
                                                            strokeWidth: 1.5),
                                                    width: 15,
                                                    height: 15));
                                          })
                                    ]),
                                SizedBox(width: 30),
                                Expanded(
                                  child: Opacity(
                                    opacity: (isAnyCart) ? 1 : .35,
                                    child: CustomButton.button(
                                        context, 'Checkout', () {
                                      if (isAnyCart) {
                                        Navigator.push(
                                            context,
                                            MaterialPageRoute(
                                                builder: (_) =>
                                                    (_isUserLoggedInState)
                                                        ? Checkout()
                                                        : Login(
                                                            routeTo:
                                                                Checkout())));
                                      } else {
                                        String _title = 'Keranjang';
                                        String _subTitle =
                                            'Anda belum membeli item apapun, silahkan belanja untuk bisa checkout.';
                                        Widget _actions = Row(
                                            mainAxisAlignment:
                                                MainAxisAlignment.center,
                                            children: [
                                              TextButton(
                                                  onPressed: () {
                                                    Navigator.pop(context);
                                                  },
                                                  child: Text('OK'))
                                            ]);

                                        CustomAlert.textComponent(context,
                                            title: _title,
                                            subTitle: _subTitle,
                                            actions: _actions);
                                      }
                                    },
                                        color: CustomButton.themeColor,
                                        outline: true,
                                        isPill: true),
                                  ),
                                )
                              ],
                            ),
                            FutureBuilder(
                                future: CheckoutModel.getWalletConfiguration(),
                                builder: (context, snapshot) {
                                  if (snapshot.hasData) {
                                    dynamic walletConfiguration = snapshot.data;

                                    if (walletConfiguration == false) {
                                      return Padding(
                                        padding: EdgeInsets.only(top: 15),
                                        child: Text(
                                            'Gagal mengambil data konfigurasi Dompet/Wallet'),
                                      );
                                    } else {
                                      if (walletConfiguration['isWalletOn']) {
                                        int potonganWallet = int.parse(
                                            walletConfiguration[
                                                'walletDiscount']);
                                        int minimalTransaksiWallet = int.parse(
                                            walletConfiguration[
                                                'minGrandTotalTransactionForWallet']);
                                        return Padding(
                                          padding: EdgeInsets.only(top: 15),
                                          child: Text(
                                              "Potongan langsung dari dompet sebesar " +
                                                  Currency.formatRupiah(
                                                      potonganWallet, 0) +
                                                  " untuk setiap minimal belanja " +
                                                  Currency.formatRupiah(
                                                      minimalTransaksiWallet,
                                                      0),
                                              textAlign: TextAlign.center,
                                              style: TextStyle(
                                                  fontStyle: FontStyle.italic,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.25)),
                                        );
                                      } else {
                                        return SizedBox();
                                      }
                                    }
                                  } else {
                                    return Center(
                                        child: SizedBox(
                                            child: CircularProgressIndicator(
                                                strokeWidth: 1.5),
                                            width: 15,
                                            height: 15));
                                  }
                                }),
                          ],
                        ),
                      ),
                    )
                  : SizedBox(),
              Container(
                  width: SizeConfig.horizontalBlock * 100,
                  padding:
                      EdgeInsets.only(left: 5, bottom: 5, top: 10, right: 5),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      (isAnyCart)
                          ? InkWell(
                              onTap: () async {
                                bool konfirmasi =
                                    await CheckoutModel.konfirmasi(context) ??
                                        false;
                                if (konfirmasi) {
                                  bool statusBersihkanKeranjang =
                                      await CheckoutModel.bersihkanKeranjang();
                                  if (statusBersihkanKeranjang) {
                                    setState(() {
                                      updatedItemInt++;
                                      isAnyCart = false;
                                    });
                                  }

                                  int jumlahKeranjang =
                                      await HeaderModel.getJumlahCart();
                                  Provider.of<HeaderViewModel>(context,
                                          listen: false)
                                      .jumlahCart = jumlahKeranjang;
                                }
                              },
                              child: Text('Bersihkan Keranjang',
                                  textAlign: TextAlign.left,
                                  style: TextStyle(
                                      color: CustomColors.textDanger,
                                      fontWeight: FontWeight.w700,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5)),
                            )
                          : SizedBox()
                    ],
                  )),
              Container(
                child: FutureBuilder<List>(
                    future: getKeranjangPerPenjual(),
                    builder: (context, snapshot) {
                      if (snapshot.hasData) {
                        if (snapshot.data != null) {
                          List keranjangPerPenjual = snapshot.data!;

                          return ListKeranjang(
                            keranjangPerPenjual,
                            onRemoveItemSuccessed: (removedItemID) async {
                              int jumlahKeranjang =
                                  await HeaderModel.getJumlahCart();
                              setState(() {
                                removedItem = removedItemID;
                                isAnyCart = jumlahKeranjang >= 1;
                              });
                            },
                            onEditItemSuccessed: () {
                              setState(() {
                                updatedItemInt++;
                              });
                            },
                            onRefresh: () {
                              initializationState();
                            },
                          );
                        }
                      }
                      return Loading();
                    }),
              ),
              
            ],
          ),
        ));
  }
}
