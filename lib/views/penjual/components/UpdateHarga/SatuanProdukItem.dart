import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/models/UpdateHargaModel.dart';
import 'package:laukpauk/views/penjual/components/UpdateHarga/EditSatuanProduk.dart';

class SatuanProdukItem extends StatefulWidget {
  final Map<String, dynamic> satuan;
  final bool showOptions, updateAble;
  final dynamic nomorUrut;
  final Function onSatuanProdukDeleted, onSatuanProdukUpdated;

  SatuanProdukItem(this.satuan,
      {this.nomorUrut = false,
      this.showOptions = false,
      this.updateAble = true,
      required this.onSatuanProdukDeleted,
      required this.onSatuanProdukUpdated});
  _SatuanProdukItemState createState() => _SatuanProdukItemState();
}

class _SatuanProdukItemState extends State<SatuanProdukItem> {
  TextEditingController hargaController = TextEditingController();
  TextEditingController diskonController = TextEditingController();

  void initData() {
    Map satuan = widget.satuan;
    hargaController.text = satuan['harga'];
    diskonController.text = satuan['diskon'];
  }

  void dispose() {
    super.dispose();
    hargaController.dispose();
    diskonController.dispose();
  }

  void initState() {
    super.initState();
    this.initData();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    bool updateAble = widget.updateAble;
    //updateable berarti akan ditampilkan sebagai form

    Map<String, dynamic> satuan = widget.satuan;

    int harga =
        int.parse((updateAble) ? hargaController.text : satuan['harga']);
    int diskon =
        int.parse((updateAble) ? diskonController.text : satuan['diskon']);
    String keterangan = satuan['keterangan'];

    return Padding(
      padding: EdgeInsets.only(bottom: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(children: [
            (widget.nomorUrut != false)
                ? Text(widget.nomorUrut.toString() + '.',
                    style: TextStyle(
                        fontSize: SizeConfig.horizontalBlock * 3.25,
                        fontWeight: FontWeight.w700))
                : SizedBox(),
            SizedBox(width: (widget.nomorUrut != false) ? 10 : 0),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(widget.satuan['nama'],
                      style: TextStyle(
                          fontSize: SizeConfig.horizontalBlock * 3.25)),
                  SizedBox(height: 2.5),
                  Container(
                    padding: EdgeInsets.symmetric(horizontal: 5, vertical: 1.5),
                    decoration: BoxDecoration(
                        color: CustomColors.textPrimary,
                        borderRadius: BorderRadius.circular(10)),
                    child: Text(
                        'Harga ' + widget.satuan['jenisHarga'].toUpperCase(),
                        style: TextStyle(
                            color: CustomColors.nearlyWhite,
                            fontSize: SizeConfig.horizontalBlock * 2.5)),
                  )
                ],
              ),
            ),
            SizedBox(width: 15),
            Expanded(
              child: Material(
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(50)),
                child: (widget.updateAble)
                    ? Padding(
                        padding: EdgeInsets.only(left: 15, right: 15),
                        child: TextField(
                            controller: hargaController,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25),
                            keyboardType: TextInputType.number,
                            decoration: InputDecoration(
                                isDense: true,
                                contentPadding: EdgeInsets.only(
                                    top: SizeConfig.verticalBlock * 2,
                                    bottom: SizeConfig.verticalBlock * 2),
                                hintText: 'Harga',
                                border: InputBorder.none)))
                    : Text(Currency.formatRupiah(harga, 0),
                        textAlign: TextAlign.right,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
              ),
            ),
            SizedBox(width: 5),
            Expanded(
              child: Material(
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(50)),
                child: (widget.updateAble)
                    ? Padding(
                        padding: EdgeInsets.only(left: 15, right: 15),
                        child: TextField(
                            controller: diskonController,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25),
                            keyboardType: TextInputType.number,
                            decoration: InputDecoration(
                                isDense: true,
                                contentPadding: EdgeInsets.only(
                                    top: SizeConfig.verticalBlock * 2,
                                    bottom: SizeConfig.verticalBlock * 2),
                                hintText: 'Diskon',
                                border: InputBorder.none)),
                      )
                    : Text(Currency.formatRupiah(diskon, 0),
                        textAlign: TextAlign.right,
                        style: TextStyle(
                            fontSize: SizeConfig.horizontalBlock * 3.25)),
              ),
            ),
            (widget.showOptions)
                ? Padding(
                    padding: EdgeInsets.only(left: 15),
                    child: Row(children: [
                      InkWell(
                        onTap: () async {
                          Map<String, dynamic> satuan = widget.satuan;
                          bool? statusEditSatuan = await Navigator.push(
                              context,
                              MaterialPageRoute(
                                  builder: (_) => EditSatuanProduk(
                                        dataSatuan: satuan,
                                        idProduk: satuan['idProduk'],
                                      )));

                          if (statusEditSatuan != null) {
                            if (statusEditSatuan) {
                              widget.onSatuanProdukUpdated();
                            } else {
                              showDialog(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                          title: Text('Update Satuan Produk',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      4,
                                                  fontWeight: FontWeight.w700)),
                                          content: Text(
                                              'Gagal memperbaharui data satuan (harga) produk. Silahkan coba lagi !',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.25)),
                                          actions: [
                                            TextButton(
                                                onPressed: () {
                                                  Navigator.pop(context);
                                                },
                                                child: Text('OK'))
                                          ]));
                            }
                          }
                        },
                        child: Icon(FontAwesomeIcons.edit,
                            size: SizeConfig.horizontalBlock * 3.25,
                            color: CustomColors.textWarning),
                      ),
                      SizedBox(width: 10),
                      InkWell(
                        onTap: () async {
                          Map<String, dynamic> satuan = widget.satuan;
                          bool konfirmasiHapus =
                              await UpdateHargaModel.konfirmasiHapusSatuan(
                                  context, satuan);

                          if (konfirmasiHapus) {
                            String idSatuanProduk = satuan['id'];
                            bool statusHapusSatuan =
                                await UpdateHargaModel.hapusSatuan(
                                    idSatuanProduk);

                            if (statusHapusSatuan) {
                              widget.onSatuanProdukDeleted();
                            } else {
                              showDialog(
                                  context: context,
                                  builder: (context) => AlertDialog(
                                          title: Text('Hapus Satuan Produk',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      4,
                                                  fontWeight: FontWeight.w700)),
                                          content: Text(
                                              'Gagal menghapus satuan (harga) produk. Silahkan coba lagi !',
                                              style: TextStyle(
                                                  fontSize: SizeConfig
                                                          .horizontalBlock *
                                                      3.25)),
                                          actions: [
                                            TextButton(
                                                onPressed: () {
                                                  Navigator.pop(context);
                                                },
                                                child: Text('OK'))
                                          ]));
                            }
                          }
                        },
                        child: Icon(FontAwesomeIcons.trash,
                            size: SizeConfig.horizontalBlock * 3.25,
                            color: CustomColors.textDanger),
                      )
                    ]),
                  )
                : SizedBox()
          ]),
          (keterangan.length >= 1)
              ? Container(
                  width: SizeConfig.horizontalBlock * 100,
                  decoration: BoxDecoration(
                      color: Color(0x4466CC99),
                      borderRadius: BorderRadius.circular(10)),
                  padding: EdgeInsets.only(top: 5),
                  margin: EdgeInsets.only(top: 5),
                  child: Padding(
                    padding: EdgeInsets.symmetric(vertical: 5, horizontal: 10),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Keterangan',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25,
                                fontWeight: FontWeight.w700)),
                        SizedBox(height: 2),
                        Text(keterangan,
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3)),
                        SizedBox(height: 3.5)
                      ],
                    ),
                  ))
              : SizedBox()
        ],
      ),
    );
  }
}
