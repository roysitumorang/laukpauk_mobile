import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/Penjual.dart';
import 'package:laukpauk/views/pembeli/DetailProduk.dart';

class SearchResult extends StatefulWidget {
  final String searchString;
  SearchResult(this.searchString);

  _SearchResultState createState() => _SearchResultState();
}

class _SearchResultState extends State<SearchResult> {
  String? _searchStringState;

  void didUpdateWidget(oldWidget) {
    super.didUpdateWidget(oldWidget);

    String _searchString = widget.searchString;
    if (oldWidget.searchString != _searchString) {
      setState(() {
        _searchStringState = _searchString;
      });
    }
  }

  Future<List> _getSuggestions() async {
    await Future.delayed(Duration(milliseconds: 500));

    String _params =
        await HTTPRequest.getQueryString(params: 'term=$_searchStringState');

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/Konten/pencarian?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);
      List hasilPencarian = dataObject['hasilPencarian'];

      return hasilPencarian;
    }

    return [];
  }

  Widget build(BuildContext context) {
    return Center(
        child: (_searchStringState != null)
            ? FutureBuilder<List>(
                future: _getSuggestions(),
                builder: (futureBuilderContext, snapshot) {
                  if (snapshot.hasData) {
                    if (snapshot.data != null) {
                      List _hasilPencarian = snapshot.data!;

                      if (_hasilPencarian.length >= 1) {
                        return RefreshIndicator(
                          onRefresh: () async {
                            await _getSuggestions();
                          },
                          child: ListView.builder(
                              itemCount: _hasilPencarian.length,
                              itemBuilder: (context, index) {
                                Map<String, dynamic> suggestion =
                                    _hasilPencarian[index];
                                String jenisItem = suggestion['jenisItem']
                                    .toString()
                                    .toLowerCase();

                                String? leading = (jenisItem == 'penjual')
                                    ? suggestion['bannerToko']
                                    : suggestion['fotoProduk']; //keading
                                String? title = (jenisItem == 'penjual')
                                    ? suggestion['namaToko']
                                    : suggestion['namaProduk']; //title
                                String? subtitle = (jenisItem == 'penjual')
                                    ? suggestion['alamatToko']
                                    : suggestion['namaPenjual']; //subtitle
                                String id = suggestion['id'];

                                if (subtitle == null || subtitle.length <= 0) {
                                  subtitle = (jenisItem == 'penjual')
                                      ? 'Alamat Toko Belum ditentukan'
                                      : 'Nama Penjual Belum ditentukan';
                                }
                                if (title == null || title.length <= 0) {
                                  title = (jenisItem == 'penjual')
                                      ? 'Nama Toko / Penjual belum ditentukan'
                                      : 'Nama Produk Belum ditentukan';
                                }

                                bool isLeadingNull =
                                    leading == null || leading.length <= 0;

                                String _image =
                                    ClientPath.assetsImg + '/laukpauk.png';
                                if (!isLeadingNull) {
                                  String imagePath = (jenisItem == 'penjual')
                                      ? ServerPath.uploadGambarBannerPenjual +
                                          '/compress/' +
                                          leading
                                      : ServerPath.uploadGambarProduk +
                                          '/compress/' +
                                          leading;
                                  _image = ServerPath.baseURL + '/' + imagePath;
                                }

                                return ListTile(
                                    onTap: () {
                                      Navigator.pushReplacement(
                                          context,
                                          MaterialPageRoute(
                                              builder: (_) =>
                                                  (jenisItem == 'penjual')
                                                      ? Penjual(id: id)
                                                      : DetailProduk(id)));
                                    },
                                    leading: ClipRRect(
                                      borderRadius: BorderRadius.circular(50.0),
                                      child: Container(
                                        width: 50.0,
                                        height: 50.0,
                                        decoration: BoxDecoration(
                                            image: DecorationImage(
                                                fit: BoxFit.fill,
                                                image: NetworkImage(_image))),
                                      ),
                                    ),
                                    title: SingleChildScrollView(
                                      scrollDirection: Axis.horizontal,
                                      child: Row(
                                        children: [
                                          Container(
                                              padding: EdgeInsets.symmetric(
                                                  horizontal: 10, vertical: 3),
                                              margin:
                                                  EdgeInsets.only(right: 7.5),
                                              child: Text(
                                                  jenisItem.toUpperCase(),
                                                  style: TextStyle(
                                                      color: CustomColors
                                                          .nearlyWhite,
                                                      fontSize: SizeConfig
                                                              .horizontalBlock *
                                                          2.5)),
                                              decoration: BoxDecoration(
                                                  color: (jenisItem ==
                                                          'penjual')
                                                      ? CustomColors.textPrimary
                                                      : CustomColors.green,
                                                  borderRadius:
                                                      BorderRadius.all(Radius
                                                          .circular(7.5)))),
                                          Text(title)
                                        ],
                                      ),
                                    ),
                                    subtitle: Text(subtitle));
                              }),
                        );
                      }
                    }
                    return Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Image.asset(ClientPath.assetsImg + '/empty.png',
                              width: SizeConfig.horizontalBlock * 30),
                          Padding(
                              padding: EdgeInsets.only(top: 10),
                              child: Text(
                                  'Produk "$_searchStringState" tidak ditemukan. Coba cari lagi ya ..',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25,
                                      color: CustomColors.textMuted)))
                        ]);
                  } else {
                    return Loading();
                  }
                })
            : Column(mainAxisSize: MainAxisSize.min, children: <Widget>[
                Image.asset(ClientPath.assetsImg + '/produk.png',
                    width: SizeConfig.horizontalBlock * 30),
                Padding(
                    padding: EdgeInsets.only(top: 10),
                    child: Text(
                        'Yuk, cari produk yang ingin kamu beli disini .. ',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25,
                            color: CustomColors.textMuted)))
              ]));
  }
}
