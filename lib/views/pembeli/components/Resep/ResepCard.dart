import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:url_launcher/url_launcher.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/views/pembeli/DetailResep.dart';

class ResepCard extends StatelessWidget {
  final dynamic dataResep;
  ResepCard(this.dataResep);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String _keterangan = dataResep['keterangan'];
    if (_keterangan.length >= 31) {
      _keterangan = _keterangan.substring(0, 30) + ' .. read more';
    }

    SnackBar snackBarURLTidakValid = SnackBar(
        content: Text('URL Tidak Valid!'), duration: Duration(seconds: 1));

    return Container(
      margin: EdgeInsets.only(bottom: 10),
      child: Material(
        elevation: 8,
        borderRadius: BorderRadius.circular(10),
        child: InkWell(
          onTap: () {
            Navigator.push(
                context,
                MaterialPageRoute(
                    builder: (_) => DetailResep(dataResep['id'])));
          },
          borderRadius: BorderRadius.circular(10),
          child: Stack(
            children: [
              Container(
                height: SizeConfig.verticalBlock * 20,
                padding: EdgeInsets.symmetric(vertical: 15),
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(10),
                    image: DecorationImage(
                        fit: BoxFit.cover,
                        image: NetworkImage(ServerPath.baseURL +
                            '/' +
                            ServerPath.uploadGambarResep +
                            '/' +
                            dataResep['foto']))),
              ),
              Positioned(
                  top: 0,
                  left: 0,
                  bottom: 0,
                  right: 0,
                  child: Container(
                    decoration: BoxDecoration(
                        color: Color.fromRGBO(0, 0, 0, 0.3),
                        borderRadius: BorderRadius.circular(10)),
                  )),
              Positioned(
                  bottom: 10,
                  child: Padding(
                    padding: EdgeInsets.symmetric(horizontal: 15),
                    child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(dataResep['nama'].toUpperCase(),
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 4.0,
                                  fontWeight: FontWeight.w700,
                                  color: CustomColors.nearlyWhite)),
                          SizedBox(height: 3.5),
                          Text(_keterangan,
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3.0,
                                  color: CustomColors.nearlyWhite))
                        ]),
                  )),
              Positioned(
                  top: 10,
                  right: 0,
                  child: InkWell(
                    onTap: () async {
                      Uri link = Uri.parse(dataResep['fbLink']);
                      if (await canLaunchUrl(link)) {
                        launchUrl(link);
                      } else {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(snackBarURLTidakValid);
                      }
                    },
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: 15),
                      child: Icon(FontAwesomeIcons.telegramPlane,
                          size: 20, color: CustomColors.nearlyWhite),
                    ),
                  )),
              Positioned(
                  top: 10,
                  right: 30,
                  child: InkWell(
                    onTap: () async {
                      Uri link = Uri.parse(dataResep['igLink']);
                      if (await canLaunchUrl(link)) {
                        launchUrl(link);
                      } else {
                        ScaffoldMessenger.of(context)
                            .showSnackBar(snackBarURLTidakValid);
                      }
                    },
                    child: Padding(
                      padding: EdgeInsets.symmetric(horizontal: 15),
                      child: Icon(FontAwesomeIcons.instagram,
                          size: 20, color: CustomColors.nearlyWhite),
                    ),
                  ))
            ],
          ),
        ),
      ),
    );
  }
}
