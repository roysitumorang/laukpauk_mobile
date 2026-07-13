import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/global-components/CustomAlert.dart';
import 'package:laukpauk/global-components/Loading.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';
import 'package:laukpauk/models/HeaderModel.dart';
import 'package:laukpauk/view-models/HeaderViewModel.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:onesignal_flutter/onesignal_flutter.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/views/pembeli/Register.dart';
import 'package:laukpauk/views/pembeli/LupaPassword.dart';
import 'package:laukpauk/views/pembeli/OTPVerifikasiUser.dart';

import 'package:laukpauk/views/SplashScreen.dart';

class Login extends StatefulWidget {
  final Widget? routeTo;
  Login({this.routeTo});
  _LoginState createState() => _LoginState();
}

class _LoginState extends State<Login> {
  String nomorHP = '';
  String password = '';
  TextEditingController nomorHPController = TextEditingController();
  TextEditingController passwordController = TextEditingController();

  bool showPassword = false;
  bool _isButtonLoginBusyState = false;

  bool _isInitializingState = false;

  void dispose() {
    nomorHPController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  void initState() {
    super.initState();
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
  }

  void cekLogin() async {
    setState(() {
      _isButtonLoginBusyState = true;
    });

    String cartCode = await LocalStorage.getCartCode();

    String _params = await HTTPRequest.getQueryString(
        params: 'nomorHP=$nomorHP&password=$password&cartCode=$cartCode');
    Uri _url = Uri.parse(
        ServerPath.apiBaseURL + '/Auth/loginAuthentication?' + _params);
    Response httpResponse = await get(_url);

    if (httpResponse.statusCode == 200) {
      Map cekLoginResponse = jsonDecode(httpResponse.body);

      if (cekLoginResponse['loginAuthentication'] == true) {
        setState(() {
          _isInitializingState = true;
        });

        SharedPreferences sharedPreferences =
            await SharedPreferences.getInstance();
        await sharedPreferences.setString(
            'authToken', cekLoginResponse['authToken']);
        await sharedPreferences.setString('idUser', cekLoginResponse['idUser']);

        await OneSignal.shared.setExternalUserId(cekLoginResponse['idUser']);
        await LocalStorage.setExternalUserID(cekLoginResponse['idUser']);

        int _jumlahSeluruhNotifikasi = await HeaderModel.getJumlahNotifikasi();
        int _jumlahCart = await HeaderModel.getJumlahCart();

        Provider.of<HeaderViewModel>(context, listen: false).jumlahCart =
            _jumlahCart;
        Provider.of<HeaderViewModel>(context, listen: false).jumlahNotifikasi =
            _jumlahSeluruhNotifikasi;

        bool isVerified = cekLoginResponse['isVerified'];

        setState(() {
          _isButtonLoginBusyState = false;
          _isInitializingState = false;
        });

        await Navigator.pushReplacement(
            context,
            MaterialPageRoute(
                builder: (_) => (!isVerified)
                    ? OTPVerifikasiUser(routeTo: widget.routeTo)
                    : (widget.routeTo == null)
                        ? SplashScreen()
                        : widget.routeTo!));
      } else {
        setState(() {
          _isButtonLoginBusyState = false;
        });

        String _title = 'Autentikasi User';
        String _subTitle =
            'Kombinasi nomor telepon dan password tidak sesuai !';
        Widget _anotherWidget = Text(
            'Silahkan ke halaman lupa password untuk memulihkan password anda !',
            textAlign: TextAlign.center,
            style: TextStyle(
                fontSize: SizeConfig.horizontalBlock * 2.75,
                color: CustomColors.textMuted));
        Widget _actions =
            Row(mainAxisAlignment: MainAxisAlignment.center, children: [
          TextButton(
              child: Text('Tutup'),
              onPressed: () {
                Navigator.pop(context);
              }),
          TextButton(
              child: Text('Lupa Password'),
              onPressed: () {
                Navigator.pushReplacement(
                    context, MaterialPageRoute(builder: (_) => LupaPassword()));
              })
        ]);

        await CustomAlert.textComponent(context,
            title: _title,
            subTitle: _subTitle,
            actions: _actions,
            moreWidgets: _anotherWidget);
      }
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        body: Stack(
      children: [
        Center(
          child: SingleChildScrollView(
              child: Padding(
            padding: EdgeInsets.symmetric(
                horizontal: SizeConfig.horizontalBlock * 10.0),
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Image.asset(ClientPath.assetsImg + '/laukpauk.png',
                      width: MediaQuery.of(context).size.width - 100),
                  SizedBox(height: 50),
                  Container(
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
                  SizedBox(height: 10),
                  Container(
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
                              vertical: SizeConfig.verticalBlock * 2)),
                    ),
                  ),
                  SizedBox(height: 20),
                  CustomButton.button(context, 'Login', () {
                    cekLogin();
                  },
                      color: CustomButton.themeColor,
                      isPill: true,
                      isBusy: _isButtonLoginBusyState),
                  SizedBox(height: 25),
                  Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                    Text('Belum punya akun? Registrasi ',
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.5)),
                    SizedBox(width: 2),
                    InkWell(
                      onTap: () {
                        Navigator.pushReplacement(
                            context,
                            MaterialPageRoute(
                                builder: (_) =>
                                    Register(routeTo: widget.routeTo)));
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
          )),
        ),
        (_isInitializingState)
            ? Container(
                decoration: BoxDecoration(color: Color(0xBBFFFFFF)),
                child: Center(
                    child: Loading(
                        title: 'Loading',
                        subTitle:
                            'Mohon sebentar, aplikasi sedang memuat konten ...')))
            : SizedBox(),
      ],
    ));
  }
}
