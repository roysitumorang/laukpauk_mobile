import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/Keranjang.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/views/pembeli/components/Resep/ResepItemItem.dart';
import 'package:laukpauk/models/ResepModel.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

class ResepItem extends StatefulWidget {
  final String idResep;
  ResepItem(this.idResep);

  _ResepItemState createState() => _ResepItemState();
}

class _ResepItemState extends State<ResepItem> {
  Map<String, dynamic> selectedItems = {};
  bool isBusy = false;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('Bahan - bahan yang diperlukan : ',
          style: TextStyle(
              fontSize: SizeConfig.horizontalBlock * 4,
              fontWeight: FontWeight.w700)),
      SizedBox(height: SizeConfig.horizontalBlock * 5.0),
      FutureBuilder<List>(
          future: ResepModel.getResepItem(widget.idResep),
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              if (snapshot.data != null) {
                List resepItem = snapshot.data!;

                if (resepItem.length >= 1) {
                  int indexData = 0;

                  List<Widget> listResepItemWidget = resepItem.map((item) {
                    bool isThisItemSelected =
                        selectedItems.containsKey(item['id']);

                    indexData++;

                    return ResepItemItem(
                        indexData,
                        item,
                        (selectedItemIDAndQuantity) {
                          Map<String, dynamic> decodedSatuan =
                              jsonDecode(selectedItemIDAndQuantity['satuan']);

                          String idItem =
                              selectedItemIDAndQuantity['idItem'].toString();
                          String idProduk =
                              selectedItemIDAndQuantity['idProduk'].toString();
                          String quantity =
                              selectedItemIDAndQuantity['quantity'].toString();
                          String idSatuan = decodedSatuan['id'].toString();
                          int subTotal = selectedItemIDAndQuantity['subTotal'];

                          Map<String, dynamic> selectedItemsLocal =
                              selectedItems;

                          if (selectedItemsLocal.containsKey(idItem)) {
                            Map<String, dynamic> data =
                                selectedItemsLocal[idItem];
                            data['idProduk'] = idProduk;
                            data['quantity'] = quantity;
                            data['idSatuan'] = idSatuan;
                            data['subTotal'] = subTotal;

                            selectedItemsLocal[idItem] = data;
                          } else {
                            selectedItemsLocal[idItem] = {
                              'idProduk': idProduk,
                              'quantity': quantity,
                              'idSatuan': idSatuan,
                              'subTotal': subTotal
                            };
                          }

                          setState(() {
                            selectedItems = selectedItemsLocal;
                          });
                        },
                        (canceledItemIDAndQuantity) {
                          String idItem =
                              canceledItemIDAndQuantity['idItem'].toString();
                          selectedItems.remove(idItem);

                          setState(() {
                            selectedItems = selectedItems;
                          });
                        },
                        (itemIDAndQuantity) {
                          String idItem =
                              itemIDAndQuantity['idItem'].toString();
                          String quantity =
                              itemIDAndQuantity['quantity'].toString();
                          int subTotal = itemIDAndQuantity['subTotal'];

                          Map<String, dynamic> decodedSatuan =
                              jsonDecode(itemIDAndQuantity['satuan']);

                          selectedItems[idItem]['subTotal'] = subTotal;
                          selectedItems[idItem]['quantity'] = quantity;
                          selectedItems[idItem]['idSatuan'] =
                              decodedSatuan['id'].toString();

                          setState(() {
                            selectedItems = selectedItems;
                          });
                        },
                        isThisItemSelected: isThisItemSelected,
                        deteksiADJD: () async {
                          ADJDModel.deleteAlamatDanJadwalDelivery();
                          Map<String, dynamic> adjdSet =
                              await HeaderModel.adjd(context);

                          String? latitude = adjdSet['latitude'];
                          String? longitude = adjdSet['longitude'];
                          String? kodeJadwalDelivery =
                              adjdSet['kodeJadwalDelivery'];

                          bool isADJDCompleted = false;
                          if (latitude != null &&
                              longitude != null &&
                              kodeJadwalDelivery != null) {
                            isADJDCompleted = true;
                            Provider.of<HeaderViewModel>(context, listen: false)
                                .isADJDSet = isADJDCompleted;

                            SnackBar _snackBar = SnackBar(
                                action: SnackBarAction(
                                    label: 'OK',
                                    textColor: CustomColors.green,
                                    onPressed: () {
                                      ScaffoldMessenger.of(context)
                                          .hideCurrentSnackBar();
                                    }),
                                duration: Duration(seconds: 2),
                                content: Text(
                                    'Anda sudah bisa berbelanja resep, silahkan lanjutkan belanjanya ya mak!'));
                            ScaffoldMessenger.of(context)
                                .showSnackBar(_snackBar);
                          }
                        });
                  }).toList();

                  bool isBahanCartOK = (selectedItems.length >= 1);
                  int totalBelanja = 0;
                  selectedItems.forEach((key, value) {
                    totalBelanja += value['subTotal'] as int;
                  });

                  return Container(
                      padding: EdgeInsets.symmetric(horizontal: 15.0),
                      child: Column(children: [
                        ...listResepItemWidget,
                        (isBahanCartOK) ? SizedBox(height: 10) : SizedBox(),
                        (isBahanCartOK)
                            ? Padding(
                                padding: EdgeInsets.only(bottom: 15.0),
                                child: Row(
                                    mainAxisAlignment:
                                        MainAxisAlignment.spaceBetween,
                                    children: [
                                      Text('Total Belanja',
                                          style: TextStyle(
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      4)),
                                      Text(
                                          Currency.formatRupiah(
                                              totalBelanja, 0),
                                          style: TextStyle(
                                              color: CustomColors.green,
                                              fontWeight: FontWeight.w500,
                                              fontSize:
                                                  SizeConfig.horizontalBlock *
                                                      4))
                                    ]))
                            : SizedBox(),
                        (isBahanCartOK)
                            ? CustomButton.button(context, 'Belanja Resep',
                                () async {
                                String cartCode =
                                    await LocalStorage.getCartCode();

                                Map<String, dynamic>? jadwalDeliveryTerpilih =
                                    await LocalStorage.savedJadwalDeliveryMap();
                                Map<String, dynamic> savedADJD =
                                    await LocalStorage
                                        .getSavedLatitudeAndLongitude();

                                String _dayDelivery =
                                    (jadwalDeliveryTerpilih != null)
                                        ? jadwalDeliveryTerpilih['day']
                                        : 'Hari Pengataran';
                                String _jadwalDeliveryCode =
                                    savedADJD['jadwalDeliveryCode'];

                                Map<String, dynamic> httpBody = {
                                  'cartCode': cartCode,
                                  'selectedItems': jsonEncode(selectedItems),
                                  'dayDelivery': _dayDelivery,
                                  'jadwalDeliveryCode': _jadwalDeliveryCode
                                };
                                bool statusAddToCart =
                                    await ResepModel.belanjaResep(
                                        context, httpBody);

                                if (statusAddToCart) {
                                  int jumlahKeranjang =
                                      await HeaderModel.getJumlahCart();
                                  Provider.of<HeaderViewModel>(context,
                                          listen: false)
                                      .jumlahCart = jumlahKeranjang;

                                  SnackBar snackBar = SnackBar(
                                      action: SnackBarAction(
                                          label: 'Lihat Keranjang',
                                          textColor: CustomColors.green,
                                          onPressed: () {
                                            Navigator.push(
                                                context,
                                                MaterialPageRoute(
                                                    builder: (_) =>
                                                        Keranjang()));
                                          }),
                                      content: Text(
                                          'Berhasil menambah resep item ke keranjang belanja !'),
                                      duration: Duration(seconds: 2));
                                  // Scaffold.of(context).showSnackBar(snackBar);
                                  ScaffoldMessenger.of(context)
                                      .showSnackBar(snackBar);
                                }
                              },
                                color: CustomButton.themeColor,
                                isBlock: true,
                                isBusy: isBusy)
                            : SizedBox()
                      ]));
                } else {
                  return Container(
                    width: double.infinity,
                    padding: EdgeInsets.symmetric(vertical: 15),
                    child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 30),
                          SizedBox(height: 5),
                          Text('Bahan bahan yang diperlukan tidak ada',
                              style: TextStyle(
                                  color: CustomColors.textDanger,
                                  fontSize: SizeConfig.horizontalBlock * 3))
                        ]),
                  );
                }
              }
            }
            return Center(
                child: SizedBox(
                    width: 15,
                    height: 15,
                    child: CircularProgressIndicator(strokeWidth: 1.5)));
          }),
    ]);
  }
}
