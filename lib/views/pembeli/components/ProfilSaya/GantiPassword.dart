import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomForm.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/views/pembeli/UserAndDashboard.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

class GantiPassword extends StatefulWidget {
  _GantiPasswordState createState() => _GantiPasswordState();
}

class _GantiPasswordState extends State<GantiPassword> {
  bool showUlangiPasswordBaru = false;
  bool showPasswordLama = false;
  bool showPasswordBaru = false;

  TextEditingController passwordLamaController = TextEditingController();
  TextEditingController passwordBaruController = TextEditingController();
  TextEditingController ulangiPasswordBaruController = TextEditingController();

  simpanPasswordBaru() async {
    Map<String, dynamic> dataUser = {
      'passwordLama': passwordLamaController.text,
      'passwordBaru': passwordBaruController.text,
      'konfirmasiPasswordBaru': ulangiPasswordBaruController.text
    };

    String _params = await HTTPRequest.getQueryString(authToken: true);
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: dataUser);

    Uri _url =
        Uri.parse(ServerPath.apiBaseURL + '/User/gantiPassword?' + _params);

    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);
      bool statusGantiPassword = dataObject['statusGantiPassword'];

      String text = 'Pembaharuan password gagal diproses !';
      if (statusGantiPassword) {
        text = 'Berhasil memperbaharui password anda !';
      }

      showDialog(
          context: context,
          builder: (context) => AlertDialog(
                  title: Text('Password'),
                  content: Text(text,
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.5)),
                  actions: [
                    TextButton(
                        onPressed: () {
                          (statusGantiPassword)
                              ? Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                      builder: (_) => UserAndDashboard()))
                              : Navigator.pop(context);
                        },
                        child: Text('OK'))
                  ]));
    }
  }

  void dispose() {
    super.dispose();

    passwordLamaController.dispose();
    passwordBaruController.dispose();
    ulangiPasswordBaruController.dispose();
  }

  final Color _borderColor = CustomColors.green;
  final double _borderWidth = 1.0;

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    bool isFormCompleted = (passwordBaruController.text.length >= 8 &&
        ulangiPasswordBaruController.text.length >= 8 &&
        passwordBaruController.text == ulangiPasswordBaruController.text);

    return Container(
        padding: EdgeInsets.symmetric(horizontal: 15, vertical: 15),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Password',
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 4.0,
                  fontWeight: FontWeight.w700)),
          SizedBox(height: 5),
          Text(
              'Anda dapat mengubah password anda dengan mengisi form di bawah ini',
              style: CustomText.textXS()),
          SizedBox(height: 15),
          CustomForm.label(context, label: 'Password Lama'),
          CustomForm.textField(context,
              obsecureText: !showPasswordLama,
              trailing: GestureDetector(
                  child: Icon(
                      (!showPasswordLama)
                          ? Icons.visibility
                          : Icons.visibility_off,
                      color: CustomColors.textMuted),
                  onTap: () {
                    setState(() {
                      showPasswordLama = !showPasswordLama;
                    });
                  }),
              hintText: 'Password Lama Anda',
              controller: passwordLamaController,
              borderColor: _borderColor,
              borderWidth: _borderWidth),
          SizedBox(height: 10),
          CustomForm.label(context, label: 'Password Baru'),
          CustomForm.textField(context,
              obsecureText: !showPasswordBaru,
              trailing: GestureDetector(
                  child: Icon(
                      (!showPasswordBaru)
                          ? Icons.visibility
                          : Icons.visibility_off,
                      color: CustomColors.textMuted),
                  onTap: () {
                    setState(() {
                      showPasswordBaru = !showPasswordBaru;
                    });
                  }),
              hintText: 'Minimal 8 Karakter',
              controller: passwordBaruController,
              borderColor: _borderColor,
              borderWidth: _borderWidth),
          SizedBox(height: 10),
          CustomForm.label(context, label: 'Konfirmasi Password Baru'),
          CustomForm.textField(context,
              obsecureText: !showUlangiPasswordBaru,
              trailing: GestureDetector(
                  child: Icon(
                      (!showUlangiPasswordBaru)
                          ? Icons.visibility
                          : Icons.visibility_off,
                      color: CustomColors.textMuted),
                  onTap: () {
                    setState(() {
                      showUlangiPasswordBaru = !showUlangiPasswordBaru;
                    });
                  }),
              hintText: 'Ulangi Password Baru',
              controller: ulangiPasswordBaruController,
              borderColor: _borderColor,
              borderWidth: _borderWidth),
          SizedBox(height: (isFormCompleted) ? 15 : 0),
          (isFormCompleted)
              ? CustomButton.button(context, 'Simpan Password Baru', () {
                  simpanPasswordBaru();
                }, isBlock: true, outline: true, color: CustomButton.themeColor)
              : SizedBox(),
          SizedBox(height: 10)
        ]));
  }
}
