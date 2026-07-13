import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/JadwalPengantaran.dart';

class TanyaJadwal extends StatefulWidget {
  final String currentLocation;
  TanyaJadwal({required this.currentLocation});
  _TanyaJadwalState createState() => _TanyaJadwalState();
}

class _TanyaJadwalState extends State<TanyaJadwal> {
  String? kodeJadwalDeliverySelected;
  Map<String, dynamic>? jadwalDeliveryTerpilih;

  Widget build(BuildContext context) {
    String currentLocation = widget.currentLocation;

    return Column(
      children: [
        JadwalPengantaran(onJadwalDeliverySelected:
            (Map<String, dynamic> selectedJadwalDelivery) {
          setState(() {
            kodeJadwalDeliverySelected = selectedJadwalDelivery['kode'];
            jadwalDeliveryTerpilih = selectedJadwalDelivery;
          });
        }),
        Padding(
            padding: EdgeInsets.only(top: 15.0, bottom: 15.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text('Belanjaan anda akan diantar ke sekitar',
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.0)),
                SizedBox(height: 5.0),
                Text(currentLocation,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 2.75,
                        color: CustomColors.darkGreen,
                        fontWeight: FontWeight.bold))
              ],
            )),
        Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          TextButton(
              onPressed: () {
                Navigator.pop(context, null);
              },
              child: Text('Nanti Saja')),
          (kodeJadwalDeliverySelected != null && jadwalDeliveryTerpilih != null)
              ? TextButton(
                  onPressed: () async {
                    Map<String, dynamic> returnJadwalDelivery = {
                      'jadwalDeliverySelected': kodeJadwalDeliverySelected,
                      'jadwalDeliveryTerpilih': jadwalDeliveryTerpilih
                    };

                    Navigator.pop(context, returnJadwalDelivery);
                  },
                  child: Text('OK'))
              : SizedBox()
        ])
      ],
    );
  }
}
