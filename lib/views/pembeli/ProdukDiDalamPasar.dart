import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/PasarModel.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart';
import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/PasarLain.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/global-components/Pencarian.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/models/ProdukModel.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:provider/provider.dart';

class ProdukDiDalamPasar extends StatefulWidget {
  final String idPasar;
  final String namaPasar;
  ProdukDiDalamPasar(
      {required this.idPasar, this.namaPasar = 'Pasar Tidak di Kenal'});
  _ProdukDiDalamPasarState createState() => _ProdukDiDalamPasarState();
}

class _ProdukDiDalamPasarState extends State<ProdukDiDalamPasar> {
  String? _pencarian;
  bool _searchTyped = false;

  int _startFrom =
      0; //0 karena akan ditambah 10 ketika initialization() dijalankan
  int _startFromSearch =
      10; //langsung 10 karena tidak diset ketika initialization. Kalau dari 0 maka 10 produk pertama akan dipanggil lagi
  List _dataProdukState = [];
  List _dataProdukStateImmutable = [];
  bool _isFetchingData = false;

  ScrollController _gridViewController = ScrollController();

  void initState() {
    super.initState();
    _initialization();
  }

  void dispose() {
    super.dispose();
    _gridViewController.dispose();
  }

  void _initialization() {
    String _idPasar = widget.idPasar;
    String _startFromString = _startFrom.toString();

    _gridViewController.addListener(() {
      if (!_isFetchingData) {
        if (_gridViewController.position.pixels >=
            _gridViewController.position.maxScrollExtent) {
          String? _cariProduk;
          bool _isSearching = false;
          if (_pencarian != null) {
            if (_pencarian!.length >= 1) {
              _cariProduk = _pencarian;
              _isSearching = true;
            }
          }

          setState(() {
            _isFetchingData = true;
          });
          PasarModel.getProdukInThisPasar(_idPasar,
                  useLimit: 'y',
                  startFrom: (_isSearching)
                      ? _startFromSearch.toString()
                      : _startFrom.toString(),
                  cariProduk: _cariProduk)
              .then((_nextDataProduk) {
            if (_isSearching) {
              _startFromSearch = _startFromSearch + 10;
            } else {
              _startFrom = _startFrom + 10;
            }
            _isFetchingData = false;
            setState(() {
              _dataProdukState = [..._dataProdukState, ..._nextDataProduk];
              _dataProdukStateImmutable = [
                ..._dataProdukStateImmutable,
                ..._nextDataProduk
              ];
            });
          });
        }
      }
    });

    PasarModel.getProdukInThisPasar(_idPasar,
            useLimit: 'y', startFrom: _startFromString)
        .then((_dataProduk) {
      setState(() {
        _dataProdukState = _dataProduk;
        _dataProdukStateImmutable = _dataProduk;
        _startFrom = _startFrom + 10;
      });
    });
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idPasar = widget.idPasar;

    String _tidakAdaProduk = 'Tunggu sebentar ya mak...';
    if (_searchTyped) {
      _tidakAdaProduk = 'Maaf Mak...Produk yang dicari belum ada di pasar ini';
    }

    List<Widget> _gridViewChildren = _dataProdukState.map((dataProduk) {
      return ProdukInThisPasarItem(dataProduk, () async {
        ADJDModel.deleteAlamatDanJadwalDelivery();
        Map<String, dynamic> adjdSet = await HeaderModel.adjd(context);

        String? latitude = adjdSet['latitude'];
        String? longitude = adjdSet['longitude'];
        String? kodeJadwalDelivery = adjdSet['kodeJadwalDelivery'];

        bool isADJDCompleted = false;
        if (latitude != null &&
            longitude != null &&
            kodeJadwalDelivery != null) {
          isADJDCompleted = true;
          Provider.of<HeaderViewModel>(context, listen: false).isADJDSet =
              isADJDCompleted;
          await ProdukModel.showSatuanProduk(context, dataProduk);
        }
      }, widgetWidth: SizeConfig.horizontalBlock * 25);
    }).toList();

    return Scaffold(
      body: Column(
        children: [
          Header(),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () async {
                setState(() {});
              },
              child: ListView(
                  shrinkWrap: true,
                  padding: EdgeInsets.zero,
                  children: [
                    Padding(
                      padding:
                          EdgeInsets.symmetric(horizontal: 10, vertical: 10),
                      child: Text('Produk di Pasar ' + widget.namaPasar,
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 4,
                              fontWeight: FontWeight.w700)),
                    ),
                    SizedBox(height: 5),
                    Padding(
                      padding: EdgeInsets.symmetric(horizontal: 10),
                      child: Pencarian(
                          onSearch: (string) async {
                            List _dataProduk = [];
                            if (string.length >= 1) {
                              List _fetchedProduk =
                                  await PasarModel.getProdukInThisPasar(
                                      widget.idPasar,
                                      useLimit: 'y',
                                      cariProduk: string);
                              _dataProduk = _fetchedProduk;
                            } else {
                              _dataProduk = _dataProdukStateImmutable;
                            }

                            setState(() {
                              _pencarian = string;
                              _searchTyped = true;
                              _dataProdukState = _dataProduk;
                            });
                          },
                          onClose: () {
                            setState(() {
                              _pencarian = null;
                              _searchTyped = false;
                              _dataProdukState = _dataProdukStateImmutable;
                            });
                          },
                          hint: 'Cari Produk di Sini',
                          text: _pencarian ?? ''),
                    ),
                    SizedBox(height: 10.0),
                    Padding(
                      padding: EdgeInsets.symmetric(horizontal: 10),
                      child: Container(
                        width: SizeConfig.horizontalBlock * 100,
                        child: Container(
                            height: SizeConfig.verticalBlock * 50,
                            child: (_dataProdukState.length >= 1)
                                ? Stack(children: [
                                    GridView.count(
                                        controller: _gridViewController,
                                        padding: EdgeInsets.only(top: 0),
                                        crossAxisCount: 3,
                                        childAspectRatio: 0.45,
                                        children: _gridViewChildren),
                                    (_isFetchingData)
                                        ? Container(
                                            decoration: BoxDecoration(
                                                borderRadius:
                                                    BorderRadius.circular(10.0),
                                                color: Color(0xCFFFFFFF)),
                                            child: Center(
                                                child: Column(
                                                    mainAxisSize:
                                                        MainAxisSize.min,
                                                    children: [
                                                  Image.asset(
                                                      ClientPath.assetsImg +
                                                          '/loading.png',
                                                      width: SizeConfig
                                                              .horizontalBlock *
                                                          35.0),
                                                  SizedBox(height: 10.0),
                                                  Text('Sedang Mem-proses',
                                                      textAlign:
                                                          TextAlign.center,
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              4.0,
                                                          fontWeight:
                                                              FontWeight.bold)),
                                                  SizedBox(height: 5.0),
                                                  Text('Tunggu sebentar ya...',
                                                      textAlign:
                                                          TextAlign.center,
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3.25,
                                                          color: CustomColors
                                                              .textMuted)),
                                                  SizedBox(height: 15.0),
                                                  SizedBox(
                                                      height: SizeConfig
                                                              .horizontalBlock *
                                                          5.0,
                                                      width: SizeConfig
                                                              .horizontalBlock *
                                                          5.0,
                                                      child: CircularProgressIndicator(
                                                          strokeWidth: 2.5,
                                                          backgroundColor:
                                                              CustomColors
                                                                  .green,
                                                          valueColor:
                                                              AlwaysStoppedAnimation(
                                                                  CustomColors
                                                                      .nearlyWhite)))
                                                ])),
                                          )
                                        : SizedBox()
                                  ])
                                : Center(
                                    child: Padding(
                                    padding: EdgeInsets.symmetric(
                                        horizontal:
                                            SizeConfig.horizontalBlock * 10.0),
                                    child: Column(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        (_searchTyped)
                                            ? Image.asset(
                                                ClientPath.assetsImg +
                                                    '/empty.png',
                                                width:
                                                    SizeConfig.horizontalBlock *
                                                        35.0)
                                            : Loading(),
                                        SizedBox(height: 15.0),
                                        Text(_tidakAdaProduk,
                                            textAlign: TextAlign.center,
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25)),
                                      ],
                                    ),
                                  ))),
                      ),
                    ),
                    SizedBox(height: 10),
                    PasarLain(idPasar)
                  ]),
            ),
          ),
        ],
      ),
      backgroundColor: CustomColors.nearlyWhite,
      bottomNavigationBar: Footer(),
    );
  }
}
