import 'package:flutter/material.dart';
import 'package:laukpauk/views/pembeli/Keranjang.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/models/ProdukModel.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';

class NamaProduk extends StatelessWidget {
  final Map<String, dynamic> detailProduk;
  final String currentUserIDString;
  final Function(bool? statusEditProduk) editProdukCallback;

  NamaProduk(
      this.detailProduk, this.currentUserIDString, this.editProdukCallback);

  Widget build(BuildContext context) {
    String namaProduk = detailProduk['namaProduk'];
    String namaKategoriProduk = detailProduk['namaKategoriProduk'];
    String idProduk = detailProduk['idProduk'];
    String idPenjual = detailProduk['idPenjual'];

    String idSatuan = detailProduk['idSatuan'];

    String _idPromo = detailProduk['idPromo'];
    String statusPromo = detailProduk['statusPromo'];
    String statusPromoLC = statusPromo.toLowerCase();

    bool isItMyProduct = idPenjual == currentUserIDString;

    return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(namaProduk.toUpperCase(),
                    style: TextStyle(
                        color: CustomColors.green,
                        fontSize: SizeConfig.horizontalBlock * 4.5,
                        fontWeight: FontWeight.w700)),
                SizedBox(height: 5),
                Text(namaKategoriProduk.toUpperCase(),
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3,
                        color: CustomColors.textMuted)),
                SizedBox(height: 15.0),
              ],
            ),
          ),
          SizedBox(width: 10),
          (statusPromoLC == 'aktif')
              ? CustomButton.button(context, (isItMyProduct) ? 'Edit' : 'Beli',
                  () async {
                  if (isItMyProduct) {
                    bool statusEditProduk = await Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) => TambahProduk(idProduk, 'satuan')));
                    editProdukCallback(statusEditProduk);
                  } else {
                    bool isADJDSet = await LocalStorage
                        .isAlamatDeliveryDanJadwalDeliverySet();
                    if (isADJDSet) {
                      Map<String, dynamic>? _response =
                          await ProdukModel.addToCart(idProduk, idSatuan, '1',
                              idPromo: _idPromo);

                      if (_response != null) {
                        bool? statusAddToCart = _response['statusAddCart'];

                        if (statusAddToCart != null) {
                          String text = (statusAddToCart)
                              ? 'Berhasil ditambahkan ke dalam keranjang !'
                              : (_response.containsKey('message'))
                                  ? _response['message']
                                  : 'Produk gagal masuk ke keranjang! Silahkan coba lagi!';

                          SnackBar snackBar = SnackBar(
                              duration: Duration(seconds: 1),
                              content: Text(text,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              action: SnackBarAction(
                                  label: 'Lihat Keranjang',
                                  textColor: CustomColors.green,
                                  onPressed: () => Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                          builder: (_) => Keranjang()))));
                          ScaffoldMessenger.of(context).showSnackBar(snackBar);

                          int jumlahKeranjang =
                              await HeaderModel.getJumlahCart();
                          Provider.of<HeaderViewModel>(context, listen: false)
                              .jumlahCart = jumlahKeranjang;
                        }
                      }
                    } else {
                      bool? tentukanADJD =
                          await ProdukModel.adjdNotSetAlert(context);
                      if (tentukanADJD != null && tentukanADJD) {
                        print('seharusnya tentukan lokasi');
                      }
                    }
                  }
                },
                  color: (!isItMyProduct)
                      ? CustomButton.themeColor
                      : CustomButton.warningColor,
                  isPill: true)
              : SizedBox()
        ]);
  }
}
