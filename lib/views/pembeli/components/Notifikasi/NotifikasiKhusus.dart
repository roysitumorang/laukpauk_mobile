import 'package:flutter/material.dart';

import 'package:laukpauk/models/NotifikasiModel.dart';
import 'package:laukpauk/views/pembeli/components/Notifikasi/NotifikasiKhususItem.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class NotifikasiKhusus extends StatefulWidget {
  final String idUser;
  final Function reloadJumlahNotifikasi;

  NotifikasiKhusus(
      {required this.idUser, required this.reloadJumlahNotifikasi});
  _NotifikasiKhususState createState() => _NotifikasiKhususState();
}

class _NotifikasiKhususState extends State<NotifikasiKhusus> {
  int reload = 0;

  Widget build(BuildContext context) {
    return FutureBuilder<List>(
        future: NotifikasiModel.getNotifikasiKhusus(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List notifikasi = snapshot.data!;
              if (notifikasi.length >= 1) {
                List<Widget> notifikasiWidget = notifikasi
                    .map((notifikasiItem) =>
                        NotifikasiKhususItem(notifikasiItem,
                            toggleNotifikasiCallback: (statusToggleNotifikasi) {
                          if (statusToggleNotifikasi) {
                            setState(() {
                              reload++;
                            });

                            widget.reloadJumlahNotifikasi();
                          } else {
                            SnackBar toggleSnackBar = SnackBar(
                                duration: Duration(seconds: 2),
                                content: Text(
                                    'Gagal mengubah status notifikasi. Silahkan coba lagi !'));

                            // Scaffold.of(context).showSnackBar(toggleSnackBar);
                            ScaffoldMessenger.of(context)
                                .showSnackBar(toggleSnackBar);
                          }
                        }))
                    .toList();

                return Expanded(
                  child: RefreshIndicator(
                    onRefresh: () async {
                      setState(() {});
                    },
                    child: ListView(
                        padding: EdgeInsets.zero,
                        children: [...notifikasiWidget]),
                  ),
                );
              } else {
                return Expanded(
                  child: LayoutBuilder(builder: (context, constrainst) {
                    return RefreshIndicator(
                      onRefresh: () async {
                        setState(() {});
                      },
                      child: ListView(padding: EdgeInsets.zero, children: [
                        Container(
                          constraints:
                              BoxConstraints(minHeight: constrainst.maxHeight),
                          child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Image.asset(ClientPath.assetsImg + '/empty.png',
                                    width: SizeConfig.horizontalBlock * 30),
                                SizedBox(height: 10),
                                Text('Tidak ada notifikasi apapun untuk anda',
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.5))
                              ]),
                        )
                      ]),
                    );
                  }),
                );
              }
            }
          }
          return Expanded(
            child: Center(
                child: SizedBox(
                    width: 25,
                    height: 25,
                    child: CircularProgressIndicator(strokeWidth: 1.5))),
          );
        });
  }
}
