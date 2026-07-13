import 'package:flutter/material.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/models/HomepageModel.dart';

import 'package:laukpauk/models/KontenModel.dart';

import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:laukpauk/views/pembeli/DetailProduk.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/KontenItemBanner.dart';

import 'package:laukpauk/global-components/SearchResult.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/ProdukPasarSekitar.dart';
import 'package:provider/provider.dart';

class HeaderSearch extends StatefulWidget {
  _HeaderSearchState createState() => _HeaderSearchState();
}

class _HeaderSearchState extends State<HeaderSearch> {
  final FocusNode _focusNode = FocusNode();
  final TextEditingController _pencarianController = TextEditingController();

  bool _onFocus = false;

  void initState() {
    super.initState();
    _focusNode.addListener(() {
      setState(() {
        _onFocus =
            (_focusNode.hasFocus || _pencarianController.text.length >= 1);
      });
    });
  }

  Widget build(BuildContext context) {
    String _search = _pencarianController.text;

    return Scaffold(
        appBar: AppBar(
            backgroundColor: Colors.transparent,
            elevation: 0.0,
            leading: GestureDetector(
                onTap: () => Navigator.pop(context),
                child: Icon(Icons.arrow_back_ios, color: CustomColors.grey)),
            title: TextField(
              controller: _pencarianController,
              focusNode: _focusNode,
              onSubmitted: (String _cari) async {

              },
              decoration: InputDecoration(
                  border: InputBorder.none,
                  hintText: 'Cari Produk dan Toko di sini',
                  hintStyle: TextStyle(color: CustomColors.grey)),
            ),
            actions: (_onFocus)
                ? [
                    IconButton(
                        icon:
                            Icon(Icons.cancel, color: CustomColors.textDanger),
                        onPressed: () {
                          _focusNode.unfocus();
                          _pencarianController.clear();
                          setState(() => _onFocus = false);
                        })
                  ]
                : null),
        body: (_onFocus)
            ? SearchResult(_search)
            : SingleChildScrollView(
                child: Padding(
                  padding:
                      EdgeInsets.only(bottom: 15.0, left: 15.0, right: 15.0),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Consumer<HeaderViewModel>(
                          builder: (consumerContext, headerView, _){
                            bool _isADJDSet = headerView.isADJDSet;
                            Map<String, dynamic>? _savedADJD = headerView.savedADJD;

                             return  FutureBuilder<List>(
                                  future: HomepageModel.getDataPasar(isADJDSet: _isADJDSet, savedADJD: _savedADJD, limitPasar: 1),
                                  builder: (context, snapshot) {
                                    if(snapshot.hasData){
                                      List? _dataPasar  = snapshot.data;
                                      if(_dataPasar != null){
                                        return ProdukPasarSekitar(pasarSekitarSaya: _dataPasar);
                                      }
                                      return SizedBox();
                                    }
                                    return Loading(circularProgressIndicatorSize: Size(25.0, 25.0));
                                  }); }),
                      FutureBuilder<List>(
                          future: KontenModel.getKontenAplikasi(
                              page: 'headersearch'),
                          builder: (futureBuilderContext, snapshot) {
                            if (snapshot.hasData) {
                              if (snapshot.data != null) {
                                List _dataKonten = snapshot.data!;
                                if (_dataKonten.length >= 1) {
                                  double _kontenTitleSize =
                                      SizeConfig.horizontalBlock * 4.0;

                                  List<Widget> _kontenItemChildren =
                                      _dataKonten.map((_konten) {
                                    String _idKonten = _konten['id'];
                                    String _namaDivisi = _konten['namaDivisi'];
                                    String _jenisItem = _konten['jenisItem'];
                                    String _jenisItemLC =
                                        _jenisItem.toLowerCase();

                                    return Padding(
                                      padding:
                                          const EdgeInsets.only(bottom: 15.0),
                                      child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          mainAxisSize: MainAxisSize.min,
                                          children: [
                                            Text(_namaDivisi,
                                                style: TextStyle(
                                                    fontWeight: FontWeight.w500,
                                                    fontSize:
                                                        _kontenTitleSize)),
                                            SizedBox(height: 10.0),
                                            FutureBuilder<List>(
                                                future:
                                                    KontenModel.getProdukKonten(
                                                        _idKonten),
                                                builder:
                                                    (kontenContext, snapshot) {
                                                  if (snapshot.hasData) {
                                                    if (snapshot.data != null) {
                                                      List _kontenItem =
                                                          snapshot.data!;
                                                      if (_kontenItem.length >=
                                                          1) {
                                                        if (_jenisItemLC ==
                                                            'produk') {
                                                          List<Widget> _children = _kontenItem
                                                              .map((_produkKontenItem) => ListTile(
                                                                  onTap: () async {
                                                                    await Navigator.pushReplacement(
                                                                        context,
                                                                        MaterialPageRoute(
                                                                            builder: (_) =>
                                                                                DetailProduk(_produkKontenItem['id'])));
                                                                  },
                                                                  dense: true,
                                                                  contentPadding: EdgeInsets.zero,
                                                                  leading: ClipRRect(
                                                                    borderRadius:
                                                                        BorderRadius.circular(
                                                                            50.0),
                                                                    child: Container(
                                                                        width:
                                                                            50.0,
                                                                        height:
                                                                            50.0,
                                                                        decoration:
                                                                            BoxDecoration(image: DecorationImage(fit: BoxFit.fill, image: NetworkImage(ServerPath.baseURL + '/' + ServerPath.uploadGambarProduk + '/compress/' + _produkKontenItem['foto'])))),
                                                                  ),
                                                                  title: Text(_produkKontenItem['nama']),
                                                                  subtitle: Text(_produkKontenItem['namaKategoriProduk'])))
                                                              .toList();

                                                          return Column(
                                                              mainAxisSize:
                                                                  MainAxisSize
                                                                      .min,
                                                              children:
                                                                  _children);
                                                        } else {
                                                          return KontenItemBanner(
                                                              _konten,
                                                              _kontenItem);
                                                        }
                                                      }
                                                    }
                                                    return SizedBox();
                                                  } else {
                                                    return Loading();
                                                  }
                                                })
                                          ]),
                                    );
                                  }).toList();
                                  return Column(
                                      mainAxisSize: MainAxisSize.min,
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: _kontenItemChildren);
                                }
                              }
                              return SizedBox();
                            } else {
                              return Loading();
                            }
                          }),
                    ],
                  ),
                ),
              ));
  }
}
