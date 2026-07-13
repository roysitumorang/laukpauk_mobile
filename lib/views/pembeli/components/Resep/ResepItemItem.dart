import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/ProdukModel.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

class ResepItemItem extends StatefulWidget {
  final Map<String, dynamic> resepItem;
  final int indexData;
  final void Function(Map<String, dynamic> idItemAndQuantityMap)
      selectedItemCallback;
  final void Function(Map<String, dynamic> idItemAndQuantityMap)
      cancelSelectedItemCallback;
  final void Function(Map<String, dynamic> idItemAndQuantityMap)
      quantityChangedCallback;
  final bool isThisItemSelected;
  final void Function() deteksiADJD;
  ResepItemItem(this.indexData, this.resepItem, this.selectedItemCallback,
      this.cancelSelectedItemCallback, this.quantityChangedCallback,
      {required this.deteksiADJD, this.isThisItemSelected = false});

  ResepItemItemState createState() => ResepItemItemState();
}

class ResepItemItemState extends State<ResepItemItem> {
  String satuanProdukResepSelected = '0';
  int quantitySelected = 1;

  Future<List> getSatuanProdukResep(String idProduk) async {
    String _params =
        await HTTPRequest.getQueryString(params: 'idProduk=$idProduk');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Produk/getHargaProduk?' + _params);
    Response httpResponse = await get(_url);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List satuanProdukResep = dataObject['hargaProduk'];
      return satuanProdukResep;
    }
    return [];
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String jenisHargaTerpilih = '', namaSatuanTerpilih = '';
    int hargaHargaTerpilih = 0, diskonHargaTerpilih = 0, hargaBersih = 0;
    if (satuanProdukResepSelected != '0') {
      Map<String, dynamic> satuanProdukResepSelectedMap =
          jsonDecode(satuanProdukResepSelected);
      jenisHargaTerpilih = satuanProdukResepSelectedMap['jenisHarga'];
      namaSatuanTerpilih = satuanProdukResepSelectedMap['nama'];
      hargaHargaTerpilih = int.parse(satuanProdukResepSelectedMap['harga']);
      diskonHargaTerpilih = int.parse(satuanProdukResepSelectedMap['diskon']);
    }

    hargaBersih = hargaHargaTerpilih - diskonHargaTerpilih;

    bool isSelected = widget.isThisItemSelected;

    return Container(
      margin: EdgeInsets.only(bottom: 10),
      padding: (isSelected) ? EdgeInsets.all(10) : null,
      decoration: BoxDecoration(
          borderRadius: (isSelected) ? BorderRadius.circular(10) : null,
          color: (isSelected) ? Color(0x3366CC99) : null),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(widget.indexData.toString() + '.',
              style: TextStyle(fontWeight: FontWeight.w700)),
          SizedBox(width: 5),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(widget.resepItem['namaProduk'].toUpperCase(),
                          style: TextStyle(
                              fontWeight: FontWeight.w600,
                              fontSize: SizeConfig.horizontalBlock * 3.5)),
                    ),
                    FutureBuilder<List>(
                        future:
                            getSatuanProdukResep(widget.resepItem['idProduk']),
                        builder: (context, snapshot) {
                          if (snapshot.hasData) {
                            if (snapshot.data != null) {
                              List satuanProdukResep = snapshot.data!;

                              if (satuanProdukResep.length >= 1) {
                                List<DropdownMenuItem<String>>
                                    satuanProdukResepWidget = [
                                  DropdownMenuItem(
                                      child: Text('Pilih Satuan',
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      3)),
                                      value: '0')
                                ];

                                satuanProdukResepWidget +=
                                    satuanProdukResep.map((satuan) {
                                  return DropdownMenuItem(
                                    value: jsonEncode(satuan),
                                    child: Text(satuan['nama'],
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3)),
                                  );
                                }).toList();

                                return Row(
                                  children: [
                                    DropdownButton<String>(
                                        isDense: true,
                                        underline: DropdownButtonHideUnderline(
                                            child: SizedBox()),
                                        dropdownColor: CustomColors.nearlyWhite,
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock * 3,
                                            color: CustomColors.nearlyBlack),
                                        value: satuanProdukResepSelected,
                                        items: satuanProdukResepWidget,
                                        onChanged: (selectedSatuan) {
                                          if (selectedSatuan != null) {
                                            Map<String, dynamic>
                                                selectedSatuanMap =
                                                jsonDecode(selectedSatuan);
                                            String jenisHarga =
                                                selectedSatuanMap['jenisHarga'];
                                            bool isHargaPas =
                                                (jenisHarga.toLowerCase() ==
                                                    'pas');
                                            int quantity = (isHargaPas)
                                                ? 1
                                                : quantitySelected;

                                            int harga = int.parse(
                                                selectedSatuanMap['harga']);
                                            int diskon = int.parse(
                                                selectedSatuanMap['diskon']);
                                            int hargaBersih = harga - diskon;
                                            int subTotal =
                                                hargaBersih * quantity;

                                            Map<String, dynamic> data = {
                                              'idItem': widget.resepItem['id'],
                                              'satuan': selectedSatuan,
                                              'idProduk':
                                                  widget.resepItem['idProduk'],
                                              'quantity': quantity,
                                              'subTotal': subTotal
                                            };
                                            widget.selectedItemCallback(data);
                                            setState(() {
                                              satuanProdukResepSelected =
                                                  selectedSatuan;
                                              if (isHargaPas) {
                                                quantitySelected = quantity;
                                              }
                                            });
                                          }
                                        }),
                                    (jenisHargaTerpilih.toLowerCase() ==
                                            'tidak pas')
                                        ? DropdownButton<int>(
                                            value: quantitySelected,
                                            isDense: true,
                                            underline:
                                                DropdownButtonHideUnderline(
                                                    child: SizedBox()),
                                            items: List.generate(
                                                    10, (index) => ++index)
                                                .map((quantity) => DropdownMenuItem(
                                                    value: quantity,
                                                    child: Text(
                                                        quantity.toString() +
                                                            ' Item',
                                                        style: TextStyle(
                                                            fontSize: SizeConfig
                                                                    .horizontalBlock *
                                                                3))))
                                                .toList(),
                                            onChanged: (quantity) {
                                              if (quantity != null) {
                                                Map<String, dynamic>
                                                    satuanProdukMap =
                                                    jsonDecode(
                                                        satuanProdukResepSelected);

                                                int harga = int.parse(
                                                    satuanProdukMap['harga']);
                                                int diskon = int.parse(
                                                    satuanProdukMap['diskon']);
                                                int hargaBersih =
                                                    harga - diskon;
                                                int subTotal =
                                                    hargaBersih * quantity;

                                                Map<String, dynamic> data = {
                                                  'idItem':
                                                      widget.resepItem['id'],
                                                  'satuan':
                                                      satuanProdukResepSelected,
                                                  'quantity': quantity,
                                                  'subTotal': subTotal
                                                };
                                                widget.quantityChangedCallback(
                                                    data);

                                                setState(() {
                                                  quantitySelected = quantity;
                                                });
                                              }
                                            })
                                        : SizedBox(),
                                    Padding(
                                      padding: EdgeInsets.only(left: 15),
                                      child: GestureDetector(
                                          onTap: () async {
                                            bool isADJDSet = await LocalStorage
                                                .isAlamatDeliveryDanJadwalDeliverySet();
                                            if (isADJDSet) {
                                              String satuanTerpilih =
                                                  satuanProdukResepSelected;

                                              int harga,
                                                  diskon,
                                                  hargaBersih,
                                                  subTotal;

                                              if (satuanProdukResepSelected ==
                                                  '0') {
                                                String encodedSatuan =
                                                    jsonEncode(
                                                        satuanProdukResep[0]);
                                                setState(() {
                                                  satuanProdukResepSelected =
                                                      encodedSatuan;
                                                });
                                                satuanTerpilih = encodedSatuan;

                                                harga = (satuanProdukResep[0]
                                                            ['harga'] !=
                                                        null)
                                                    ? int.parse(
                                                        satuanProdukResep[0]
                                                            ['harga'])
                                                    : 0;
                                                diskon = (satuanProdukResep[0]
                                                            ['diskon'] !=
                                                        null)
                                                    ? int.parse(
                                                        satuanProdukResep[0]
                                                            ['diskon'])
                                                    : 0;
                                              } else {
                                                Map<String, dynamic>
                                                    satuanTerpilihMap =
                                                    jsonDecode(satuanTerpilih);
                                                harga = int.parse(
                                                    satuanTerpilihMap['harga']);
                                                diskon = int.parse(
                                                    satuanTerpilihMap[
                                                        'diskon']);
                                              }

                                              hargaBersih = harga - diskon;
                                              subTotal = hargaBersih *
                                                  quantitySelected;

                                              Map<String, dynamic>
                                                  dataToManipulate = {
                                                'idItem':
                                                    widget.resepItem['id'],
                                                'satuan': satuanTerpilih,
                                                'idProduk': widget
                                                    .resepItem['idProduk'],
                                                'quantity': quantitySelected,
                                                'subTotal': subTotal
                                              };

                                              (!isSelected)
                                                  ? widget.selectedItemCallback(
                                                      dataToManipulate)
                                                  : widget
                                                      .cancelSelectedItemCallback(
                                                          dataToManipulate);
                                            } else {
                                              bool? lakukanDeteksiADJD =
                                                  await ProdukModel
                                                      .adjdNotSetAlert(context);
                                              if (lakukanDeteksiADJD != null &&
                                                  lakukanDeteksiADJD) {
                                                widget.deteksiADJD();
                                              }
                                            }
                                          },
                                          child: Icon(
                                              (!isSelected)
                                                  ? FontAwesomeIcons.cartPlus
                                                  : FontAwesomeIcons.trashAlt,
                                              size: SizeConfig.horizontalBlock *
                                                  4,
                                              color: (!isSelected)
                                                  ? CustomColors.green
                                                  : CustomColors.textDanger)),
                                    )
                                  ],
                                );
                              } else {
                                return Text('Tidak ada satuan',
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3,
                                        fontStyle: FontStyle.italic,
                                        color: CustomColors.textMuted));
                              }
                            }
                          }
                          return Center(
                              child: SizedBox(
                                  width: 10,
                                  height: 10,
                                  child: CircularProgressIndicator(
                                      strokeWidth: 1.5)));
                        })
                  ],
                ),
                Text('Diperlukan ' + widget.resepItem['takaran'],
                    style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
                (isSelected)
                    ? Padding(
                        padding: EdgeInsets.only(top: 5.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                                'Diorder ' +
                                    Currency.formatSatuan(quantitySelected,
                                        satuan: 'x ' +
                                            namaSatuanTerpilih +
                                            ' @ ' +
                                            Currency.formatRupiah(
                                                hargaBersih, 0)),
                                style: TextStyle(
                                    fontSize: SizeConfig.horizontalBlock * 3)),
                            SizedBox(height: 10.0),
                            Text(
                                'Sub Total ' +
                                    Currency.formatRupiah(
                                        hargaBersih * quantitySelected, 0),
                                style: TextStyle(
                                    color: CustomColors.textSuccess,
                                    fontSize: SizeConfig.horizontalBlock * 3.25,
                                    fontWeight: FontWeight.w500)),
                          ],
                        ),
                      )
                    : SizedBox()
              ],
            ),
          )
        ],
      ),
    );
  }
}
