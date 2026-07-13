import 'package:flutter/material.dart';

import 'package:laukpauk/models/UpdateHargaModel.dart';
import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

class EditSatuanProduk extends StatefulWidget {
  final Map<String, dynamic>? dataSatuan;
  final String idProduk, appBarTitle;
  final bool isUpdate;
  EditSatuanProduk(
      {required this.idProduk,
      this.dataSatuan,
      this.appBarTitle = 'Edit Satuan Produk',
      this.isUpdate = true});
  _EditSatuanProdukState createState() => _EditSatuanProdukState();
}

class _EditSatuanProdukState extends State<EditSatuanProduk> {
  bool isBusyState = false;

  TextEditingController namaController = TextEditingController();
  TextEditingController hargaController = TextEditingController();
  TextEditingController diskonController = TextEditingController();
  TextEditingController jenisHargaController = TextEditingController();
  TextEditingController keteranganController = TextEditingController();

  void dispose() {
    super.dispose();
    namaController.dispose();
    hargaController.dispose();
    diskonController.dispose();
    jenisHargaController.dispose();
    keteranganController.dispose();
  }

  void initData() {
    Map<String, dynamic>? satuan = widget.dataSatuan;
    if (satuan != null) {
      namaController.text = satuan['nama'];
      hargaController.text = satuan['harga'];
      diskonController.text = satuan['diskon'];
      jenisHargaController.text = satuan['jenisHarga'];
      keteranganController.text = satuan['keterangan'];
    } else {
      hargaController.text = '0';
      diskonController.text = '0';
    }
  }

  void initState() {
    super.initState();
    this.initData();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String jenisHarga = jenisHargaController.text;
    bool isFormComplete = namaController.text.length >= 1 &&
        hargaController.text.length >= 1 &&
        diskonController.text.length >= 1 &&
        jenisHarga.length >= 1;

    TextStyle noteJenisHargaTextStyle = TextStyle(
        fontSize: SizeConfig.horizontalBlock * 3.25,
        color: (jenisHarga.length >= 1)
            ? CustomColors.textInfo
            : CustomColors.textMuted);
    Widget noteJenisHarga = Text(
        'Silahkan tentukan jenis harga pada satuan (harga) produk  ini.',
        style: noteJenisHargaTextStyle);

    if (jenisHarga.toLowerCase() == 'pas') {
      noteJenisHarga = Text(
          'Tipe ini adalah produk anda TIDAK memiliki quantity. Contoh jual cabe dengan harga Rp. 2.000, tidak ada quantity 1 s/d 10.',
          style: noteJenisHargaTextStyle);
    }
    if (jenisHarga.toLowerCase() == 'tidak pas') {
      noteJenisHarga = Text(
          'Tipe ini adalah produk anda memiliki quantity. Pembeli dapat memilih quantity 1 s/d 10.',
          style: noteJenisHargaTextStyle);
    }

    String actions = (widget.isUpdate) ? 'edit (perbaharui)' : 'tambah';

    print('idProduk ESP ' + widget.idProduk);

    return Scaffold(
        appBar: AppBar(
            title: Text(widget.appBarTitle),
            backgroundColor: CustomColors.green),
        body: SingleChildScrollView(
          child: Padding(
            padding: EdgeInsets.symmetric(horizontal: 5, vertical: 10),
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Card(
                elevation: 5,
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10)),
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 15, horizontal: 15),
                  child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                            (widget.isUpdate)
                                ? 'Edit Satuan Produk'
                                : 'Satuan Produk Baru',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 4,
                                fontWeight: FontWeight.w700)),
                        Text(
                            'Silahkan $actions data harga produk dengan mengisi form berikut ini :',
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.25)),
                        SizedBox(height: 15),
                        Container(
                          margin: EdgeInsets.only(bottom: 7.5),
                          child: Container(
                              height: SizeConfig.horizontalBlock * 15,
                              padding: EdgeInsets.only(right: 15, left: 25),
                              decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(10),
                                  color: Color(0xFFF0F8FF)),
                              child: TextField(
                                  controller: namaController,
                                  keyboardType: TextInputType.text,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  decoration: InputDecoration(
                                      border: InputBorder.none,
                                      labelText: 'Nama Satuan (Harga)',
                                      contentPadding: EdgeInsets.only(
                                          top:
                                              SizeConfig.horizontalBlock * 3.25,
                                          bottom: SizeConfig.horizontalBlock *
                                              3.25),
                                      labelStyle: TextStyle(
                                          fontFamily: 'WorkSans',
                                          fontWeight: FontWeight.w500,
                                          color: Colors.grey)))),
                        ),
                        SizedBox(height: 5),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded(
                              child: Container(
                                margin: EdgeInsets.only(bottom: 7.5),
                                child: Container(
                                    height: SizeConfig.horizontalBlock * 15,
                                    padding:
                                        EdgeInsets.only(right: 15, left: 25),
                                    decoration: BoxDecoration(
                                        borderRadius: BorderRadius.circular(10),
                                        color: Color(0xFFF0F8FF)),
                                    child: TextField(
                                        controller: hargaController,
                                        keyboardType: TextInputType.number,
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5),
                                        decoration: InputDecoration(
                                            border: InputBorder.none,
                                            labelText: 'Harga (Rupiah)',
                                            contentPadding: EdgeInsets.only(
                                                top:
                                                    SizeConfig.horizontalBlock *
                                                        3.25,
                                                bottom:
                                                    SizeConfig.horizontalBlock *
                                                        3.25),
                                            labelStyle: TextStyle(
                                                fontFamily: 'WorkSans',
                                                fontWeight: FontWeight.w500,
                                                color: Colors.grey)))),
                              ),
                            ),
                            SizedBox(width: 5),
                            Expanded(
                              child: Container(
                                margin: EdgeInsets.only(bottom: 7.5),
                                child: Container(
                                    height: SizeConfig.horizontalBlock * 15,
                                    padding:
                                        EdgeInsets.only(right: 15, left: 25),
                                    decoration: BoxDecoration(
                                        borderRadius: BorderRadius.circular(10),
                                        color: Color(0xFFF0F8FF)),
                                    child: TextField(
                                        controller: diskonController,
                                        keyboardType: TextInputType.number,
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    3.5),
                                        decoration: InputDecoration(
                                            border: InputBorder.none,
                                            labelText: 'Diskon (Rupiah)',
                                            contentPadding: EdgeInsets.only(
                                                top:
                                                    SizeConfig.horizontalBlock *
                                                        3.25,
                                                bottom:
                                                    SizeConfig.horizontalBlock *
                                                        3.25),
                                            labelStyle: TextStyle(
                                                fontFamily: 'WorkSans',
                                                fontWeight: FontWeight.w500,
                                                color: Colors.grey)))),
                              ),
                            ),
                          ],
                        ),
                        SizedBox(height: 5),
                        Container(
                          margin: EdgeInsets.only(bottom: 7.5),
                          child: Container(
                              height: SizeConfig.horizontalBlock * 12,
                              width: SizeConfig.horizontalBlock * 100,
                              padding: EdgeInsets.only(right: 15, left: 25),
                              decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(10),
                                  color: Color(0xFFF0F8FF)),
                              child: DropdownButton<String>(
                                  isExpanded: true,
                                  underline: DropdownButtonHideUnderline(
                                      child: SizedBox()),
                                  value: jenisHargaController.text,
                                  items: <DropdownMenuItem<String>>[
                                    DropdownMenuItem(
                                        child: Text('Jenis Harga',
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25)),
                                        value: ''),
                                    DropdownMenuItem(
                                        child: Text('Harga Pas',
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25)),
                                        value: 'pas'),
                                    DropdownMenuItem(
                                        child: Text('Harga Tidak Pas',
                                            style: TextStyle(
                                                fontSize:
                                                    SizeConfig.horizontalBlock *
                                                        3.25)),
                                        value: 'tidak pas')
                                  ],
                                  onChanged: (valueSelected) {
                                    if (valueSelected != null) {
                                      setState(() {
                                        jenisHargaController.text =
                                            valueSelected;
                                      });
                                    }
                                  })),
                        ),
                        Padding(
                          padding: EdgeInsets.only(top: 0, bottom: 10),
                          child: noteJenisHarga,
                        ),
                        SizedBox(height: 5),
                        Container(
                          margin: EdgeInsets.only(bottom: 7.5),
                          child: Container(
                              height: SizeConfig.horizontalBlock * 15,
                              padding: EdgeInsets.only(right: 15, left: 25),
                              decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(10),
                                  color: Color(0xFFF0F8FF)),
                              child: TextField(
                                  controller: keteranganController,
                                  keyboardType: TextInputType.text,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.5),
                                  decoration: InputDecoration(
                                      border: InputBorder.none,
                                      labelText: 'Keterangan',
                                      contentPadding: EdgeInsets.only(
                                          top:
                                              SizeConfig.horizontalBlock * 3.25,
                                          bottom: SizeConfig.horizontalBlock *
                                              3.25),
                                      labelStyle: TextStyle(
                                          fontFamily: 'WorkSans',
                                          fontWeight: FontWeight.w500,
                                          color: Colors.grey)))),
                        ),
                        (isFormComplete)
                            ? Padding(
                                padding: EdgeInsets.only(top: 15, bottom: 10),
                                child: CustomButton.button(context, 'Simpan',
                                    () async {
                                  setState(() {
                                    isBusyState = true;
                                  });

                                  Map<String, dynamic>? dataSatuan =
                                      widget.dataSatuan;

                                  String idSatuan = (dataSatuan != null)
                                      ? dataSatuan['id']
                                      : null;
                                  String idProduk = widget.idProduk;

                                  Map<String, dynamic> dataSatuanProduk = {
                                    'idProduk': idProduk,
                                    'nama': namaController.text,
                                    'harga': hargaController.text,
                                    'diskon': diskonController.text,
                                    'jenisHarga': jenisHargaController.text,
                                    'keterangan': keteranganController.text
                                  };

                                  bool statusEditSatuan =
                                      await UpdateHargaModel.editSatuanProduk(
                                          idSatuan, dataSatuanProduk);
                                  return Navigator.pop(
                                      context, statusEditSatuan);
                                },
                                    color: CustomButton.themeColor,
                                    isPill: true,
                                    isBlock: true,
                                    isBusy: isBusyState))
                            : SizedBox()
                      ]),
                ),
              )
            ]),
          ),
        ));
  }
}
