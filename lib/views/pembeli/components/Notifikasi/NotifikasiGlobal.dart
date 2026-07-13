import 'package:flutter/material.dart';

import 'package:laukpauk/views/pembeli/components/Notifikasi/NotifikasiGlobalItem.dart';
import 'package:laukpauk/models/NotifikasiModel.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class NotifikasiGlobal extends StatefulWidget {
  final String idUser;
  final Function reloadJumlahNotifikasi;
  NotifikasiGlobal(
      {required this.idUser, required this.reloadJumlahNotifikasi});
  _NotifikasiGlobalState createState() => _NotifikasiGlobalState();
}

class _NotifikasiGlobalState extends State<NotifikasiGlobal> {
  int reload = 0;

  Widget build(BuildContext context) {
    return FutureBuilder<List>(
        future: NotifikasiModel.getUpdateNotifikasi(),
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            if (snapshot.data != null) {
              List notifikasi = snapshot.data!;
              if (notifikasi.length >= 1) {
                List<Widget> notifikasiWidget = notifikasi
                    .map((notifikasiItem) =>
                        NotifikasiGlobalItem(notifikasiItem))
                    .toList();

                return Expanded(
                  child: RefreshIndicator(
                    onRefresh: () async {
                      widget.reloadJumlahNotifikasi();
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
                        widget.reloadJumlahNotifikasi();
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
                                Text('Mak, belum ada info terbaru nih...',
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
