import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:http/http.dart';
import 'package:laukpauk/libraries/HTTPRequest.dart';

import 'package:laukpauk/views/pembeli/components/EditKeranjangItem/KeranjangItemInput.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

class EditKeranjangItem extends StatefulWidget {
  final Map<String, dynamic> dataKeranjangItem;
  EditKeranjangItem(this.dataKeranjangItem);

  _EditKeranjangItemState createState() => _EditKeranjangItemState();
}

class _EditKeranjangItemState extends State<EditKeranjangItem> {
  String satuanSelected = '0';
  String quantity = '';
  String catatanTambahan = '';

  void initState() {
    super.initState();

    setState(() {
      satuanSelected = widget.dataKeranjangItem['idSatuan'];
      quantity = widget.dataKeranjangItem['quantity'];
      catatanTambahan = widget.dataKeranjangItem['catatan'];
    });
  }

  Future<bool> simpanPerubahan() async {
    String idItem = widget.dataKeranjangItem['idCart'];
    Map<String, String> httpBody = {
      'quantity': quantity,
      'catatan': catatanTambahan
    };
    Map<String, dynamic> _body = await HTTPRequest.postBody(body: httpBody);

    Uri _url = Uri.parse(ServerPath.apiBaseURL + '/Keranjang/saveCart/$idItem');
    Response httpResponse = await post(_url, body: _body);
    if (httpResponse.statusCode == 200) {
      Map dataObject = jsonDecode(httpResponse.body);

      bool statusSaveCart = dataObject['statusAddCart'];

      return statusSaveCart;
    }

    return false;
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    Map<String, dynamic> _detailKeranjangItem = widget.dataKeranjangItem;
    String _jenisSatuan = _detailKeranjangItem['jenisSatuan'];
    String? _idPromo = _detailKeranjangItem['idPromo'];

    Map dataKeranjangItem = widget.dataKeranjangItem;
    List<String> quantities = [
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      '10'
    ];

    List<DropdownMenuItem<String>> quantityItems = quantities.map((q) {
      return DropdownMenuItem(
          value: q,
          child: Text(q,
              style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25)));
    }).toList();

    if (!quantities.contains(quantity)) {
      DropdownMenuItem<String> _item =
          DropdownMenuItem(child: Text(quantity), value: quantity);
      quantityItems.add(_item);
    }

    return Scaffold(
        backgroundColor: CustomColors.aliceBlue,
        appBar: AppBar(
          backgroundColor: CustomColors.green,
          title: Text('Edit Keranjang Item'),
          leading: IconButton(
              icon: Icon(FontAwesomeIcons.arrowLeft),
              onPressed: () {
                Navigator.pop(context);
              }),
        ),
        body: SingleChildScrollView(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 5, vertical: 5),
            child: Card(
              elevation: 8,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10)),
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 15, vertical: 25),
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.start,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(dataKeranjangItem['namaProduk'],
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 4,
                              fontWeight: FontWeight.w700)),
                      SizedBox(height: 5),
                      Text('Silahkan sesuaikan dengan kebutuhan belanja anda',
                          style: TextStyle(
                              color: CustomColors.grey,
                              fontSize: SizeConfig.horizontalBlock * 3.5)),
                      (_jenisSatuan.toLowerCase() == 'tidak pas' &&
                              _idPromo == null)
                          ? Container(
                              margin: EdgeInsets.only(top: 25.0),
                              padding: EdgeInsets.symmetric(
                                  vertical: 10, horizontal: 25),
                              decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(10),
                                  color: Color(0xFFF0F8FF)),
                              child: Row(
                                mainAxisAlignment:
                                    MainAxisAlignment.spaceBetween,
                                children: [
                                  Text('Quantity',
                                      style: TextStyle(
                                          fontSize: SizeConfig.horizontalBlock *
                                              3.5)),
                                  DropdownButton<String>(
                                      elevation: 0,
                                      isDense: true,
                                      underline: DropdownButtonHideUnderline(
                                          child: SizedBox()),
                                      value: quantity,
                                      items: quantityItems,
                                      onChanged: (newQuantity) {
                                        if (newQuantity != null) {
                                          setState(() {
                                            quantity = newQuantity;
                                          });
                                        }
                                      })
                                ],
                              ),
                            )
                          : SizedBox(),
                      (_jenisSatuan == 'tidak pas')
                          ? SizedBox(height: 10)
                          : SizedBox(),
                      KeranjangItemInput(
                          'Catatan Tambahan',
                          dataKeranjangItem['catatan'],
                          TextInputType.text, (data) {
                        catatanTambahan = data;
                      }, height: SizeConfig.horizontalBlock * 25, minLines: 5),
                      Container(height: 1, color: Colors.grey[300]),
                      SizedBox(height: 20),
                      CustomButton.button(context, 'Simpan Perubahan',
                          () async {
                        bool statusSaveCart = await simpanPerubahan();

                        Navigator.pop(context, statusSaveCart);
                      }, color: CustomButton.themeColor, isBlock: true)
                    ]),
              ),
            ),
          ),
        ));
  }
}
