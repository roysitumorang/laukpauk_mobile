import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart' show XFile, ImageSource;
import 'package:laukpauk/global-components/CustomForm.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/DefaultImage.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/TambahProdukModel.dart';

import 'package:laukpauk/models/KategoriProdukModel.dart';
import 'package:laukpauk/models/ImagePickerModel.dart';
import 'package:laukpauk/models/PaketModel.dart';
import 'package:laukpauk/models/ProdukModel.dart';

class TambahProduk extends StatefulWidget {
  final String? idProduk;
  final String jenisProduk;
  TambahProduk(this.idProduk, this.jenisProduk);
  _TambahProdukState createState() => _TambahProdukState();
}

class _TambahProdukState extends State<TambahProduk> {
  final TextEditingController _namaProdukController = TextEditingController();
  final TextEditingController _keteranganProdukController =
      TextEditingController();
  final TextEditingController _hargaProdukController = TextEditingController();
  final TextEditingController _diskonProdukController = TextEditingController();

  final double _borderWidth = 1.0;
  final double _sizedBoxHeight = 15.0;

  final Color _borderColor = CustomColors.green;

  String _kategoriProdukSelected = '', _jenisProdukSelected = '';
  XFile? _imageProdukData;
  dynamic detailProdukState = false;
  bool _isButtonSimpanBusy = false;

  List _kategoriProdukState = [];

  void initEditProduk() async {
    String? idProduk = widget.idProduk;
    String jenisProduk = widget.jenisProduk;

    if (jenisProduk.toLowerCase() == 'satuan') {
      dynamic detailProduk = await ProdukModel.getDetailProduk(idProduk);
      if (detailProduk != false) {
        _namaProdukController.text = detailProduk['nama'];
        _keteranganProdukController.text = detailProduk['keterangan'];

        setState(() {
          _jenisProdukSelected = 'satuan';
          _kategoriProdukSelected = detailProduk['idKategoriProduk'];
          detailProdukState = detailProduk;
        });
      }
    } else {
      dynamic detailPaket = await PaketModel.detailPaket(idProduk);
      if (detailPaket != false) {
        _namaProdukController.text = detailPaket['nama'];
        _keteranganProdukController.text = detailPaket['keterangan'];
        _hargaProdukController.text = detailPaket['harga'];
        _diskonProdukController.text = detailPaket['diskon'];

        setState(() {
          _jenisProdukSelected = 'paket';
          detailProdukState = detailPaket;
        });
      }
    }
  }

  void dispose() {
    super.dispose();
    _namaProdukController.dispose();
    _keteranganProdukController.dispose();
    _hargaProdukController.dispose();
    _diskonProdukController.dispose();
  }

  void initState() {
    super.initState();
    initialization();
  }

  void initialization() async {
    String? idProduk = widget.idProduk;

    List kategoriProduk = await KategoriProdukModel.getKategoriProduk();
    setState(() {
      _kategoriProdukState = kategoriProduk;
    });
    if (idProduk != null) {
      this.initEditProduk();
    } else {
      _hargaProdukController.text = '0';
      _diskonProdukController.text = '0';
    }
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String? idProduk = widget.idProduk;
    String jenisProduk = widget.jenisProduk;
    String jenisProdukLC = jenisProduk.toLowerCase();
    Widget uploadFotoWidget = Container(
      child: Opacity(
          opacity: 0.35,
          child: Image.asset(ClientPath.assetsImg + '/upload-icon.png')),
      padding: EdgeInsets.all(SizeConfig.horizontalBlock * 10.0),
    );

    String namaProduk = _namaProdukController.text;
    String hargaProduk = _hargaProdukController.text;
    String diskonProduk = _diskonProdukController.text;
    String keteranganProduk = _keteranganProdukController.text;

    bool _isJenisProdukNotEmpty = _jenisProdukSelected != '';

    bool isFormCompleted = false;
    if (_jenisProdukSelected == 'satuan') {
      isFormCompleted = (namaProduk.length >= 1 &&
              _kategoriProdukSelected != '0' &&
              _isJenisProdukNotEmpty)
          ? true
          : false;
    }

    if (_jenisProdukSelected == 'paket') {
      isFormCompleted = (namaProduk.length >= 1 &&
              hargaProduk.length >= 1 &&
              diskonProduk.length >= 1 &&
              _isJenisProdukNotEmpty)
          ? true
          : false;
    }

    if (idProduk == null) {
      if (_imageProdukData != null) {
        uploadFotoWidget = Container(
          decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(10),
              image: DecorationImage(
                  image: FileImage(File(_imageProdukData!.path)),
                  fit: BoxFit.cover)),
        );
      }
    } else {
      if (detailProdukState != false) {
        String fotoProdukLC =
            detailProdukState['foto'].toString().toLowerCase();

        if (jenisProdukLC == 'satuan') {
          String defaultProdukLC = DefaultImage.defaultProduk.toLowerCase();

          bool isDefaultImage = defaultProdukLC == fotoProdukLC;

          ImageProvider _uploadFotoWidgetImage;
          if (_imageProdukData == null) {
            _uploadFotoWidgetImage = NetworkImage(ServerPath.baseURL +
                '/' +
                ServerPath.uploadGambarProduk +
                '/' +
                fotoProdukLC);
          } else {
            _uploadFotoWidgetImage = FileImage(File(_imageProdukData!.path));
          }

          uploadFotoWidget = Container(
            decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                image: DecorationImage(
                    image: _uploadFotoWidgetImage,
                    fit: (isDefaultImage) ? BoxFit.contain : BoxFit.cover)),
          );
        }

        if (jenisProdukLC == 'paket') {
          String defaultPaketLC = DefaultImage.defaultPaket.toLowerCase();

          bool isDefaultImage = defaultPaketLC == fotoProdukLC;

          ImageProvider _uploadFotoWidgetImage;
          if (_imageProdukData == null) {
            _uploadFotoWidgetImage = NetworkImage(ServerPath.baseURL +
                '/' +
                ServerPath.uploadGambarPaket +
                '/' +
                detailProdukState['foto']);
          } else {
            _uploadFotoWidgetImage = FileImage(File(_imageProdukData!.path));
          }

          uploadFotoWidget = Container(
            decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(10),
                image: DecorationImage(
                    image: _uploadFotoWidgetImage,
                    fit: (isDefaultImage) ? BoxFit.contain : BoxFit.cover)),
          );
        }
      }
    }

    double contentFontSize = SizeConfig.horizontalBlock * 3.0;

    List<SelectBoxItem> _jenisProdukItems = [
      SelectBoxItem('satuan', 'Produk Satuan'),
      SelectBoxItem('paket', 'Produk Paketan')
    ];

    List<SelectBoxItem> _kategoriItems = _kategoriProdukState
        .map((kategori) => SelectBoxItem(kategori['id'], kategori['nama']))
        .toList();

    return Scaffold(
      appBar: AppBar(
          title: Text((idProduk == null) ? 'Tambah Produk' : 'Update Produk'),
          backgroundColor: CustomColors.green),
      backgroundColor: CustomColors.nearlyWhite,
      body: SingleChildScrollView(
          child: Padding(
        padding: EdgeInsets.symmetric(horizontal: 15.0, vertical: 20.0),
        child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text((idProduk == null) ? 'Tambah Produk' : 'Update Produk',
                  style: TextStyle(
                      fontWeight: FontWeight.w700,
                      fontSize: SizeConfig.horizontalBlock * 4)),
              SizedBox(height: 5),
              Text(
                  (idProduk == null)
                      ? 'Anda dapat menambahkan produk dengan mengisi data produk anda di bawah ini'
                      : 'Silahkan perbaharui data produk dengan mengisi form di bawah ini',
                  style: TextStyle(fontSize: contentFontSize)),
              SizedBox(height: _sizedBoxHeight),
              GestureDetector(
                onTap: () async {
                  ImageSource? imageSource =
                      await ImagePickerModel.showImagePicker(context);
                  XFile? pickedFile =
                      await ImagePickerModel.takePicture(context, imageSource);
                  if (pickedFile != null) {
                    setState(() {
                      _imageProdukData = pickedFile;
                    });
                  }
                },
                child: Container(
                  width: double.infinity,
                  height: SizeConfig.horizontalBlock * 50.0,
                  child: uploadFotoWidget,
                ),
              ),
              SizedBox(height: _sizedBoxHeight),
              CustomForm.label(context, label: 'Nama Produk'),
              CustomForm.textField(context,
                  hintText: 'Contoh : Daun Bawang',
                  controller: _namaProdukController,
                  borderColor: _borderColor,
                  borderWidth: _borderWidth),
              SizedBox(height: _sizedBoxHeight),
              CustomForm.label(context, label: 'Jenis Produk'),
              CustomForm.selectBox(context,
                  items: _jenisProdukItems,
                  value: _jenisProdukSelected, onChanged: (selectedValue) {
                setState(() {
                  _jenisProdukSelected = selectedValue;
                });
              },
                  defaultItemText: 'Pilih Jenis Produk',
                  defaultItemValue: '',
                  borderColor: _borderColor,
                  borderWidth: _borderWidth),
              (_isJenisProdukNotEmpty && _jenisProdukSelected == 'satuan')
                  ? Padding(
                      padding: EdgeInsets.only(top: 15.0),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          CustomForm.label(context, label: 'Kategori Produk'),
                          CustomForm.selectBox(context,
                              items: _kategoriItems,
                              value: _kategoriProdukSelected,
                              onChanged: (selectedValue) {
                            setState(() {
                              _kategoriProdukSelected = selectedValue;
                            });
                          },
                              defaultItemText: 'Pilih Kategori Produk',
                              defaultItemValue: '',
                              borderColor: _borderColor,
                              borderWidth: _borderWidth),
                        ],
                      ),
                    )
                  : SizedBox(),
              SizedBox(height: 5.0),
              (_isJenisProdukNotEmpty)
                  ? (_jenisProdukSelected == 'paket')
                      ? Padding(
                          padding: EdgeInsets.only(top: 10.0),
                          child: Row(
                              mainAxisSize: MainAxisSize.min,
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Expanded(
                                  child: Column(
                                    mainAxisSize: MainAxisSize.min,
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      CustomForm.label(context, label: 'Harga'),
                                      CustomForm.textField(context,
                                          leading: Text('Rp.'),
                                          leadingAndHintTextSpacing: 15.0,
                                          hintText: 'Rp.',
                                          controller: _hargaProdukController,
                                          borderColor: _borderColor,
                                          borderWidth: _borderWidth)
                                    ],
                                  ),
                                ),
                                SizedBox(width: 10.0),
                                Expanded(
                                    child: Column(
                                  mainAxisSize: MainAxisSize.min,
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    CustomForm.label(context, label: 'Diskon'),
                                    CustomForm.textField(context,
                                        leading: Text('Rp.'),
                                        leadingAndHintTextSpacing: 15.0,
                                        hintText: 'Rp.',
                                        controller: _diskonProdukController,
                                        borderColor: _borderColor,
                                        borderWidth: _borderWidth)
                                  ],
                                )),
                              ]),
                        )
                      : Text(
                          'Anda akan dapat menentukan harga - harga produk setelah anda menyimpan produk ini',
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 3,
                              color: CustomColors.textMuted))
                  : SizedBox(),
              SizedBox(height: _sizedBoxHeight),
              CustomForm.label(context, label: 'Keterangan (Opsional)'),
              CustomForm.textField(context,
                  hintText:
                      'Contoh : Produk bagus, diambil langsung dari kebun dijual dengan harga murah',
                  controller: _keteranganProdukController,
                  borderColor: _borderColor,
                  borderWidth: _borderWidth),
              SizedBox(height: 20),
              (isFormCompleted)
                  ? CustomButton.button(context, 'Simpan Data Produk',
                      () async {
                      String _jenisProdukSelectedLC =
                          _jenisProdukSelected.toLowerCase();

                      Map<String, dynamic> dataProdukMap = {
                        'nama': namaProduk,
                        'jenis': _jenisProdukSelectedLC,
                        'keterangan': keteranganProduk,
                        'imageProdukData': _imageProdukData
                      };

                      if (_jenisProdukSelectedLC == 'satuan') {
                        dataProdukMap['kategori'] = _kategoriProdukSelected;
                      } else {
                        dataProdukMap['harga'] = hargaProduk;
                        dataProdukMap['diskon'] = diskonProduk;
                      }

                      setState(() {
                        _isButtonSimpanBusy = true;
                      });
                      bool statusSaveProduk =
                          await TambahProdukModel.saveProduk(context,
                              idProduk: idProduk ?? '',
                              dataProdukMap: dataProdukMap);
                      setState(() {
                        _isButtonSimpanBusy = false;
                      });
                      Navigator.pop(context, statusSaveProduk);
                    },
                      isBlock: true,
                      isBusy: _isButtonSimpanBusy,
                      color: CustomButton.themeColor)
                  : SizedBox()
            ]),
      )),
    );
  }
}
