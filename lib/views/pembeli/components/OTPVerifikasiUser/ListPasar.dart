import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

import 'package:laukpauk/global-components/Pencarian.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class ListPasar extends StatefulWidget {
  final String pasarSelected;
  final Function onPasarClicked;

  ListPasar({required this.pasarSelected, required this.onPasarClicked});

  _ListPasarState createState() => _ListPasarState();
}

class _ListPasarState extends State<ListPasar> {
  bool isReadyToShowState = false;
  List dataPasarState = [];
  List dataPasarImmutableState = [];
  String pencarianString = '';

  Future<List> getPasar() async {
    String _params =
        await HTTPRequest.getQueryString(params: 'jenisPasar=pasarTersedia');

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Pasar/getPasar?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      List dataPasar = dataObject['dataPasar'];

      return dataPasar;
    }

    return [];
  }

  void initialization() async {
    List dataPasar = await getPasar();

    setState(() {
      dataPasarState = dataPasar;
      dataPasarImmutableState = dataPasar;
      isReadyToShowState = true;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
        margin: EdgeInsets.only(top: 5),
        height: SizeConfig.horizontalBlock * 40.0,
        child: (isReadyToShowState)
            ? Column(children: [
                Pencarian(
                    onSearch: (String string) {
                      setState(() {
                        pencarianString = string;
                      });

                      List dataPasarFiltered =
                          dataPasarImmutableState.where((pasar) {
                        String namaPasar = pasar['nama'];
                        String namaPasarLC = namaPasar.toLowerCase();

                        return namaPasarLC.indexOf(string) >= 0;
                      }).toList();

                      setState(() {
                        dataPasarState = dataPasarFiltered;
                      });
                    },
                    onClose: () {
                      setState(() {
                        dataPasarState = dataPasarImmutableState;
                        pencarianString = '';
                      });
                    },
                    text: pencarianString),
                (dataPasarState.length >= 1)
                    ? Expanded(
                        child: ListView.builder(
                            itemBuilder: (context, index) {
                              Map<String, dynamic> pasarItem =
                                  dataPasarState[index];
                              BoxDecoration? boxDecoration;
                              if (pasarItem['id'] == widget.pasarSelected) {
                                boxDecoration = BoxDecoration(
                                    color: Color(0x7766CC99),
                                    borderRadius: BorderRadius.circular(5));
                              }

                              String pasarImg = ServerPath.baseURL +
                                  '/' +
                                  ServerPath.uploadGambarPasar +
                                  '/compress/' +
                                  pasarItem['gambar'];
                              String namaPasar = pasarItem['nama'];

                              return InkWell(
                                onTap: () {
                                  widget.onPasarClicked(pasarItem['id']);
                                },
                                child: Container(
                                  width: SizeConfig.horizontalBlock * 25,
                                  padding: EdgeInsets.only(right: 5, left: 5),
                                  decoration: boxDecoration,
                                  child: Row(
                                      mainAxisAlignment:
                                          MainAxisAlignment.center,
                                      children: [
                                        Expanded(
                                          child: Column(
                                              mainAxisAlignment:
                                                  MainAxisAlignment.center,
                                              crossAxisAlignment:
                                                  CrossAxisAlignment.center,
                                              children: [
                                                ClipRRect(
                                                  borderRadius:
                                                      BorderRadius.circular(50),
                                                  child: Image.network(pasarImg,
                                                      width: SizeConfig
                                                              .horizontalBlock *
                                                          15,
                                                      height: SizeConfig
                                                              .horizontalBlock *
                                                          15),
                                                ),
                                                SizedBox(height: 7.5),
                                                SingleChildScrollView(
                                                  scrollDirection:
                                                      Axis.horizontal,
                                                  child: Text(namaPasar,
                                                      textAlign:
                                                          TextAlign.center,
                                                      style: TextStyle(
                                                          fontSize: SizeConfig
                                                                  .horizontalBlock *
                                                              3,
                                                          fontWeight:
                                                              FontWeight.w500)),
                                                ),
                                              ]),
                                        )
                                      ]),
                                ),
                              );
                            },
                            itemCount: dataPasarState.length,
                            scrollDirection: Axis.horizontal),
                      )
                    : Container(
                        margin: EdgeInsets.only(bottom: 5, top: 5),
                        child: Center(
                            child: Column(
                                mainAxisAlignment: MainAxisAlignment.center,
                                crossAxisAlignment: CrossAxisAlignment.center,
                                children: [
                              Image.asset(ClientPath.assetsImg + '/empty.png',
                                  width: SizeConfig.horizontalBlock * 25),
                              Text('Tidak ada pasar yang bisa dipilih',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 3))
                            ])),
                      )
              ])
            : Center(
                child: SizedBox(
                    width: 15,
                    height: 15,
                    child: CircularProgressIndicator(strokeWidth: 1.5))));
  }
}
