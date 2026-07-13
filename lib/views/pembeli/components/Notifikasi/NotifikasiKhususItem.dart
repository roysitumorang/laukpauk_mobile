import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

import 'package:laukpauk/models/NotifikasiModel.dart';

import 'package:laukpauk/models/HeaderModel.dart';

class NotifikasiKhususItem extends StatelessWidget {
  final Map<String, dynamic> dataNotifikasiKhususItem;
  final void Function(bool statusToggleNotifikasi) toggleNotifikasiCallback;

  NotifikasiKhususItem(this.dataNotifikasiKhususItem,
      {required this.toggleNotifikasiCallback});
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> notifikasi = dataNotifikasiKhususItem;

    String id = notifikasi['id'];
    String judul = notifikasi['judul'];
    String isi = notifikasi['isi'];
    String routeName = notifikasi['routeName'];
    String routeParams = notifikasi['routeParams'];
    bool isRead = notifikasi['isRead'];
    String jenisNotifikasi = notifikasi['jenisNotifikasi'];
    String waktu = notifikasi['waktu'];

    // String waktu = notifikasi['waktu'];

    return InkWell(
      onTap: () async {
        bool setAsRead;
        if (!isRead) {
          setAsRead =
              await NotifikasiModel.setAs(id, 'read', 'khususuntukkamu');
        } else {
          setAsRead = true;
        }

        if (setAsRead) {
          Navigator.pushNamed(context, routeName, arguments: routeParams);
          toggleNotifikasiCallback(true);

          int jumlahNotifikasi = await HeaderModel.getJumlahNotifikasi();
          Provider.of<HeaderViewModel>(context, listen: false)
              .jumlahNotifikasi = jumlahNotifikasi;
        }
      },
      child: Container(
        decoration: BoxDecoration(
            color: (!isRead) ? Color(0x2566CC99) : Color(0xFFFFFFFF)),
        child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 10, vertical: 10),
            child: ListTile(
                leading: Container(
                  width: SizeConfig.horizontalBlock * 10.0,
                  height: SizeConfig.horizontalBlock * 10.0,
                  child: Image.asset(ClientPath.assetsImg +
                      '/notifikasi/$jenisNotifikasi.png'),
                ),
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
                    Text(isi,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3)),
                    SizedBox(height: 3.5),
                    Text(waktu,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 2.5)),
                    SizedBox(height: 5),
                    Text((isRead) ? 'Sudah dibaca' : 'Belum dibaca',
                        style: TextStyle(
                            fontWeight: FontWeight.w700,
                            color: CustomColors.textInfo))
                  ],
                ))),
      ),
    );
  }
}

/*

                trailing: InkWell(
                  onTap: () async {
                    bool statusToggleNotifikasi =
                        await NotifikasiFunctions.toggleStatus(id,
                            jenisNotifikasi: 'khususuntukkamu');
                    if (statusToggleNotifikasi != null) {
                      toggleNotifikasiCallback(statusToggleNotifikasi);

                      int jumlahSeluruhNotifikasi =
                          await HeaderModel.getJumlahNotifikasi();
                      Provider.of<HeaderViewModel>(context, listen: false)
                          .jumlahNotifikasi = jumlahSeluruhNotifikasi;
                    }
                  },
                  child: Icon(
                      (isRead)
                          ? FontAwesomeIcons.eyeSlash
                          : FontAwesomeIcons.eye,
                      size: SizeConfig.horizontalBlock * 4.5,
                      color: (isRead) ? CustomColors.textWarning : CustomColors.green),
                )
 */
