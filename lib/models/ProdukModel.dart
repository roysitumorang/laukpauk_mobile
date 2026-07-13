import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/AuthModel.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/SettingModel.dart';
import 'package:laukpauk/views/pembeli/components/DetailProduk/ShowSatuanProduk.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/views/pembeli/Keranjang.dart';

class ProdukModel {
  static Future<bool?> adjdNotSetAlert(BuildContext context,
      {String namaProduk = ''}) async {
    String _title = 'Pilih Jadwal Dulu';
    String _subTitle =
        'Untuk bisa order sayur, ikan, daging segar, tentukan lokasi pengantaran dulu ya...';
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
      CustomButton.button(context, 'Tutup', () {
        Navigator.pop(context, false);
      }, color: CustomColors.textMuted, outline: true),
      const SizedBox(width: 5.0),
      CustomButton.button(context, 'Pilih Jadwal', () {
        Navigator.pop(context, true);
      }, color: CustomColors.green)
    ]);
    return CustomAlert.textComponent<bool>(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }

  static Future<void> showSatuanProduk(
      BuildContext context, Map<String, dynamic> dataProduk,
      {Map<String, dynamic>? dataPromo}) async {
    String idProduk = dataProduk['id'];

    bool? isThisProductInCAVariable, isPenjualAvailable;

    String ongkirPerKMString = '';
    Map<String, dynamic> adjdSet =
        await LocalStorage.getSavedLatitudeAndLongitude();
    String? latitude = adjdSet['latitude'];
    String? longitude = adjdSet['longitude'];
    String? jadwalDeliveryCode = adjdSet['jadwalDeliveryCode'];

    if (latitude != null && longitude != null && jadwalDeliveryCode != null) {
      isThisProductInCAVariable = await isThisProductInCA(idProduk,
          latitude: latitude, longitude: longitude);
      isPenjualAvailable = await isProductSellerAvailable(idProduk,
          jadwalDeliveryCode: jadwalDeliveryCode);
      dynamic getCostPerKM = await SettingModel.getSettingValue('costPerKm');
      if (getCostPerKM != false) {
        int costPerKM = int.parse(getCostPerKM);
        ongkirPerKMString = Currency.formatRupiah(costPerKM, 0);
      }
    }

    showModalBottomSheet(
        context: context,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.only(
                topLeft: Radius.circular(15), topRight: Radius.circular(15))),
        builder: (BuildContext buildContext) {
          return ShowSatuanProduk(
              idProduk: idProduk,
              isPenjualAvailable: isPenjualAvailable,
              isThisProductInCAVariable: isThisProductInCAVariable,
              dataProduk: dataProduk,
              ongkirPerKMString: ongkirPerKMString,
              addCartCallback: (addToCartResponse) async {
                Navigator.pop(context);
                if (addToCartResponse != null) {
                  bool _statusAddCart = addToCartResponse['statusAddCart'];
                  if (_statusAddCart) {
                    SnackBar snackBar = SnackBar(
                        duration: Duration(seconds: 2),
                        action: SnackBarAction(
                            label: 'Lihat Keranjang',
                            textColor: CustomColors.green,
                            onPressed: () {
                              Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                      builder: (_) => Keranjang()));
                            }),
                        content: Text(
                            'Berhasil ditambahkan ke dalam keranjang !',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25)));

                    ScaffoldMessenger.of(context).showSnackBar(snackBar);

                    int jumlahKeranjang = await HeaderModel.getJumlahCart();
                    Provider.of<HeaderViewModel>(context, listen: false)
                        .jumlahCart = jumlahKeranjang;
                  } else {
                    Widget? _moreWidgets;
                    if (addToCartResponse.containsKey('message')) {
                      String _message = addToCartResponse['message'];
                      _moreWidgets = Text(_message,
                          textAlign: TextAlign.center,
                          style: TextStyle(
                              color: CustomColors.textDanger,
                              fontSize: SizeConfig.horizontalBlock * 3.25));
                    }

                    String _title = 'Keranjang';
                    String _subTitle =
                        'Item gagal ditambahkan ke dalam keranjang! ';

                    Widget _actions = Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          TextButton(
                              onPressed: () => Navigator.pop(context),
                              child: Text('OK'))
                        ]);
                    await CustomAlert.textComponent(context,
                        title: _title,
                        subTitle: _subTitle,
                        actions: _actions,
                        moreWidgets: _moreWidgets);
                  }
                }
              });
        });
  }

  static Future<Map<String, dynamic>?> addToCart(
      String idProduk, String idSatuan, String quantity,
      {String? idPromo}) async {
    bool _isLoggedIn = await AuthModel.isLogin();

    String cartCode = await LocalStorage.getCartCode();

    Map<String, dynamic>? jadwalDeliveryTerpilih =
        await LocalStorage.savedJadwalDeliveryMap();
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String dayDelivery = (jadwalDeliveryTerpilih != null)
        ? jadwalDeliveryTerpilih['day']
        : 'Hari Pengantaran';
    String jadwalDeliveryCode = savedADJD['jadwalDeliveryCode'];

    Map<String, dynamic> _cartData = {
      'cartCode': cartCode,
      'idProduk': idProduk,
      'jenisProduk': 'nonpaket',
      'idSatuan': idSatuan,
      'quantity': quantity,
      'jadwalDeliveryCode': jadwalDeliveryCode,
      'dayDelivery': dayDelivery
    };

    if (idPromo != null) {
      _cartData['idPromo'] = idPromo;
    }
    if (_isLoggedIn) {
      String _currentUserID = await LocalStorage.getCurrentUserID();
      _cartData['idUser'] = _currentUserID;
    }

    Map<String, dynamic> _body = await HTTPRequest.postBody(body: _cartData);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Keranjang/saveCart');
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject;
    }
    return null;
  }

  static Future<bool?> isThisProductInCA(String idProduk,
      {required String latitude, required String longitude}) async {
    String _params = await HTTPRequest.getQueryString(
        params: 'idProduk=$idProduk&latitude=$latitude&longitude=$longitude');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/isThisProductInCA?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      bool inCA = serverResponse['isThisProductInCA'];

      return inCA;
    }
    return null;
  }

  static Future<bool?> isProductSellerAvailable(String idProduk,
      {required String jadwalDeliveryCode}) async {
    String _params = await HTTPRequest.getQueryString(
        params: 'idProduk=$idProduk&jadwalDeliveryCode=$jadwalDeliveryCode');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Produk/isProductSellerAvailable?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> serverResponse = jsonDecode(httpResponse.body);
      bool isSellerAvailable = serverResponse['isSellerAvailable'];

      return isSellerAvailable;
    }

    return null;
  }

  static Future<dynamic> getDetailProduk(String? idProduk) async {
    idProduk = idProduk ?? '';

    String _params =
        await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/detailProduk?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      dynamic detailProduk = dataObject['detailProduk'];
      return detailProduk;
    }

    return false;
  }

  static Future<dynamic> getLowestPrice(String idProduk) async {
    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Produk/getLowestPriceOfThisProduct/$idProduk');
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      dynamic lowestPrice = dataObject['lowestPrice'];

      return lowestPrice;
    }

    return false;
  }
}
