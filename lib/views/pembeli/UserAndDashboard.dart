import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Header.dart';
import 'package:laukpauk/global-components/Footer.dart';
import 'package:laukpauk/global-components/Loading.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/UserAndDashboard/Transaksi.dart';
import 'package:laukpauk/views/pembeli/components/UserAndDashboard/UserMenu.dart';

import 'package:laukpauk/views/pembeli/Login.dart';

import 'package:laukpauk/models/AuthModel.dart';
import 'package:laukpauk/models/UserModel.dart';
import 'package:laukpauk/views/pembeli/components/HomePage/WalletAndVoucher.dart';

class UserAndDashboard extends StatefulWidget {
  _UserAndDashboardState createState() => _UserAndDashboardState();
}

class _UserAndDashboardState extends State<UserAndDashboard> {
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    final double _profilePicWidthAndHeight = SizeConfig.horizontalBlock * 37.5;

    return Scaffold(
        backgroundColor: CustomColors.nearlyWhite,
        body: FutureBuilder<bool>(
            future: AuthModel.isLogin(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                if (snapshot.data != null) {
                  bool isLogin = snapshot.data!;
                  if (isLogin) {
                    return FutureBuilder(
                      future: UserModel.getDetailActiveUser(),
                      builder: (context, snapshot) {
                        if (snapshot.hasData) {
                          dynamic dataUser = snapshot.data;

                          if (dataUser != false) {
                            return Column(
                                mainAxisAlignment: MainAxisAlignment.start,
                                children: [
                                  Header(),
                                  Expanded(
                                    child: Center(
                                      child: RefreshIndicator(
                                        onRefresh: () async {
                                          setState(() {});
                                        },
                                        child: SingleChildScrollView(
                                          padding: EdgeInsets.only(
                                              top: 25.0, bottom: 25.0),
                                          child: Column(
                                              mainAxisSize: MainAxisSize.min,
                                              children: [
                                                Padding(
                                                    padding: EdgeInsets.only(
                                                        left: 15.0,
                                                        right: 15.0),
                                                    child: Column(
                                                        mainAxisSize:
                                                            MainAxisSize.min,
                                                        children: [
                                                          Container(
                                                              width:
                                                                  _profilePicWidthAndHeight,
                                                              height:
                                                                  _profilePicWidthAndHeight,
                                                              decoration: BoxDecoration(
                                                                  image: DecorationImage(
                                                                      fit: BoxFit
                                                                          .fill,
                                                                      image: NetworkImage(ServerPath.baseURL +
                                                                          '/' +
                                                                          ServerPath
                                                                              .uploadGambarUser +
                                                                          '/' +
                                                                          dataUser[
                                                                              'foto'])),
                                                                  borderRadius:
                                                                      BorderRadius
                                                                          .circular(
                                                                              _profilePicWidthAndHeight))),
                                                          SizedBox(
                                                              height: 15.0),
                                                          Text(dataUser['nama'],
                                                              style: CustomText
                                                                      .textLG()
                                                                  .copyWith(
                                                                      color: CustomColors
                                                                          .green,
                                                                      fontWeight:
                                                                          FontWeight
                                                                              .w700)),
                                                          Text(
                                                              dataUser[
                                                                  'kodeReferal'],
                                                              style: TextStyle(
                                                                  color:
                                                                      CustomColors
                                                                          .green,
                                                                  fontSize:
                                                                      SizeConfig
                                                                              .horizontalBlock *
                                                                          3.5,
                                                                  fontWeight:
                                                                      FontWeight
                                                                          .w700)),
                                                          SizedBox(
                                                              height: 10.0),
                                                          (dataUser['alamat']
                                                                      .length >=
                                                                  1)
                                                              ? Padding(
                                                                  padding: EdgeInsets
                                                                      .symmetric(
                                                                          horizontal:
                                                                              10.0),
                                                                  child: Text(
                                                                      dataUser[
                                                                          'alamat'],
                                                                      textAlign:
                                                                          TextAlign
                                                                              .center,
                                                                      style: TextStyle(
                                                                          fontSize: SizeConfig.horizontalBlock *
                                                                              3.35,
                                                                          color:
                                                                              CustomColors.textMuted)),
                                                                )
                                                              : Text(
                                                                  'Alamat belum ditentukan',
                                                                  style: TextStyle(
                                                                      fontSize:
                                                                          SizeConfig.horizontalBlock *
                                                                              3.35,
                                                                      fontStyle:
                                                                          FontStyle
                                                                              .italic,
                                                                      color: CustomColors
                                                                          .textMuted)),
                                                          SizedBox(
                                                              height: 15.0),
                                                          WalletAndVoucher()
                                                        ])),
                                                SizedBox(height: 15.0),
                                                Padding(
                                                  padding: EdgeInsets.symmetric(
                                                      horizontal: 5),
                                                  child: Transaksi(dataUser),
                                                ),
                                                Padding(
                                                    padding:
                                                        EdgeInsets.symmetric(
                                                            horizontal: 5),
                                                    child: UserMenu())
                                              ]),
                                        ),
                                      ),
                                    ),
                                  )
                                ]);
                          } else {
                            return Container(
                              width: MediaQuery.of(context).size.width,
                              height: MediaQuery.of(context).size.height,
                              child: Column(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  children: [
                                    Image.asset(
                                        ClientPath.assetsImg + '/empty.png',
                                        width: SizeConfig.horizontalBlock * 30),
                                    SizedBox(height: 10),
                                    Text('Data Akun Anda tidak Ditemukan',
                                        style: TextStyle(
                                            color: CustomColors.textDanger,
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5))
                                  ]),
                            );
                          }
                        } else {
                          return Loading(
                              showLoadingImage: true,
                              circularProgressIndicatorSize: Size(50.0, 50.0));
                        }
                      },
                    );
                  }
                }
                return Login();
              } else {
                return Loading();
              }
            }),
        bottomNavigationBar: Footer());
  }
}
