import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:laukpauk/views/pembeli/components/Keranjang/ProductPenjual.dart';
import 'package:laukpauk/views/pembeli/ProdukDiDalamPasar.dart';
import 'package:laukpauk/views/Penjual.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class PenjualCard extends StatelessWidget {
  final Map dataKeranjang;
  final Function onRemoveItemSuccessed;
  final Function onEditItemSuccessed;

  PenjualCard(this.dataKeranjang,
      {required this.onRemoveItemSuccessed, required this.onEditItemSuccessed});

  final List<Widget> cartProduct = [];

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    dataKeranjang['produkPenjual'].forEach((produk) {
      cartProduct
          .add(ProductPenjual(produk, onRemoveItemSuccessed: (removedItemID) {
        onRemoveItemSuccessed(removedItemID);
      }, onEditItemSuccessed: () {
        onEditItemSuccessed();
      }));
    });

    String lokasiPasar = (dataKeranjang['lokasiPasar'] != null)
        ? dataKeranjang['lokasiPasar']
        : 'Pasar Tanpa Identitas';

    String namaPenjual = (dataKeranjang['namaPenjual'] != null)
        ? dataKeranjang['namaPenjual']
        : 'Penjual Tidak Dikenal';

    return Card(
      shadowColor: Color.fromRGBO(0, 0, 0, 0.5),
      elevation: 5,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: Container(
          padding: EdgeInsets.all(15),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Padding(
              padding: EdgeInsets.only(bottom: 10),
              child: Row(
                children: [
                  Icon(FontAwesomeIcons.mapMarkerAlt, size: 17),
                  SizedBox(width: 10),
                  Expanded(
                      child: Row(children: [
                    GestureDetector(
                      onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) =>
                                  Penjual(id: dataKeranjang['idPenjual']))),
                      child: Text(namaPenjual,
                          style: TextStyle(
                              color: CustomColors.green,
                              fontWeight: FontWeight.w500)),
                    ),
                    Text(' di ',
                        style: TextStyle(color: CustomColors.nearlyBlack)),
                    GestureDetector(
                      onTap: () => Navigator.push(
                          context,
                          MaterialPageRoute(
                              builder: (_) => ProdukDiDalamPasar(
                                  idPasar: dataKeranjang['pasarPenjual'],
                                  namaPasar: lokasiPasar))),
                      child: Text(lokasiPasar,
                          style: TextStyle(
                              color: CustomColors.textInfo,
                              fontWeight: FontWeight.w500)),
                    )
                  ]))
                ],
              ),
            ),
            Column(
              children: cartProduct,
            )
          ])),
    );
  }
}
