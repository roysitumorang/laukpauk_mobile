import 'package:flutter/material.dart';

import 'package:laukpauk/views/pembeli/DetailPaket.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/PaketModel.dart';

class PaketPenjualCard extends StatefulWidget {
  final Map<String, dynamic> dataProduk;
  final void Function() deteksiADJD;
  final void Function() reload;
  PaketPenjualCard(this.dataProduk,
      {required this.deteksiADJD, required this.reload});

  @override
  _PaketPenjualCardState createState() => _PaketPenjualCardState();
}

class _PaketPenjualCardState extends State<PaketPenjualCard> {
  int? currentUserIDState;

  void initialization() async {
    String currentUserIDString = await LocalStorage.getCurrentUserID();
    int currentUserID = int.parse(currentUserIDString);
    setState(() {
      currentUserIDState = currentUserID;
    });
  }

  void initState() {
    super.initState();
    this.initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> dataPaket = widget.dataProduk;

    int idPenjual = int.parse(dataPaket['idPenjual']);
    bool produkPenjual = (currentUserIDState == idPenjual);

    String idPaket = dataPaket['id'];
    int hargaKotor = int.parse(dataPaket['harga']);

    int diskon = int.parse(dataPaket['diskon']);
    int hargaBersih = hargaKotor - diskon;
    double persentaseDiskon = diskon / hargaKotor * 100;
    String fotoPaket = dataPaket['foto'];

    bool isDefaultImage = (fotoPaket == DefaultImage.defaultPaket);

    return Container(
        padding: EdgeInsets.only(top: 5),
        child: InkWell(
          onTap: () {
            Navigator.push(context, MaterialPageRoute(builder: (_) {
              return DetailPaket(idPaket);
            }));
          },
          child: Card(
              margin: EdgeInsets.only(bottom: 10, left: 2.5, right: 2.5),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Container(
                decoration:
                    BoxDecoration(borderRadius: BorderRadius.circular(10)),
                child: Center(
                  child: Column(
                    children: [
                      Expanded(
                          child: LayoutBuilder(builder: (context, constraints) {
                        return Container(
                            constraints: BoxConstraints(
                                minHeight: constraints.maxHeight,
                                minWidth: constraints.maxWidth),
                            margin: EdgeInsets.only(bottom: 10),
                            decoration: BoxDecoration(
                                borderRadius: BorderRadius.only(
                                    topLeft: Radius.circular(10.0),
                                    topRight: Radius.circular(10.0)),
                                image: DecorationImage(
                                    image: NetworkImage(ServerPath.baseURL +
                                        '/' +
                                        ServerPath.uploadGambarPaket +
                                        '/compress/' +
                                        fotoPaket),
                                    fit: (isDefaultImage)
                                        ? BoxFit.contain
                                        : BoxFit.fill)));
                      })),
                      Expanded(
                        child: SingleChildScrollView(
                          child: Padding(
                            padding: EdgeInsets.symmetric(horizontal: 5.0),
                            child: Column(children: [
                              Text(dataPaket['nama'],
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5,
                                      fontWeight: FontWeight.w700),
                                  textAlign: TextAlign.center),
                              SizedBox(height: 5),
                              (persentaseDiskon > 0)
                                  ? SingleChildScrollView(
                                      scrollDirection: Axis.horizontal,
                                      child: Row(children: [
                                        Text(
                                            Currency.formatRupiah(
                                                hargaKotor, 0),
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3)),
                                        (diskon >= 1)
                                            ? Container(
                                                margin:
                                                    EdgeInsets.only(left: 5),
                                                child: Text(
                                                    '-' +
                                                        Currency.formatSatuan(
                                                            persentaseDiskon
                                                                .toInt(),
                                                            satuan: '%'),
                                                    style: TextStyle(
                                                        fontSize: SizeConfig
                                                                .horizontalBlock *
                                                            3)),
                                                padding: EdgeInsets.symmetric(
                                                    horizontal: 10,
                                                    vertical: 5),
                                                decoration: BoxDecoration(
                                                    color: Colors.amber,
                                                    borderRadius:
                                                        BorderRadius.circular(
                                                            50)))
                                            : SizedBox()
                                      ]),
                                    )
                                  : SizedBox(),
                              Container(
                                  margin: EdgeInsets.only(top: 5),
                                  child: Text(
                                      Currency.formatRupiah(hargaBersih, 0),
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5,
                                          color: CustomColors.green,
                                          fontWeight: FontWeight.w700))),
                              SizedBox(height: 7.5),
                              Padding(
                                padding: EdgeInsets.symmetric(horizontal: 10.0),
                                child: CustomButton.button(
                                    context, (produkPenjual) ? 'Edit' : 'Beli',
                                    () async {
                                  if (!produkPenjual) {
                                    bool isADJDSet = await LocalStorage
                                        .isAlamatDeliveryDanJadwalDeliverySet();
                                    if (isADJDSet) {
                                      bool statusAddToCart =
                                          await PaketModel.addToCart(idPaket);

                                      String _text = (statusAddToCart)
                                          ? 'Produk berhasil ditambahkan ke keranjang !'
                                          : 'Produk gagal ditambahkan ke keranjang, silahkan coba lagi.';

                                      SnackBar _snackBar =
                                          PaketModel.addToCartSnackBar(
                                              context: context, text: _text);
                                      ScaffoldMessenger.of(context)
                                          .showSnackBar(_snackBar);

                                      int jumlahKeranjang =
                                          await HeaderModel.getJumlahCart();
                                      Provider.of<HeaderViewModel>(context,
                                              listen: false)
                                          .jumlahCart = jumlahKeranjang;
                                    } else {
                                      bool? tentukanADJD =
                                          await PaketModel.adjdNotSetAlert(
                                              context);
                                      if (tentukanADJD != null &&
                                          tentukanADJD) {
                                        widget.deteksiADJD();
                                      }
                                    }
                                  } else {
                                    bool? statusEditPaket =
                                        await Navigator.push(
                                            context,
                                            MaterialPageRoute(
                                                builder: (_) => TambahProduk(
                                                    idPaket, 'paket')));
                                    if (statusEditPaket != null) {
                                      String text = (statusEditPaket)
                                          ? 'Berhasil mengubah data paket'
                                          : 'Gagal mengubah data paket !';
                                      SnackBar snackBar = SnackBar(
                                          content: Text(text),
                                          duration: Duration(seconds: 2));

                                      ScaffoldMessenger.of(context)
                                          .showSnackBar(snackBar);
                                      widget.reload();
                                    }
                                  }
                                },
                                    color: (produkPenjual)
                                        ? CustomButton.warningColor
                                        : CustomButton.themeColor,
                                    isBlock: true,
                                    isPill: true,
                                    size: CustomButton.xsSize),
                              )
                            ]),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              )),
        ));
  }
}
