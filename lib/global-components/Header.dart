import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/global-components/HeaderSearch.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomText.dart';

import 'package:laukpauk/models/ADJDModel.dart';
import 'package:laukpauk/models/HeaderModel.dart';

import 'package:laukpauk/views/pembeli/Notifikasi.dart';
import 'package:laukpauk/views/pembeli/Keranjang.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

class Header extends StatefulWidget {
  final bool askADJD;
  final dynamic askADJDListener;
  final void Function(Map<String, dynamic> dataFromCallback)? adjdSetCallback;
  Header({this.adjdSetCallback, this.askADJD = false, this.askADJDListener});
  HeaderState createState() => HeaderState();
}

class HeaderState extends State<Header> {
  final FocusNode _focusNode = FocusNode();

  Future<void> headerADJD() async {
    Map<String, dynamic> adjdFromHeader = await HeaderModel.adjd(context);

    if (widget.adjdSetCallback != null) {
      widget.adjdSetCallback!(adjdFromHeader);
    }
  }

  void initState() {
    super.initState();
    if (widget.askADJD) {
      headerADJD();
    }
  }

  void didUpdateWidget(oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.askADJDListener != widget.askADJDListener) {
      headerADJD();
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);
    CustomText(context);

    return Container(
      padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top),
      decoration: BoxDecoration(color: CustomColors.green),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Consumer<HeaderViewModel>(builder: (context, headerStateManager, _) {
          bool isADJDSet = headerStateManager.isADJDSet;
          String alamatDelivery = headerStateManager.alamatDelivery;

          return Container(
            child: (isADJDSet)
                ? Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                        Container(
                          padding: EdgeInsets.symmetric(
                              horizontal: 10.0, vertical: 7.5),
                          child: Text('Diantarin ke mana ?',
                              style: CustomText.contentSize(
                                  color: CustomColors.nearlyWhite,
                                  fontWeight: FontWeight.bold)),
                        ),
                        InkWell(
                            onTap: () async {
                              bool? ubahLokasi =
                                  await HeaderModel.showAlamatPengantaran(
                                      context, alamatDelivery);
                              if (ubahLokasi != null) {
                                if (ubahLokasi) {
                                  ADJDModel.deleteAlamatDanJadwalDelivery();
                                  headerADJD();
                                }
                              }
                            },
                            child: Container(
                              decoration: BoxDecoration(
                                  color: CustomColors.nearlyWhite),
                              padding: EdgeInsets.symmetric(
                                  horizontal: 15, vertical: 7.5),
                              child: Text(
                                  (alamatDelivery.length >= 30)
                                      ? alamatDelivery.substring(0, 30) + ' ...'
                                      : alamatDelivery,
                                  style: CustomText.contentSize(
                                      color: CustomColors.textMuted,
                                      fontWeight: FontWeight.bold)),
                            ))
                      ])
                : SizedBox(),
          );
        }),
        Container(
            height: 50,
            child: Row(children: [
              Expanded(
                child: Container(
                  child: TextField(
                    focusNode: _focusNode,
                    onTap: () {
                      _focusNode.unfocus();
                      showDialog(
                          useSafeArea: false,
                          context: context,
                          builder: (showDialogContext) => HeaderSearch());
                    },
                    textAlign: TextAlign.left,
                    textAlignVertical: TextAlignVertical.center,
                    cursorColor: Colors.white,
                    style: TextStyle(color: Colors.white),
                    decoration: InputDecoration(
                        prefixIcon: Icon(FontAwesomeIcons.search,
                            size: SizeConfig.horizontalBlock * 4,
                            color: Colors.white),
                        hintText: 'Cari Toko atau Sayuran Segar',
                        hintStyle: TextStyle(
                            color: Colors.white,
                            fontSize: SizeConfig.horizontalBlock * 4),
                        border: InputBorder.none),
                  ),
                ),
              ),
              Container(
                  color: CustomColors.nearlyWhite,
                  height: 50.5,
                  child: Row(children: [
                    Stack(children: [
                      IconButton(
                          icon: Icon(FontAwesomeIcons.shoppingCart,
                              size: SizeConfig.horizontalBlock * 4.5,
                              color: CustomColors.green),
                          onPressed: () {
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (context) => Keranjang()));
                          }),
                      Consumer<HeaderViewModel>(
                          builder: (context, headerStateManager, _) {
                        int jumlahKeranjang = headerStateManager.jumlahCart;
                        return Positioned(
                            top: 5,
                            right: 0,
                            child: Container(
                              constraints: BoxConstraints(
                                  minWidth: 17.5, minHeight: 17.5),
                              padding: EdgeInsets.all(3),
                              decoration: BoxDecoration(
                                  shape: BoxShape.circle, color: Colors.red),
                              child: Text(
                                  (jumlahKeranjang >= 10)
                                      ? '9+'
                                      : jumlahKeranjang.toString(),
                                  style: TextStyle(
                                      color: Colors.white,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.5),
                                  textAlign: TextAlign.center),
                            ));
                      })
                    ]),
                    Stack(children: [
                      IconButton(
                          icon: Icon(FontAwesomeIcons.bell,
                              size: SizeConfig.horizontalBlock * 4.5,
                              color: CustomColors.green),
                          onPressed: () {
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (_) => Notifikasi()));
                          }),
                      Consumer<HeaderViewModel>(
                          builder: (context, headerStateManager, _) {
                        int jumlahSeluruhNotifikasi =
                            headerStateManager.jumlahNotifikasi;
                        return Positioned(
                            top: 5,
                            right: 5,
                            child: Container(
                              constraints: BoxConstraints(
                                  minWidth: 17.5, minHeight: 17.5),
                              padding: EdgeInsets.all(3),
                              decoration: BoxDecoration(
                                  shape: BoxShape.circle, color: Colors.red),
                              child: Text(
                                  (jumlahSeluruhNotifikasi >= 10)
                                      ? '9+'
                                      : jumlahSeluruhNotifikasi.toString(),
                                  style: TextStyle(
                                      color: Colors.white,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 2.5),
                                  textAlign: TextAlign.center),
                            ));
                      })
                    ])
                  ]))
            ]))
      ]),
    );
  }
}
