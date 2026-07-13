import 'package:flutter/material.dart';

import 'package:laukpauk/views/pembeli/components/Notifikasi/DetailNotifikasiGlobal.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class NotifikasiGlobalItem extends StatelessWidget {
  final Map<String, dynamic> dataNotifikasiItem;

  NotifikasiGlobalItem(this.dataNotifikasiItem);
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> notifikasi = dataNotifikasiItem;

    String foto = notifikasi['foto'];
    String pesan = notifikasi['pesan'];
    if (pesan.length >= 75) {
      pesan = pesan.substring(0, 75) + '..';
    }
    String judul = notifikasi['judul'];
    bool isRead = notifikasi['isRead'];
    String idNotifikasi = notifikasi['idNotifikasi'];
    String createdAt = notifikasi['createdAt'];

    double left = SizeConfig.horizontalBlock * 5.0;
    double right = SizeConfig.horizontalBlock * 5.0;

    return InkWell(
      onTap: () {
        Navigator.push(
            context,
            MaterialPageRoute(
                builder: (_) => DetailNotifikasiGlobal(idNotifikasi)));
      },
      child: Container(
        padding: EdgeInsets.only(bottom: 5.0),
        decoration: BoxDecoration(
            color: (!isRead) ? Color(0x2566CC99) : Color(0xFFFFFFFF)),
        child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 10),
            child: ListTile(
                contentPadding: EdgeInsets.only(
                    bottom: 5.0, top: 10.0, left: left, right: right),
                leading: ClipRRect(
                    borderRadius: BorderRadius.circular(50),
                    child: (foto.length >= 1)
                        ? Image.network(
                            ServerPath.baseURL +
                                '/' +
                                ServerPath.uploadGambarPushNotifikasi +
                                '/compress/$foto',
                            fit: BoxFit.cover,
                            width: SizeConfig.horizontalBlock * 13.5,
                            height: SizeConfig.horizontalBlock * 13.5,
                          )
                        : Image.asset(
                            ClientPath.assetsImg + '/laukpauk-icon.png',
                            width: 50.0,
                            height: 50.0)),
                title: Text(judul.toUpperCase(),
                    style: TextStyle(
                        color: (!isRead)
                            ? CustomColors.textSuccess
                            : CustomColors.nearlyBlack,
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        fontWeight: FontWeight.w700)),
                subtitle: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(pesan,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3)),
                    SizedBox(height: 5.0),
                    Text(createdAt,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 2.5))
                  ],
                ))),
      ),
    );
  }
}
