import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/CustomButton.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/Login.dart';
import 'package:laukpauk/views/pembeli/OTPRegistrasi.dart';
import 'package:laukpauk/views/pembeli/LupaPassword.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Register extends StatefulWidget {
  final Widget? routeTo;
  Register({this.routeTo});
  _RegisterPage createState() => _RegisterPage();
}

class _RegisterPage extends State<Register> {
  String nama = '';
  String nomorHP = '';
  String konfirmasiPassword = '';
  String password = '';
  String kodeReferal = '';

  TextEditingController namaController = TextEditingController();
  TextEditingController nomorHPController = TextEditingController();
  TextEditingController passwordController = TextEditingController();
  TextEditingController konfirmasiPasswordController = TextEditingController();
  TextEditingController kodeReferalController = TextEditingController();

  bool showPassword = false;
  bool showKonfirmasiPassword = false;
  bool _isButtonRegisterBusy = false;

  void dispose() {
    disposeRegisterForm();
    super.dispose();
  }

  void initState() {
    super.initState();
    initializeRegisterForm();
  }

  void initializeRegisterForm() {
    namaController.addListener(() {
      setState(() {
        nama = namaController.text;
      });
    });
    nomorHPController.addListener(() {
      setState(() {
        nomorHP = nomorHPController.text;
      });
    });
    passwordController.addListener(() {
      setState(() {
        password = passwordController.text;
      });
    });
    konfirmasiPasswordController.addListener(() {
      setState(() {
        konfirmasiPassword = konfirmasiPasswordController.text;
      });
    });
    kodeReferalController.addListener(() {
      setState(() {
        kodeReferal = kodeReferalController.text;
      });
    });
  }

  void disposeRegisterForm() {
    namaController.dispose();
    nomorHPController.dispose();
    passwordController.dispose();
  }

  doRegistrasi() async {
    setState(() {
      _isButtonRegisterBusy = true;
    });

    Map<String, String> _registrasiData = {
      'namaLengkap': nama,
      'nomorHP': nomorHP,
      'password': password,
      'konfirmasiPassword': konfirmasiPassword,
      'kodeReferal': kodeReferal
    };

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Auth/register');
    Map<String, dynamic> _body =
        await HTTPRequest.postBody(body: _registrasiData);
    Response httpResponse = await post(_url, body: _body);

    setState(() {
      _isButtonRegisterBusy = false;
    });

    if (httpResponse.statusCode == 200) {
      Map<String, dynamic> dataObject = jsonDecode(httpResponse.body);

      String _alertMessage;
      Map returnData = dataObject['returnData'];
      if (dataObject['statusRegister'] == true) {
        _alertMessage =
            'Registrasi berhasil ! Silahkan konfirmasikan kode OTP yang kami kirim ke nomor yang anda gunakan untuk mendaftar!';

        SharedPreferences _sP = await SharedPreferences.getInstance();

        _sP.setBool('otpHasSent', true);
        _sP.setBool('otpHasConfirmed', false);
        _sP.setString('idUser', returnData['idUser'].toString());
      } else {
        _alertMessage = 'Gagal Registrasi ! ' + returnData['message'];
      }

      String _title = 'Registrasi';
      Widget _actions =
          Row(mainAxisAlignment: MainAxisAlignment.center, children: [
        TextButton(
            onPressed: () {
              (dataObject['statusRegister'] == false)
                  ? Navigator.pop(context)
                  : Navigator.pushReplacement(
                      context,
                      MaterialPageRoute(
                          builder: (_) =>
                              OTPRegistrasi(routeTo: widget.routeTo)));
            },
            child: Text((dataObject['statusRegister'] == false)
                ? 'OK'
                : 'Konfirmasi OTP'))
      ]);

      await CustomAlert.textComponent(context,
          title: _title, subTitle: _alertMessage, actions: _actions);
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        body: SingleChildScrollView(
      child: Container(
        padding: EdgeInsets.symmetric(horizontal: 10, vertical: 15),
        height: MediaQuery.of(context).size.height,
        width: MediaQuery.of(context).size.width,
        decoration: BoxDecoration(color: Color(0xFFF9F9F9)),
        child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Image.asset(ClientPath.assetsImg + '/laukpauk.png',
                  width: MediaQuery.of(context).size.width - 100),
              SizedBox(height: 50),
              // Image.asset(ClientPath.assetsImg + '/add-user-icon.png', width: 85),
              // SizedBox(height: 40),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 30),
                  decoration: BoxDecoration(
                      color: CustomColors.lightGreen,
                      borderRadius: BorderRadius.circular(50)),
                  child: TextField(
                    style: TextStyle(
                        color: CustomColors.darkText,
                        fontSize: SizeConfig.horizontalBlock * 3.5),
                    controller: namaController,
                    keyboardType: TextInputType.text,
                    cursorColor: CustomColors.darkText,
                    textAlignVertical: TextAlignVertical.center,
                    textAlign: TextAlign.left,
                    decoration: InputDecoration(
                        isDense: true,
                        contentPadding: EdgeInsets.symmetric(
                            vertical: SizeConfig.verticalBlock * 2),
                        border: InputBorder.none,
                        hintText: 'Nama Lengkap',
                        hintStyle: TextStyle(color: CustomColors.darkText)),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 30),
                  decoration: BoxDecoration(
                      color: CustomColors.lightGreen,
                      borderRadius: BorderRadius.circular(50)),
                  child: TextField(
                    style: TextStyle(
                        color: CustomColors.darkText,
                        fontSize: SizeConfig.horizontalBlock * 3.5),
                    controller: nomorHPController,
                    keyboardType: TextInputType.number,
                    cursorColor: CustomColors.darkText,
                    textAlignVertical: TextAlignVertical.center,
                    textAlign: TextAlign.left,
                    decoration: InputDecoration(
                        isDense: true,
                        contentPadding: EdgeInsets.symmetric(
                            vertical: SizeConfig.verticalBlock * 2),
                        border: InputBorder.none,
                        hintText: 'Nomor HP',
                        hintStyle: TextStyle(color: CustomColors.darkText)),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 30),
                  decoration: BoxDecoration(
                      color: CustomColors.lightGreen,
                      borderRadius: BorderRadius.circular(50)),
                  child: TextField(
                    controller: passwordController,
                    style: TextStyle(
                        color: CustomColors.darkText,
                        fontSize: SizeConfig.horizontalBlock * 3.5),
                    keyboardType: TextInputType.text,
                    obscureText: (showPassword) ? false : true,
                    cursorColor: CustomColors.darkText,
                    textAlignVertical: TextAlignVertical.center,
                    textAlign: TextAlign.left,
                    decoration: InputDecoration(
                      suffix: InkWell(
                        onTap: () {
                          setState(() {
                            showPassword = !showPassword;
                          });
                        },
                        child: Icon(
                            (showPassword)
                                ? FontAwesomeIcons.eyeSlash
                                : FontAwesomeIcons.eye,
                            size: SizeConfig.horizontalBlock * 3.5,
                            color: CustomColors.darkText),
                      ),
                      border: InputBorder.none,
                      hintText: 'Password',
                      hintStyle: TextStyle(color: CustomColors.darkText),
                      isDense: true,
                      contentPadding: EdgeInsets.symmetric(
                          vertical: SizeConfig.verticalBlock * 2),
                    ),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 30),
                  decoration: BoxDecoration(
                      color: CustomColors.lightGreen,
                      borderRadius: BorderRadius.circular(50)),
                  child: TextField(
                    controller: konfirmasiPasswordController,
                    style: TextStyle(
                        color: CustomColors.darkText,
                        fontSize: SizeConfig.horizontalBlock * 3.5),
                    keyboardType: TextInputType.text,
                    obscureText: (showKonfirmasiPassword) ? false : true,
                    cursorColor: CustomColors.darkText,
                    textAlignVertical: TextAlignVertical.center,
                    textAlign: TextAlign.left,
                    decoration: InputDecoration(
                      suffix: InkWell(
                        onTap: () {
                          setState(() {
                            showKonfirmasiPassword = !showKonfirmasiPassword;
                          });
                        },
                        child: Icon(
                            (showKonfirmasiPassword)
                                ? FontAwesomeIcons.eyeSlash
                                : FontAwesomeIcons.eye,
                            size: SizeConfig.horizontalBlock * 3.5,
                            color: CustomColors.darkText),
                      ),
                      border: InputBorder.none,
                      hintText: 'Konfirmasi Password',
                      hintStyle: TextStyle(color: CustomColors.darkText),
                      isDense: true,
                      contentPadding: EdgeInsets.symmetric(
                          vertical: SizeConfig.verticalBlock * 2),
                    ),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Padding(
                padding: EdgeInsets.symmetric(horizontal: 25),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      padding: EdgeInsets.symmetric(horizontal: 30),
                      decoration: BoxDecoration(
                          color: CustomColors.lightGreen,
                          borderRadius: BorderRadius.circular(50)),
                      child: TextField(
                        controller: kodeReferalController,
                        style: TextStyle(
                            color: CustomColors.darkText,
                            fontSize: SizeConfig.horizontalBlock * 3.5),
                        keyboardType: TextInputType.text,
                        cursorColor: CustomColors.darkText,
                        textAlignVertical: TextAlignVertical.center,
                        textAlign: TextAlign.left,
                        decoration: InputDecoration(
                          border: InputBorder.none,
                          hintText: 'Kode Referal (Opsional)',
                          hintStyle: TextStyle(color: CustomColors.darkText),
                          isDense: true,
                          contentPadding: EdgeInsets.symmetric(
                              vertical: SizeConfig.verticalBlock * 2),
                        ),
                      ),
                    ),
                    SizedBox(height: 10.0),
                    Text(
                        'Dapatkan uang belanja GRATIS ketika anda menggunakan Kode Referral dari Teman/Keluarga/Sahabat.',
                        style: TextStyle(
                            fontStyle: FontStyle.italic,
                            fontSize: SizeConfig.horizontalBlock * 3))
                  ],
                ),
              ),
              SizedBox(height: 20),
              Padding(
                  padding: EdgeInsets.symmetric(horizontal: 25),
                  child: CustomButton.button(context, 'Registrasi', () {
                    doRegistrasi();
                  },
                      color: CustomButton.themeColor,
                      isPill: true,
                      isBlock: false,
                      isBusy: _isButtonRegisterBusy)),
              SizedBox(height: 25),
              Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                Text('Sudah punya akun? Login ',
                    style:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.5)),
                SizedBox(width: 2),
                InkWell(
                  onTap: () {
                    Navigator.pushReplacement(
                        context, MaterialPageRoute(builder: (_) => Login()));
                  },
                  child: Text('disini',
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.5,
                          fontWeight: FontWeight.w700,
                          color: CustomColors.green)),
                )
              ]),
              SizedBox(height: 10),
              InkWell(
                onTap: () {
                  Navigator.pushReplacement(context,
                      MaterialPageRoute(builder: (_) => LupaPassword()));
                },
                child: Text('Lupa Password',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.5,
                        fontWeight: FontWeight.w700,
                        color: CustomColors.green)),
              )
            ]),
      ),
    ));
  }
}
