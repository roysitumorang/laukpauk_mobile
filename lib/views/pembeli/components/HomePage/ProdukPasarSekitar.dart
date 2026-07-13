import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/models/PasarModel.dart';
import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/models/ProdukModel.dart';

import 'package:laukpauk/views/pembeli/ProdukDiDalamPasar.dart';

import 'package:laukpauk/views/pembeli/components/ProdukDiDalamPasar/ProdukDiDalamPasarItem.dart';

class ProdukPasarSekitar extends StatelessWidget {
  final List pasarSekitarSaya;
  ProdukPasarSekitar({required this.pasarSekitarSaya});

  Widget build(BuildContext context) {
    List _pasarSekitarSaya = pasarSekitarSaya;
    if (_pasarSekitarSaya.length >= 3) {
      _pasarSekitarSaya = [_pasarSekitarSaya[0], _pasarSekitarSaya[1]];
    }
    List<Widget> _pasarSekitar =
        _pasarSekitarSaya.map((pasar) => ProdukPasar(pasar: pasar)).toList();

    return Container(
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      Text('Pasar Terdekat',
          style: TextStyle(
              fontSize: SizeConfig.horizontalBlock * 4.0,
              fontWeight: FontWeight.w500)),
      SizedBox(height: 10.0),
      ..._pasarSekitar,
      SizedBox(height: 15.0)
    ]));
  }
}

class ProdukPasar extends StatelessWidget {
  final Map<String, dynamic> pasar;
  ProdukPasar({required this.pasar});
  Widget build(BuildContext context) {
    String _idPasar = pasar['id'];
    String _nama = pasar['nama'];
    String _gambar = pasar['gambar'];
    String _gambarPasarURL = ServerPath.baseURL +
        '/' +
        ServerPath.uploadGambarPasar +
        '/compress/' +
        _gambar;

    String? _jarakString = pasar['distance'];
    double? _jarak = (_jarakString != null) ? double.parse(_jarakString) : null;

    return Container(
        margin: EdgeInsets.only(bottom: 15.0),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(mainAxisAlignment: MainAxisAlignment.start, children: [
            ClipRRect(
                borderRadius: BorderRadius.all(Radius.circular(50.0)),
                child: Image.network(_gambarPasarURL,
                    width: 50.0, height: 50.0, fit: BoxFit.cover)),
            SizedBox(width: 15.0),
            Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Produk Pasar ' + _nama,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.5,
                            fontWeight: FontWeight.w500)),
                    SizedBox(height: (_jarak != null) ? 2.5 : 0.0),
                    (_jarak != null)
                        ? Text(_jarak.toStringAsFixed(1) + ' KM',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 2.75))
                        : SizedBox(),
                  ]),
            ),
            SizedBox(width: 15.0),
            GestureDetector(
              child: Text('Lihat Semua',
                  style: TextStyle(
                      color: CustomColors.darkGreen,
                      fontWeight: FontWeight.w500,
                      fontSize: SizeConfig.horizontalBlock * 3.0)),
              onTap: () => Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (_) => ProdukDiDalamPasar(
                          idPasar: _idPasar, namaPasar: _nama))),
            )
          ]),
          FutureBuilder<List>(
              future: PasarModel.getProdukInThisPasar(_idPasar,
                  useLimit: 'y', startFrom: '0'),
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  if (snapshot.data != null) {
                    List _produkPasar = snapshot.data!;

                    return Container(
                      margin: EdgeInsets.only(top: 10.0),
                      height: 275.0,
                      child: ListView.builder(
                          padding: EdgeInsets.zero,
                          scrollDirection: Axis.horizontal,
                          itemCount: _produkPasar.length,
                          itemBuilder: (context, index) {
                            Map<String, dynamic> _dataProduk =
                                _produkPasar[index];

                            return ProdukInThisPasarItem(_dataProduk, () async {
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
                                Provider.of<HeaderViewModel>(context,
                                        listen: false)
                                    .isADJDSet = isADJDCompleted;
                                await ProdukModel.showSatuanProduk(
                                    context, _dataProduk);
                              }
                            }, widgetWidth: SizeConfig.horizontalBlock * 35);
                          }),
                    );
                  }
                }
                return Loading(circularProgressIndicatorSize: Size(25.0, 25.0));
              })
        ]));
  }
}
