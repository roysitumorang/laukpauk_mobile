import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/global-components/Yuhu.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/views/Penjual.dart';
import 'package:laukpauk/views/pembeli/components/DetailProduk/PaketPenjualLainnya.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/PaketModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/ProdukModel.dart';

class DetailPaket extends StatefulWidget {
  final String idPaket;
  DetailPaket(this.idPaket);

  DetailPaketState createState() => DetailPaketState();
}

class DetailPaketState extends State<DetailPaket> {
  String currentUserIDState = '';

  void initialization() async {
    String currentUserID = await LocalStorage.getCurrentUserID();
    setState(() {
      currentUserIDState = currentUserID;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idPaket = widget.idPaket;

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        body: Column(mainAxisAlignment: MainAxisAlignment.start, children: [
          Header(),
          Expanded(
              child: FutureBuilder(
                  future: PaketModel.detailPaket(idPaket),
                  builder: (context, snapshot) {
                    if (snapshot.hasData) {
                      dynamic data = snapshot.data;
                      if (data != false) {
                        Map<String, dynamic> detailPaket = data;

                        String idPenjual = detailPaket['idPenjual'];
                        String foto = detailPaket['foto'];
                        String fotoLC = foto.toLowerCase();
                        String nama = detailPaket['nama'];
                        int harga = int.parse(detailPaket['harga']);
                        int diskon = int.parse(detailPaket['diskon']);
                        String keterangan =
                            (detailPaket['keterangan'] != null &&
                                    detailPaket['keterangan'].length >= 1)
                                ? detailPaket['keterangan']
                                : 'Deskripsi Produk Tidak Ada';
                        String namaPenjual = detailPaket['namaToko'];
                        bool isItMyProduct = (currentUserIDState == idPenjual);
                        int hargaBersih = harga - diskon;
                        double persentaseDiskon = 0;
                        if (diskon >= 1 && harga >= 1) {
                          persentaseDiskon = diskon / harga * 100;
                        }

                        String defaultPaketImg = DefaultImage.defaultPaket;
                        String defaultPaketImgLC =
                            defaultPaketImg.toLowerCase();

                        bool isDefaultImage = defaultPaketImgLC == fotoLC;

                        return SingleChildScrollView(
                          child: Column(
                              mainAxisAlignment: MainAxisAlignment.start,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  width: SizeConfig.horizontalBlock * 100,
                                  height: SizeConfig.horizontalBlock * 60,
                                  decoration: BoxDecoration(
                                      image: DecorationImage(
                                          fit: (isDefaultImage)
                                              ? BoxFit.contain
                                              : BoxFit.cover,
                                          image: NetworkImage(
                                              ServerPath.baseURL +
                                                  '/' +
                                                  ServerPath.uploadGambarPaket +
                                                  '/' +
                                                  foto))),
                                ),
                                Padding(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 2.5, vertical: 5),
                                    child: Card(
                                      shape: RoundedRectangleBorder(
                                          borderRadius:
                                              BorderRadius.circular(10)),
                                      child: Padding(
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 15, vertical: 15),
                                        child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Row(
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.start,
                                                mainAxisAlignment:
                                                    MainAxisAlignment
                                                        .spaceBetween,
                                                children: [
                                                  Expanded(
                                                    child: Column(
                                                      crossAxisAlignment:
                                                          CrossAxisAlignment
                                                              .start,
                                                      children: [
                                                        Text(nama.toUpperCase(),
                                                            style: TextStyle(
                                                                color:
                                                                    CustomColors
                                                                        .green,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    4.5,
                                                                fontWeight:
                                                                    FontWeight
                                                                        .w700)),
                                                        SizedBox(height: 5),
                                                        (persentaseDiskon >=
                                                                0.1)
                                                            ? Row(
                                                                mainAxisAlignment:
                                                                    MainAxisAlignment
                                                                        .start,
                                                                children: [
                                                                  Text(
                                                                      Currency.formatRupiah(
                                                                          harga,
                                                                          0),
                                                                      style: TextStyle(
                                                                          decoration: TextDecoration
                                                                              .lineThrough,
                                                                          fontSize: SizeConfig.horizontalBlock *
                                                                              3.5,
                                                                          fontWeight: FontWeight
                                                                              .w700,
                                                                          color:
                                                                              CustomColors.textMuted)),
                                                                  Padding(
                                                                      padding: EdgeInsets.only(
                                                                          left:
                                                                              10.0),
                                                                      child: Container(
                                                                          child: Text(
                                                                              'Diskon ' + persentaseDiskon.toStringAsFixed(1) + ' %',
                                                                              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)),
                                                                          padding: EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                                                                          decoration: BoxDecoration(color: Colors.amber, borderRadius: BorderRadius.circular(50))))
                                                                ],
                                                              )
                                                            : SizedBox(),
                                                        Text(
                                                            Currency
                                                                .formatRupiah(
                                                                    hargaBersih,
                                                                    0),
                                                            style: TextStyle(
                                                                fontWeight:
                                                                    FontWeight
                                                                        .w700,
                                                                color:
                                                                    CustomColors
                                                                        .green,
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    4.0)),
                                                        SizedBox(height: 15),
                                                        Row(
                                                            mainAxisAlignment:
                                                                MainAxisAlignment
                                                                    .start,
                                                            children: [
                                                              Image.asset(
                                                                  ClientPath
                                                                          .assetsImg +
                                                                      '/store.png',
                                                                  width: SizeConfig
                                                                          .horizontalBlock *
                                                                      7.5),
                                                              SizedBox(
                                                                  width: 10),
                                                              Expanded(
                                                                child: Column(
                                                                    crossAxisAlignment:
                                                                        CrossAxisAlignment
                                                                            .start,
                                                                    children: [
                                                                      SingleChildScrollView(
                                                                        scrollDirection:
                                                                            Axis.horizontal,
                                                                        child: Row(
                                                                            mainAxisAlignment:
                                                                                MainAxisAlignment.start,
                                                                            children: [
                                                                              Text('Penjual', style: TextStyle(color: CustomColors.textMuted, fontSize: SizeConfig.horizontalBlock * 3.5)),
                                                                              SizedBox(width: 5),
                                                                              InkWell(
                                                                                onTap: () {
                                                                                  Navigator.push(context, MaterialPageRoute(builder: (_) => Penjual(id: detailPaket['idPenjual'])));
                                                                                },
                                                                                child: Text((namaPenjual.length >= 1) ? namaPenjual : 'Penjual Tidak Diketahui', style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5, color: CustomColors.textInfo, fontWeight: FontWeight.w600)),
                                                                              )
                                                                            ]),
                                                                      ),
                                                                      Text(
                                                                          'Klik nama penjual untuk melihat seluruh produk penjual',
                                                                          style:
                                                                              TextStyle(fontSize: SizeConfig.horizontalBlock * 2.5)),
                                                                    ]),
                                                              )
                                                            ])
                                                      ],
                                                    ),
                                                  ),
                                                  SizedBox(width: 10),
                                                  CustomButton.button(
                                                      context,
                                                      (!isItMyProduct)
                                                          ? 'Beli'
                                                          : 'Edit', () async {
                                                    if (!isItMyProduct) {
                                                      bool isADJDSet =
                                                          await LocalStorage
                                                              .isAlamatDeliveryDanJadwalDeliverySet();
                                                      if (isADJDSet) {
                                                        bool statusAddToCart =
                                                            await PaketModel
                                                                .addToCart(
                                                                    idPaket);
                                                        if (statusAddToCart) {
                                                          String _snackbarText =
                                                              (statusAddToCart)
                                                                  ? 'Berhasil menambahkan paket ke dalam keranjang'
                                                                  : 'Gagal menghapus produk paket. Silahkan coba lagi !';

                                                          SnackBar
                                                              _addToCartSnackbar =
                                                              PaketModel.addToCartSnackBar(
                                                                  context:
                                                                      context,
                                                                  text:
                                                                      _snackbarText);
                                                          ScaffoldMessenger.of(
                                                                  context)
                                                              .showSnackBar(
                                                                  _addToCartSnackbar);

                                                          int jumlahKeranjang =
                                                              await HeaderModel
                                                                  .getJumlahCart();
                                                          Provider.of<HeaderViewModel>(
                                                                      context,
                                                                      listen: false)
                                                                  .jumlahCart =
                                                              jumlahKeranjang;
                                                        }
                                                      } else {
                                                        bool? tentukanADJD =
                                                            await ProdukModel
                                                                .adjdNotSetAlert(
                                                                    context);
                                                        if (tentukanADJD !=
                                                                null &&
                                                            tentukanADJD) {
                                                          Map<String, dynamic>
                                                              adjdSet =
                                                              await HeaderModel
                                                                  .adjd(
                                                                      context);
                                                          String? latitude =
                                                              adjdSet[
                                                                  'latitude'];
                                                          String? longitude =
                                                              adjdSet[
                                                                  'longitude'];
                                                          String?
                                                              jadwalDeliveryCode =
                                                              adjdSet[
                                                                  'kodeJadwalDelivery'];

                                                          bool isADJDCompleted =
                                                              false;
                                                          if (latitude !=
                                                                  null &&
                                                              longitude !=
                                                                  null &&
                                                              jadwalDeliveryCode !=
                                                                  null) {
                                                            isADJDCompleted =
                                                                true;
                                                            bool
                                                                statusAddToCart =
                                                                await PaketModel
                                                                    .addToCart(
                                                                        idPaket);

                                                            String text =
                                                                (statusAddToCart)
                                                                    ? 'Produk berhasil ditambahkan ke keranjang !'
                                                                    : 'Produk gagal ditambahkan ke keranjang, silahkan coba lagi.';

                                                            SnackBar snackBar = SnackBar(
                                                                duration:
                                                                    Duration(
                                                                        seconds:
                                                                            1),
                                                                content: Text(
                                                                    text,
                                                                    style: TextStyle(
                                                                        fontSize:
                                                                            SizeConfig.horizontalBlock *
                                                                                3.25)));
                                                            // Scaffold.of(context)
                                                            //     .showSnackBar(
                                                            //         snackBar);

                                                            ScaffoldMessenger
                                                                    .of(context)
                                                                .showSnackBar(
                                                                    snackBar);

                                                            int jumlahKeranjang =
                                                                await HeaderModel
                                                                    .getJumlahCart();
                                                            Provider.of<HeaderViewModel>(
                                                                        context,
                                                                        listen:
                                                                            false)
                                                                    .jumlahCart =
                                                                jumlahKeranjang;
                                                            Provider.of<HeaderViewModel>(
                                                                        context,
                                                                        listen:
                                                                            false)
                                                                    .isADJDSet =
                                                                isADJDCompleted;
                                                          }
                                                        }
                                                      }
                                                    } else {
                                                      bool? statusEditPaket =
                                                          await Navigator.push(
                                                              context,
                                                              MaterialPageRoute(
                                                                  builder: (_) =>
                                                                      TambahProduk(
                                                                          idPaket,
                                                                          'paket')));
                                                      if (statusEditPaket !=
                                                          null) {
                                                        String text = (statusEditPaket)
                                                            ? 'Berhasil mengubah data paket'
                                                            : 'Gagal mengubah data paket !';
                                                        SnackBar snackBar =
                                                            SnackBar(
                                                                content:
                                                                    Text(text),
                                                                duration:
                                                                    Duration(
                                                                        seconds:
                                                                            2));

                                                        ScaffoldMessenger.of(
                                                                context)
                                                            .showSnackBar(
                                                                snackBar);
                                                        if (statusEditPaket) {
                                                          setState(() {});
                                                        }
                                                      }
                                                    }
                                                  },
                                                      color: (!isItMyProduct)
                                                          ? CustomButton
                                                              .themeColor
                                                          : CustomButton
                                                              .warningColor,
                                                      isPill: true)
                                                ]),
                                            Yuhu(),
                                            SizedBox(height: 15.0),
                                            Column(
                                                mainAxisAlignment:
                                                    MainAxisAlignment.start,
                                                crossAxisAlignment:
                                                    CrossAxisAlignment.start,
                                                children: [
                                                  Text('Deskripsi Paket',
                                                      style: TextStyle(
                                                          color: CustomColors
                                                              .textMuted,
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              4,
                                                          fontWeight:
                                                              FontWeight.w700)),
                                                  SizedBox(height: 5),
                                                  Text(keterangan,
                                                      style: TextStyle(
                                                          fontStyle: (detailPaket[
                                                                          'keterangan'] !=
                                                                      null &&
                                                                  detailPaket['keterangan']
                                                                          .length >=
                                                                      1)
                                                              ? null
                                                              : FontStyle
                                                                  .italic,
                                                          color: CustomColors
                                                              .textMuted,
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3))
                                                ])
                                          ],
                                        ),
                                      ),
                                    )),
                                Padding(
                                    padding: EdgeInsets.symmetric(
                                        horizontal: 15, vertical: 10),
                                    child: Column(
                                        mainAxisAlignment:
                                            MainAxisAlignment.start,
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text('Paket Lainnya',
                                              textAlign: TextAlign.left,
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.75,
                                                  fontWeight: FontWeight.w700)),
                                          SizedBox(height: 10),
                                          PaketPenjualLainnya(
                                              currentProduct: idPaket)
                                        ]))
                              ]),
                        );
                      } else {
                        return Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Image.asset(
                                ClientPath.assetsImg + '/empty.png',
                                width: 200,
                              ),
                              SizedBox(height: 20),
                              Text('Detail Produk Tidak Ditemukan',
                                  style: TextStyle(
                                      color: CustomColors.textDanger,
                                      fontSize: 16,
                                      fontWeight: FontWeight.w600))
                            ]);
                      }
                    } else {
                      return Center(
                          child: SizedBox(
                              width: 15,
                              height: 15,
                              child:
                                  CircularProgressIndicator(strokeWidth: 1.5)));
                    }
                  })),
        ]),
        bottomNavigationBar: Footer());
  }
}
