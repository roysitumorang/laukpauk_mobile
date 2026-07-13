import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/views/pembeli/components/RepeatOrder/RepeatOrderTransaksiItemPerPenjual.dart';
import 'package:laukpauk/models/ROModel.dart';
import 'package:laukpauk/views/pembeli/Keranjang.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

class RepeatOrderItemTransaksi extends StatefulWidget {
  final String idTransaksi;
  final Function onRefresh;
  RepeatOrderItemTransaksi(this.idTransaksi, this.onRefresh);

  _RepeatOrderItemTransaksiState createState() =>
      _RepeatOrderItemTransaksiState();
}

class _RepeatOrderItemTransaksiState extends State<RepeatOrderItemTransaksi> {
  List<String> selectedSellers = [];
  List itemTransaksiState = [];

  bool _isProcessingRO = false;

  void didUpdateWidget(x) {
    super.didUpdateWidget(x);
    setState(() {
      selectedSellers = [];
    });
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idTransaksi = widget.idTransaksi;
    Function onRefresh = widget.onRefresh;

    double _widthAndHeightofLoading = SizeConfig.horizontalBlock * 4.5;

    return FutureBuilder<List>(
        future: ROModel.getItemTransaksiPerPenjual(idTransaksi),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List itemTransaksiState = snapshot.data!;

              return Expanded(
                child: RefreshIndicator(
                  onRefresh: () async {
                    onRefresh();
                  },
                  child: (itemTransaksiState.length >= 1)
                      ? Stack(
                          children: [
                            ListView.builder(
                                padding: EdgeInsets.only(
                                    top: 0,
                                    left: 5.0,
                                    right: 5.0,
                                    bottom: 10.0),
                                itemCount: itemTransaksiState.length,
                                itemBuilder: (context, index) {
                                  Map<String, dynamic> item =
                                      itemTransaksiState[index];
                                  bool isAvailable = item['isAvailable'];
                                  String idTransaksi = item['idTransaksi'];
                                  String idPenjual = item['idPenjual'];
                                  String namaPenjual = item['namaPenjual'];
                                  String namaPasar = item['namaPasar'];
                                  String bannerPenjual = item['bannerPenjual'];
                                  String alamatToko = item['alamatToko'];

                                  List itemBelanjaanTransaksi =
                                      item['itemTransaksi'];

                                  Map<String, dynamic> detailPenjual = {
                                    'idPenjual': idPenjual,
                                    'namaPenjual': namaPenjual,
                                    'namaPasar': namaPasar,
                                    'bannerPenjual': bannerPenjual,
                                    'alamatToko': alamatToko
                                  };

                                  bool isSelected =
                                      selectedSellers.contains(idPenjual);

                                  return RepeatOrderTransaksiItemPerPenjual(
                                      isSelected,
                                      isAvailable,
                                      idTransaksi,
                                      detailPenjual,
                                      itemBelanjaanTransaksi, (isChecked) {
                                    if (selectedSellers.contains(idPenjual)) {
                                      selectedSellers.remove(idPenjual);
                                    } else {
                                      selectedSellers.add(idPenjual);
                                    }

                                    List<String> newSelectedSellers =
                                        selectedSellers;
                                    setState(() {
                                      selectedSellers = newSelectedSellers;
                                    });
                                  });
                                }),
                            (selectedSellers.length >= 1)
                                ? Positioned(
                                    bottom: SizeConfig.horizontalBlock * 5.0,
                                    right: SizeConfig.horizontalBlock * 5.0,
                                    child: FloatingActionButton(
                                        backgroundColor: CustomColors.green,
                                        onPressed: (_isProcessingRO)
                                            ? null
                                            : () async {
                                                setState(() {
                                                  _isProcessingRO = true;
                                                });
                                                Map<String, dynamic>
                                                    _addToCartResponse =
                                                    await ROModel
                                                        .addToCartThesePenjual(
                                                            idTransaksi,
                                                            selectedSellers);

                                                String _text =
                                                    'Gagal menambahkan ke keranjang belanjaan, coba lagi!';

                                                bool _statusAddToCart =
                                                    _addToCartResponse[
                                                        'statusAddCart'];
                                                if (_statusAddToCart) {
                                                  _text =
                                                      'Berhasil menambahkan ke keranjang!';
                                                }

                                                if (_addToCartResponse
                                                    .containsKey('message')) {
                                                  String? _message =
                                                      _addToCartResponse[
                                                          'message'];
                                                  if (_message != null) {
                                                    _text =
                                                        _text + ' ' + _message;
                                                  }
                                                }

                                                SnackBar snackBarAddToCart =
                                                    SnackBar(
                                                        duration: Duration(
                                                            seconds: 3),
                                                        action:
                                                            (_statusAddToCart)
                                                                ? SnackBarAction(
                                                                    label:
                                                                        'Lihat Keranjang',
                                                                    textColor:
                                                                        CustomColors
                                                                            .green,
                                                                    onPressed:
                                                                        () {
                                                                      Navigator.push(
                                                                          context,
                                                                          MaterialPageRoute(
                                                                              builder: (_) => Keranjang()));
                                                                    })
                                                                : null,
                                                        content: Text(_text,
                                                            style: TextStyle(
                                                                fontSize: SizeConfig
                                                                        .horizontalBlock *
                                                                    3.5)));

                                                ScaffoldMessenger.of(context)
                                                    .showSnackBar(
                                                        snackBarAddToCart);

                                                if (_statusAddToCart) {
                                                  int jumlahCart =
                                                      await HeaderModel
                                                          .getJumlahCart();
                                                  Provider.of<HeaderViewModel>(
                                                          context,
                                                          listen: false)
                                                      .jumlahCart = jumlahCart;
                                                }

                                                setState(() {
                                                  _isProcessingRO = false;
                                                });
                                              },
                                        child: (_isProcessingRO)
                                            ? SizedBox(
                                                width: _widthAndHeightofLoading,
                                                height:
                                                    _widthAndHeightofLoading,
                                                child:
                                                    CircularProgressIndicator(
                                                        strokeWidth: 1.5,
                                                        color: CustomColors
                                                            .nearlyWhite))
                                            : Icon(Icons.shopping_cart,
                                                size:
                                                    SizeConfig.horizontalBlock *
                                                        6.0)),
                                  )
                                : SizedBox()
                          ],
                        )
                      : LayoutBuilder(builder: (context, constraints) {
                          return ListView(
                              padding: EdgeInsets.only(
                                  top: 0.0, right: 0, left: 0, bottom: 10.0),
                              children: [
                                Container(
                                    padding: EdgeInsets.symmetric(
                                        horizontal:
                                            SizeConfig.horizontalBlock * 15.0),
                                    constraints: BoxConstraints(
                                        minHeight: constraints.maxHeight),
                                    child: Column(
                                        mainAxisAlignment:
                                            MainAxisAlignment.center,
                                        children: [
                                          Image.asset(
                                              ClientPath.assetsImg +
                                                  '/empty.png',
                                              width:
                                                  SizeConfig.horizontalBlock *
                                                      25),
                                          SizedBox(height: 10),
                                          Text(
                                              'Tidak ada belanjaan di dalam transaksi ini yang dapat dibelanja ulang.\nSilahkan belanja ulang seperti biasa!',
                                              textAlign: TextAlign.center,
                                              style: TextStyle(
                                                  color: CustomColors.textMuted,
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      2.75))
                                        ]))
                              ]);
                        }),
                ),
              );
            }
          }

          return Expanded(
              child: Center(
            child: SizedBox(
                height: 15.0,
                width: 15.0,
                child: CircularProgressIndicator(strokeWidth: 1.5)),
          ));
        });
  }
}
