import 'package:flutter/material.dart';
import 'package:laukpauk/models/KontenModel.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/Konten.dart';
import 'package:laukpauk/global-components/Loading.dart';

class KontenAplikasi extends StatefulWidget {
  _KontenAplikasiState createState() => _KontenAplikasiState();
}

class _KontenAplikasiState extends State<KontenAplikasi> {
  Widget build(BuildContext context) {
    return Container(
        child: FutureBuilder<List>(
            future: KontenModel.getKontenAplikasi(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                if (snapshot.data != null) {
                  List kontenAplikasi = snapshot.data!;
                  if (kontenAplikasi.length >= 1) {
                    List<Widget> listKonten =
                        kontenAplikasi.map((item) => Konten(item)).toList();
                    return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: listKonten);
                  }
                }
                return SizedBox();
              } else {
                return Padding(
                    padding: EdgeInsets.symmetric(vertical: 25.0),
                    child: Loading());
              }
            }));
  }
}
