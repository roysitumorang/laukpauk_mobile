import 'dart:io';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:image_picker/image_picker.dart';
import 'package:http/http.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/ImagePickerModel.dart';

class GantiFotoProfil extends StatefulWidget {
  final dynamic detailDataUser;
  final Function(bool statusGantiFotoProfil) gantiFotoProfilCallback;

  GantiFotoProfil(
      {required this.detailDataUser, required this.gantiFotoProfilCallback});
  _GantiFotoProfilState createState() => _GantiFotoProfilState();
}

class _GantiFotoProfilState extends State<GantiFotoProfil> {
  XFile? fotoProfilBaruData;
  bool isButtonBusyState = false;

  Future<bool> saveFotoProfil() async {
    String _params = await HTTPRequest.getQueryString(authToken: true);

    Map<String, dynamic> dataFotoProfil = {
      'fotoProfil': base64Encode(await fotoProfilBaruData!.readAsBytes())
    };

    Map<String, dynamic> _body =
        await HTTPRequest.postBody(body: dataFotoProfil);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/updateFotoProfil?' + _params);
    Response httpResponse = await post(_url, body: _body);

    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusGantiFotoProfil = dataObject['saveFotoProfil'];

      return statusGantiFotoProfil;
    }
    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);
    Map<String, dynamic> dataUser = widget.detailDataUser;

    ImageProvider _image;
    if (fotoProfilBaruData == null) {
      _image = NetworkImage(ServerPath.baseURL +
          '/' +
          ServerPath.uploadGambarUser +
          '/' +
          dataUser['foto']);
    } else {
      _image = FileImage(File(fotoProfilBaruData!.path));
    }

    return Card(
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        child: Container(
            padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text('Foto Profil',
                  style: TextStyle(
                      fontSize: SizeConfig.horizontalBlock * 4.0,
                      fontWeight: FontWeight.w700)),
              SizedBox(height: 5),
              Text(
                  'Anda dapat mengubah foto profil anda dengan pilihan di bawah ini',
                  style: CustomText.textXS()),
              SizedBox(height: 15),
              Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
                Stack(children: [
                  Container(
                    width: SizeConfig.horizontalBlock * 25,
                    height: SizeConfig.horizontalBlock * 25,
                    decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(50),
                        image:
                            DecorationImage(fit: BoxFit.cover, image: _image)),
                  ),
                  Positioned(
                    right: -7.5,
                    bottom: -7.5,
                    child: InkWell(
                      onTap: () async {
                        ImageSource? imageSource =
                            await ImagePickerModel.showImagePicker(context);
                        XFile? pickedFile = await ImagePickerModel.takePicture(
                            context, imageSource);

                        if (pickedFile != null) {
                          setState(() {
                            fotoProfilBaruData = pickedFile;
                          });
                        }
                      },
                      child: Container(
                          width: 45,
                          height: 45,
                          child: Icon(FontAwesomeIcons.camera,
                              color: CustomColors.nearlyWhite, size: 20),
                          decoration: BoxDecoration(
                              color: CustomColors.green,
                              borderRadius: BorderRadius.circular(25))),
                    ),
                  )
                ]),
                SizedBox(width: 10),
                Expanded(
                  child: Column(
                      mainAxisAlignment: MainAxisAlignment.start,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(dataUser['nama'].toUpperCase(),
                            style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: CustomColors.green,
                                fontSize: SizeConfig.horizontalBlock * 4.25)),
                        SizedBox(height: 2),
                        Text(dataUser['kodeReferal'],
                            style: TextStyle(
                                fontWeight: FontWeight.w700,
                                color: CustomColors.textInfo,
                                fontSize: SizeConfig.horizontalBlock * 3.5)),
                        SizedBox(height: 7.5),
                        Text(
                            'Bergabung pada ' +
                                dataUser['createdAt'].toUpperCase(),
                            style: CustomText.textXS()
                                .copyWith(color: CustomColors.textMuted)),
                        SizedBox(height: 1.5),
                        Text(
                            'Terakhir kali memperbaharui profil pada ' +
                                dataUser['createdAt'].toUpperCase(),
                            style: CustomText.textXS()
                                .copyWith(color: CustomColors.textMuted))
                      ]),
                )
              ]),
              (fotoProfilBaruData != null)
                  ? Padding(
                      padding: EdgeInsets.only(top: 15),
                      child: CustomButton.button(
                          context, 'Simpan Perubahan Foto Profil', () async {
                        setState(() {
                          isButtonBusyState = true;
                        });

                        bool statusGantiFotoProfil = await saveFotoProfil();

                        XFile? fotoProfilBaruDataLocalVariable =
                            fotoProfilBaruData;
                        if (statusGantiFotoProfil) {
                          fotoProfilBaruDataLocalVariable = null;
                          widget.gantiFotoProfilCallback(statusGantiFotoProfil);
                        }
                        setState(() {
                          isButtonBusyState = false;
                          fotoProfilBaruData = fotoProfilBaruDataLocalVariable;
                        });
                      },
                          color: CustomButton.themeColor,
                          outline: true,
                          isBlock: true,
                          isBusy: isButtonBusyState))
                  : SizedBox()
            ])));
  }
}
