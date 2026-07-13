import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/pembeli/TransferBank.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/VoucherItemCheckout.dart';
import 'package:laukpauk/views/pembeli/components/Checkout/PilihanMetodePembayaran.dart';
import 'package:laukpauk/views/pembeli/BuatOrderanBerhasil.dart';
import 'package:shared_preferences/shared_preferences.dart';

class CheckoutModel {
  static Future<List> getVoucher(int totalBelanja) async {
    String totalBelanjaString = totalBelanja.toString();
    String entitasVoucher =
        'id, nama, foto, minimumBelanja, nilai, jenis, kode, maximumUsage';

    String _params = await HTTPRequest.getQueryString(
        params:
            'entitasVoucher=$entitasVoucher&forThisTotalBelanja=$totalBelanjaString',
        authToken: true);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/User/getVoucher?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List listVoucher = dataObject['listVoucher'];

      return listVoucher;
    }

    return [];
  }

  static showVoucher(BuildContext context, int totalBelanja,
      Map<String, dynamic>? voucherTerpilih, onVoucherSelected) async {
    return showModalBottomSheet(
        context: context,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.only(
                topLeft: Radius.circular(15), topRight: Radius.circular(15))),
        builder: (context) {
          SizeConfig().initSize(context);

          return Padding(
              padding: EdgeInsets.symmetric(horizontal: 15, vertical: 20),
              child: SingleChildScrollView(
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                    Text('Voucher Kamu',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 4,
                            fontWeight: FontWeight.w700)),
                    SizedBox(height: 2.5),
                    Text(
                        'Voucher aktif untuk minimal belanja ' +
                            Currency.formatRupiah(totalBelanja, 0) +
                            ' : ',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
                    SizedBox(height: 10),
                    FutureBuilder<List>(
                        future: getVoucher(totalBelanja),
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            if (snapshot.data != null) {
                              List dataVoucher = snapshot.data!;
                              if (dataVoucher.length >= 1) {
                                List<Widget> dataVoucherWidget = dataVoucher
                                    .map((voucher) => VoucherItemCheckout(
                                            voucher, voucherTerpilih,
                                            (selectedVoucher) {
                                          onVoucherSelected(selectedVoucher);
                                          Navigator.pop(context);
                                        }))
                                    .toList();
                                return SingleChildScrollView(
                                    child: Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 5, vertical: 5),
                                        child: Container(
                                            width: MediaQuery.of(context)
                                                .size
                                                .width,
                                            child: Column(
                                                children: dataVoucherWidget))));
                              }
                            }
                            return Container(
                              width: SizeConfig.horizontalBlock * 100,
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  crossAxisAlignment: CrossAxisAlignment.center,
                                  children: [
                                    Image.asset(
                                        ClientPath.assetsImg + '/empty.png',
                                        width: SizeConfig.horizontalBlock * 30),
                                    SizedBox(height: 10),
                                    Text(
                                        'Tidak ada voucher yang bisa anda gunakan.',
                                        style: TextStyle(
                                            color: CustomColors.textMuted,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5)),
                                    SizedBox(height: 10),
                                  ]),
                            );
                          } else {
                            return Center(
                                child: SizedBox(
                                    width: 25,
                                    height: 25,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 1.5)));
                          }
                        })
                  ])));
        });
  }

  static Future<void> showMetodePembayaran(BuildContext context,
      Map<String, dynamic> checkoutData, int totalTagihan) async {
    int _totalTagihan = totalTagihan;

    return showModalBottomSheet(
        isDismissible: false,
        isScrollControlled: true,
        context: context,
        enableDrag: true,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.only(
                topLeft: Radius.circular(15), topRight: Radius.circular(15))),
        builder: (BuildContext builderContext) {
          return PilihanMetodePembayaran(
              builderContext, checkoutData, _totalTagihan,
              checkoutCallback: (Map<String, dynamic> returnedData) {
            Map<String, dynamic> responseCheckout =
                returnedData['responseCheckout'];

            bool statusCheckout = responseCheckout['statusCheckout'];
            if (statusCheckout) {
              String metodePembayaran = responseCheckout['metodePembayaran'];
              String metodePembayaranLC = metodePembayaran.toLowerCase();

              Navigator.pushAndRemoveUntil(
                  builderContext,
                  MaterialPageRoute(
                      builder: (_) => (metodePembayaranLC == 'tf_bca')
                          ? TransferBank(returnedData)
                          : BuatOrderanBerhasil(
                              removeTransactionSession: true,
                              returnedDataFromCheckout: returnedData)),
                  (route) => false);
            }
          });
        });
  }

  static Future<bool> bersihkanKeranjang() async {
    String cartCode = await LocalStorage.getCartCode();
    String _params =
        await HTTPRequest.getQueryString(params: 'cartCode=$cartCode');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Keranjang/bersihkanKeranjang?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      bool statusBersihkanKeranjang = dataObject['statusBersihkanKeranjang'];

      return statusBersihkanKeranjang;
    }

    return false;
  }

  static Future<bool?> konfirmasi(BuildContext context) async {
    String _title = 'Konfirmasi Keranjang';
    String _subTitle =
        'Benarkah anda akan menghapus seluruh item belanjaan anda di keranjang?';
    Widget _actions =
        Row(mainAxisAlignment: MainAxisAlignment.center, children: <TextButton>[
      TextButton(
          onPressed: () {
            Navigator.pop(context, false);
          },
          child: Text('Tidak')),
      TextButton(
          onPressed: () {
            Navigator.pop(context, true);
          },
          child: Text('Ya, lanjutkan !'))
    ]);

    return CustomAlert.textComponent<bool?>(context,
        title: _title, subTitle: _subTitle, actions: _actions);
  }

  static Future<Map<String, dynamic>?> getJenisOngkir() async {
    String cartCode = await LocalStorage.getCartCode();
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String latDelivery = savedADJD['latitude'];
    String longDelivery = savedADJD['longitude'];

    String _params = await HTTPRequest.getQueryString(
        params:
            'cartCode=$cartCode&latDelivery=$latDelivery&longDelivery=$longDelivery');

    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Transaksi/getJenisOngkir?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      return dataObject;
    }
    return null;
  }

  static Future<dynamic> getWalletConfiguration() async {
    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Pengaturan/getWalletConfiguration');
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);

      return dataObject['walletConfiguration'];
    }

    return false;
  }

  static Future<int> getTotalKeranjang() async {
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String cartCode = await LocalStorage.getCartCode();
    String latDelivery = savedADJD['latitude'];
    String longDelivery = savedADJD['longitude'];

    String _params = await HTTPRequest.getQueryString(
        params:
            'cartCode=$cartCode&latDelivery=$latDelivery&longDelivery=$longDelivery');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getTotalKeranjang?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);

      return dataObject['totalKeranjang'];
    }

    return 0;
  }

  static Future<int> getTotalOngkir() async {
    String cartCode = await LocalStorage.getCartCode();
    Map<String, dynamic> savedADJD =
        await LocalStorage.getSavedLatitudeAndLongitude();

    String latDelivery = savedADJD['latitude'];
    String longDelivery = savedADJD['longitude'];

    String _params = await HTTPRequest.getQueryString(
        params:
            'cartCode=$cartCode&latDelivery=$latDelivery&longDelivery=$longDelivery');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getTotalOngkir?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);

      return dataObject['totalOngkir'];
    }

    return 0;
  }

  static Future<int> getJumlahCart() async {
    String cartCode = await LocalStorage.getCartCode();
    String _params =
        await HTTPRequest.getQueryString(params: 'cartCode=$cartCode');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getJumlahCart?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      dynamic dataObject = jsonDecode(httpResponse.body);
      int jumlahKeranjang = dataObject['jumlahKeranjang'];

      return jumlahKeranjang;
    }
    return 0;
  }

  static Future<dynamic> getDiskonKategori() async {
    String _cartCode = await LocalStorage.getCartCode();
    String _params =
        await HTTPRequest.getQueryString(params: 'cartCode=$_cartCode');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/getDiskonKategori?' + _params);
    Response _httpResponse = await get(_url);
    if (_httpResponse.statusCode == 200) {
      Map<String, dynamic> _dataObject = jsonDecode(_httpResponse.body);
      dynamic _diskonKategori = _dataObject['diskonKategori'];
      return _diskonKategori;
    }
    return null;
  }

  static Future<bool> isJadwalDeliveryOpened() async {
    SharedPreferences _sp = await SharedPreferences.getInstance();
    String? _encodedJadwalDelivery = _sp.getString('savedJadwalDeliveryMap');
    if (_encodedJadwalDelivery != null) {
      Map<String, dynamic> _decodedJadwalDelivery =
          jsonDecode(_encodedJadwalDelivery);

      String _kode = _decodedJadwalDelivery['kode'];
      String _day = _decodedJadwalDelivery['day'];
      String _setOn = _decodedJadwalDelivery['setOn'];

      Uri _url = Uri.parse(
          ServerPath.apiBaseURL + '/JadwalDelivery/isJadwalDeliveryOpened');

      Map<String, dynamic> _body = {
        'kode': _kode,
        'day': _day,
        'setOn': _setOn
      };
      Response _httpResponse = await post(_url, body: _body);
      if (_httpResponse.statusCode == 200) {
        Map<String, dynamic> _dataObject = jsonDecode(_httpResponse.body);
        bool _isJadwakDeliveryOpened = _dataObject['isJadwalDeliveryOpened'];
        return _isJadwakDeliveryOpened;
      }
    }

    return true;
  }
}
