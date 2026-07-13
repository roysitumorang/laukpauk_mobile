import 'package:flutter/material.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/models/NotifikasiModel.dart';
import 'package:laukpauk/models/UserModel.dart';
import 'package:laukpauk/views/pembeli/components/Notifikasi/NotifikasiGlobal.dart';
import 'package:laukpauk/views/pembeli/components/Notifikasi/NotifikasiKhusus.dart';
import 'package:laukpauk/views/pembeli/components/Notifikasi/NotifikasiTab.dart';

import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Header.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/Login.dart';

import 'package:laukpauk/models/AuthModel.dart';

class Notifikasi extends StatefulWidget {
  Notifikasi();
  _NotifikasiState createState() => _NotifikasiState();
}

class _NotifikasiState extends State<Notifikasi> {
  int jumlahNotifikasiGlobal = 0, jumlahNotifikasiKhusus = 0;
  int reload = 0;
  String jenisNotifikasi = 'update';

  Future<void> _initialization() async {
    int jumlahNotifikasiGlobalVariable =
        await NotifikasiModel.getJumlahNotifikasi('unread');
    int jumlahNotifikasiKhususVariable =
        await NotifikasiModel.getJumlahNotifikasiKhusus('unread');

    setState(() {
      jumlahNotifikasiGlobal = jumlahNotifikasiGlobalVariable;
      jumlahNotifikasiKhusus = jumlahNotifikasiKhususVariable;
    });
  }

  void initState() {
    super.initState();
    _initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    // String penjelasanJenisNotifikasi;
    // if (jenisNotifikasi == 'update') {
    //   penjelasanJenisNotifikasi =
    //       'Dapatkan beragam informasi menarik dari Laukpauk mulai dari voucher, promo, diskon, dan lainnya disini!';
    // } else {
    //   penjelasanJenisNotifikasi =
    //       'Info orderan, dompet dan transaksi penjual (jika terdaftar sebagai penjual)';
    // }

    return FutureBuilder<bool>(
        future: AuthModel.isLogin(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              bool isLogin = snapshot.data!;
              if (isLogin) {
                return Scaffold(
                    bottomNavigationBar: Footer(),
                    body: FutureBuilder(
                        future: UserModel.getDetailActiveUser(),
                        builder: (detailActiveUserContext, snapshot) {
                          if (snapshot.hasData) {
                            if (snapshot.data != null) {
                              if (snapshot.data != false) {
                                Map<String, dynamic> _data =
                                    snapshot.data as Map<String, dynamic>;
                                String _idUser = _data['id'];
                                return Container(
                                    child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                      Header(),
                                      NotifikasiTab(jenisNotifikasi,
                                          (String jenisNotifikasiSelected) {
                                        setState(() {
                                          jenisNotifikasi =
                                              jenisNotifikasiSelected;
                                        });
                                      },
                                          jumlahNotifikasiGlobal:
                                              jumlahNotifikasiGlobal,
                                          jumlahNotifikasiKhusus:
                                              jumlahNotifikasiKhusus),
                                      (jenisNotifikasi == 'update')
                                          ? NotifikasiGlobal(
                                              idUser: _idUser,
                                              reloadJumlahNotifikasi: () {
                                                _initialization();
                                              })
                                          : NotifikasiKhusus(
                                              idUser: _idUser,
                                              reloadJumlahNotifikasi: () {
                                                _initialization();
                                              })
                                    ]));
                              } else {
                                return Center(
                                    child: Text('Anda tidak terautentikasi!'));
                              }
                            }
                          }
                          return Loading();
                        }));
              }
            }

            return Login(routeTo: Notifikasi());
          } else {
            return Scaffold(
                body: Center(
                    child: SizedBox(
                        width: 25.0,
                        height: 25.0,
                        child: CircularProgressIndicator(strokeWidth: 1.5))));
          }
        });
  }
}
