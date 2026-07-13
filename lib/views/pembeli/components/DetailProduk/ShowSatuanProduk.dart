import 'package:flutter/material.dart';
import 'package:laukpauk/libraries/CustomText.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/views/pembeli/components/DetailProduk/PilihanSatuanProduk.dart';
import 'package:laukpauk/models/ProdukModel.dart';

class ShowSatuanProduk extends StatefulWidget {
  final bool? isPenjualAvailable, isThisProductInCAVariable;
  final dynamic dataProduk;
  final String idProduk;
  final String ongkirPerKMString;
  final void Function(Map<String, dynamic>? statusAddCart) addCartCallback;
  ShowSatuanProduk(
      {required this.idProduk,
      required this.isPenjualAvailable,
      required this.isThisProductInCAVariable,
      required this.dataProduk,
      required this.ongkirPerKMString,
      required this.addCartCallback});
  _ShowSatuanProdukState createState() => _ShowSatuanProdukState();
}

class _ShowSatuanProdukState extends State<ShowSatuanProduk> {
  Map<String, dynamic>? _satuanAndQuantitySelectedState;

  Widget build(BuildContext context) {
    String idProduk = widget.idProduk;
    bool isPenjualAvailable = widget.isPenjualAvailable!;
    bool isThisProductInCAVariable = widget.isThisProductInCAVariable!;
    dynamic dataProduk = widget.dataProduk;

    String fotoProdukURL = ServerPath.baseURL +
        '/' +
        ServerPath.uploadGambarProduk +
        '/compress/' +
        dataProduk['foto'];

    String namaKategoriProduk = (dataProduk['namaKategoriProduk'] != null)
        ? dataProduk['namaKategoriProduk']
        : 'Kategori Produk Tidak Diketahui';

    String ongkirPerKMString = widget.ongkirPerKMString;

    bool _isStockAvailable = true;
    if (dataProduk.containsKey('stokAvailable')) {
      _isStockAvailable = dataProduk['stokAvailable'] == '1';
    }

    String idSatuan = '';
    String quantity = '1';

    String? _idPromo;

    if (_satuanAndQuantitySelectedState != null) {
      idSatuan = _satuanAndQuantitySelectedState!['idSatuan'];
      quantity = _satuanAndQuantitySelectedState!['quantity'];

      if (_satuanAndQuantitySelectedState!.containsKey('isPromo')) {
        idSatuan = _satuanAndQuantitySelectedState!['id'];
        _idPromo = _satuanAndQuantitySelectedState!['idPromo'];
      }
    }

    return Padding(
      padding: EdgeInsets.symmetric(
          horizontal: SizeConfig.horizontalBlock * 3.0,
          vertical: SizeConfig.horizontalBlock * 5.0),
      child: SingleChildScrollView(
        child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 7.5),
                child: Text('Pilihan Satuan',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 4,
                        fontWeight: FontWeight.w700)),
              ),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 7.5),
                child: Text(
                    'Silahkan pilih satuan dan quantity yang anda mau : ',
                    style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
              ),
              SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: 7.5),
                      child: Row(children: [
                        ClipRRect(
                            borderRadius: BorderRadius.circular(50),
                            child: Container(
                                width: SizeConfig.horizontalBlock * 15,
                                height: SizeConfig.horizontalBlock * 15,
                                decoration: BoxDecoration(
                                    image: DecorationImage(
                                        image: NetworkImage(fotoProdukURL),
                                        fit: BoxFit.cover)))),
                        SizedBox(width: 15),
                        Expanded(
                          child: Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(dataProduk['nama'],
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 4,
                                        fontWeight: FontWeight.w600,
                                        color: CustomColors.green)),
                                SizedBox(height: 3),
                                Text(namaKategoriProduk,
                                    style: TextStyle(
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3,
                                        color: CustomColors.textMuted))
                              ],
                              mainAxisAlignment: MainAxisAlignment.start),
                        )
                      ]),
                    ),
                  ),
                  (_satuanAndQuantitySelectedState != null)
                      ? CustomButton.button(context, 'Beli', () async {
                          Map<String, dynamic>? _response =
                              await ProdukModel.addToCart(
                                  idProduk, idSatuan, quantity,
                                  idPromo: _idPromo);

                          widget.addCartCallback(_response);
                        },
                          isPill: true,
                          color: CustomButton.themeColor,
                          size: CustomButton.smSize)
                      : SizedBox()
                ],
              ),
              SizedBox(height: 5.0),
              (!isThisProductInCAVariable)
                  ? Container(
                      margin: EdgeInsets.symmetric(vertical: 5.0),
                      padding: EdgeInsets.symmetric(
                          vertical: 10.0, horizontal: 15.0),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Note!',
                              style: TextStyle(
                                  color: CustomColors.textDanger,
                                  fontWeight: FontWeight.w500,
                                  fontSize: SizeConfig.horizontalBlock * 3.5)),
                          SizedBox(height: 5.0),
                          Text(
                              'Produk ini berada diluar Coverage Area Anda. Anda akan dikenakan ongkir $ongkirPerKMString/KM. Disarankan belanja dari 1 pasar di terdekat lokasi Anda',
                              style: CustomText.textXS()
                                  .copyWith(fontStyle: FontStyle.italic))
                        ],
                      ),
                      decoration: BoxDecoration(
                          color: Color(0x55dc3545),
                          borderRadius: BorderRadius.circular(10.0)))
                  : SizedBox(),
              (!isPenjualAvailable)
                  ? Container(
                      margin: EdgeInsets.symmetric(vertical: 5.0),
                      padding: EdgeInsets.symmetric(
                          vertical: 10.0, horizontal: 15.0),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Pemberitahuan',
                              style: TextStyle(
                                  color: CustomColors.nearlyBlack,
                                  fontWeight: FontWeight.w500,
                                  fontSize: SizeConfig.horizontalBlock * 3.5)),
                          SizedBox(height: 5.0),
                          Text(
                              'Penjual dari produk ini tidak tersedia dalam jam pengantaran yang anda pilih!',
                              style: CustomText.textXS()
                                  .copyWith(fontStyle: FontStyle.italic))
                        ],
                      ),
                      decoration: BoxDecoration(
                          color: Color(0x55FFc107),
                          borderRadius: BorderRadius.circular(10.0)))
                  : SizedBox(),
              (isPenjualAvailable) ? SizedBox(height: 5.0) : SizedBox(),
              (isPenjualAvailable)
                  ? (_isStockAvailable)
                      ? PilihanSatuanProduk(idProduk,
                          onSatuanAndQuantitySelected:
                              (satuanAndQuantitySelected) {
                          setState(() {
                            _satuanAndQuantitySelectedState =
                                satuanAndQuantitySelected;
                          });
                        })
                      : Padding(
                          padding: const EdgeInsets.only(top: 5.0),
                          child: Text('Stok tidak tersedia',
                              style: TextStyle(
                                  color: CustomColors.textDanger,
                                  fontSize: SizeConfig.horizontalBlock * 2.75)),
                        )
                  : SizedBox()
            ]),
      ),
    );
  }
}
