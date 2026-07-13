import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/penjual/PesananItemTidakTersedia.dart';

class ProsesPesananItem extends StatefulWidget {
  final Map<String, dynamic> pesananItem;
  final Function(bool? returnedData) pesananItemCallback;
  ProsesPesananItem(this.pesananItem, this.pesananItemCallback);
  _ProsesPesananItemState createState() => _ProsesPesananItemState();
}

class _ProsesPesananItemState extends State<ProsesPesananItem> {
  Future<bool> pembatalanKlaimTidakAda() async {
    Map<String, dynamic> pesananItem = widget.pesananItem;
    Map<String, dynamic> httpBody = {'idItem': pesananItem['id']};

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL +
        '/Transaksi/pembatalanKlaimTidakAda?' +
        _params);

    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      return dataObject['statusPembatalanKlaimTidakTersedia'];
    }
    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> pesananItem = widget.pesananItem;

    int quantity = int.parse(pesananItem['quantity']);
    String jenisProduk = pesananItem['jenisProduk'];
    String? namaSatuan = (jenisProduk.toLowerCase() == 'paket')
        ? 'pcs'
        : pesananItem['namaSatuan'];
    namaSatuan = (namaSatuan != null) ? namaSatuan : 'Satuan Tidak Diketahui';
    int harga = int.parse(pesananItem['harga']);
    int diskon = int.parse(pesananItem['diskon']);
    String fotoProduk = pesananItem['fotoProduk'];
    String catatan = pesananItem['catatan'];
    String namaProduk = pesananItem['namaProduk'];
    String? status = pesananItem['status'];

    bool? isProdukAvailable;
    bool _isRejected = false;
    if (status != null) {
      String _statusLC = status.toLowerCase();
      _isRejected = _statusLC == 'rejected';

      isProdukAvailable = !_isRejected;
    }

    return Container(
        margin: EdgeInsets.only(bottom: 5),
        decoration: BoxDecoration(
            color: (_isRejected) ? Color(0x11dc3545) : CustomColors.nearlyWhite,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(
                width: 1.5,
                color: (_isRejected)
                    ? CustomColors.textDanger
                    : CustomColors.green)),
        padding: EdgeInsets.symmetric(vertical: 10),
        child: ListTile(
          leading: ClipRRect(
              borderRadius: BorderRadius.circular(50),
              child: Image.network(
                  ServerPath.baseURL +
                      '/' +
                      ServerPath.uploadGambarProduk +
                      '/compress/$fotoProduk',
                  width: SizeConfig.horizontalBlock * 13.5,
                  height: SizeConfig.horizontalBlock * 13.5)),
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.start,
            children: [
              Text(namaProduk,
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 3.75,
                      fontWeight: FontWeight.w700,
                      color: CustomColors.green)),
              Container(
                margin: EdgeInsets.only(bottom: 5.0, top: 2.5),
                padding: EdgeInsets.all(5.0),
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(10.0),
                    color: (isProdukAvailable != null)
                        ? (isProdukAvailable)
                            ? CustomColors.textInfo
                            : CustomColors.textDanger
                        : CustomColors.textMuted),
                child: Text(
                    (isProdukAvailable != null)
                        ? (isProdukAvailable)
                            ? 'Tersedia'
                            : 'Tidak Tersedia'
                        : 'Belum diperiksa',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 2.5,
                        color: CustomColors.nearlyWhite)),
              )
            ],
          ),
          subtitle:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(Currency.formatSatuan(quantity, satuan: 'x ' + namaSatuan)),
            SizedBox(height: 10),
            Text('Harga ' + Currency.formatRupiah(harga, 0),
                style: TextStyle(
                    color: CustomColors.darkGreen,
                    fontWeight: FontWeight.w600)),
            (diskon >= 1)
                ? Padding(
                    padding: EdgeInsets.only(top: 2.5),
                    child: Text('Diskon ' + Currency.formatRupiah(diskon, 0),
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 2.5,
                            color: CustomColors.textWarning)),
                  )
                : SizedBox(),
            (catatan.length >= 1)
                ? Padding(
                    padding: EdgeInsets.only(top: 5),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Catatan Tambahan',
                            style: TextStyle(fontWeight: FontWeight.w500)),
                        Text(catatan,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3,
                                color: CustomColors.textMuted)),
                      ],
                    ),
                  )
                : SizedBox()
          ]),
          trailing: InkWell(
            onTap: () async {
              if (_isRejected) {
                bool statusPembatalanKlaimTidakAda =
                    await pembatalanKlaimTidakAda();
                widget.pesananItemCallback(statusPembatalanKlaimTidakAda);
              } else {
                bool statusPesananTidakTersedia = await Navigator.push(
                    context,
                    MaterialPageRoute(
                        builder: (_) => PesananItemTidakTersedia(pesananItem)));
                widget.pesananItemCallback(statusPesananTidakTersedia);
              }
            },
            child: Icon(
                (_isRejected)
                    ? FontAwesomeIcons.checkCircle
                    : FontAwesomeIcons.minusCircle,
                size: SizeConfig.horizontalBlock * 4.5,
                color: (_isRejected)
                    ? CustomColors.green
                    : CustomColors.textDanger),
          ),
        ));
  }
}
