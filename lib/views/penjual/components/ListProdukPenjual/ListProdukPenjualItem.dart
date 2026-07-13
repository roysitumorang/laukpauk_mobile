import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:laukpauk/views/penjual/TambahProduk.dart';

import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/ProdukPenjualModel.dart';
import 'package:laukpauk/views/penjual/DetailProdukPenjual.dart';

class ListProdukPenjualItem extends StatelessWidget {
  final Map<String, dynamic> dataProduk;
  final Function(bool statusHapusProduk) hapusProdukCallback;
  final Function(bool statusEditProduk) editProdukCallback;

  ListProdukPenjualItem(this.dataProduk,
      {required this.hapusProdukCallback, required this.editProdukCallback});
  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String? namaKategoriProduk;
    if (dataProduk['namaKategoriProduk'] != null) {
      namaKategoriProduk = dataProduk['namaKategoriProduk'];
    }

    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 10),
      child: Container(
        margin: EdgeInsets.only(bottom: 5),
        decoration: BoxDecoration(
            color: CustomColors.nearlyWhite,
            borderRadius: BorderRadius.circular(10)),
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 5, vertical: 7.5),
          child: InkWell(
            onTap: () {
              Navigator.push(
                  context,
                  MaterialPageRoute(
                      builder: (_) =>
                          DetailProdukPenjual(dataProduk['id'], 'satuan')));
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 7.5),
              child: ListTile(
                  leading: ClipRRect(
                      borderRadius: BorderRadius.circular(50),
                      child: (dataProduk['foto'].length >= 1)
                          ? Image.network(
                              ServerPath.baseURL +
                                  '/' +
                                  ServerPath.uploadGambarProduk +
                                  '/compress/' +
                                  dataProduk['foto'],
                              width: SizeConfig.horizontalBlock * 13.5,
                              height: SizeConfig.horizontalBlock * 13.5,
                            )
                          : Image.asset(
                              ClientPath.assetsImg + '/placeholder-150.png')),
                  title: Text(dataProduk['nama'].toUpperCase(),
                      style: TextStyle(
                          color: CustomColors.textSuccess,
                          fontSize: SizeConfig.horizontalBlock * 3.25,
                          fontWeight: FontWeight.w700)),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                          (namaKategoriProduk != null)
                              ? namaKategoriProduk
                              : 'Kategori Produk Tidak Diketahui',
                          style: TextStyle(
                              fontStyle: (namaKategoriProduk == null)
                                  ? FontStyle.italic
                                  : null,
                              fontSize: SizeConfig.horizontalBlock * 3)),
                      (dataProduk['keterangan'].length >= 1)
                          ? Padding(
                              padding: EdgeInsets.only(top: 5),
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text('Keterangan Produk',
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3,
                                          fontWeight: FontWeight.w500)),
                                  SizedBox(height: 1.5),
                                  Text(dataProduk['keterangan'],
                                      style: TextStyle(
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3))
                                ],
                              ),
                            )
                          : SizedBox(),
                      Padding(
                        padding: EdgeInsets.only(top: 10.0),
                        child: InkWell(
                          onTap: () async {
                            bool? statusEditProduk = await Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (_) => TambahProduk(
                                        dataProduk['id'], 'satuan')));
                            if (statusEditProduk != null) {
                              editProdukCallback(statusEditProduk);
                            }
                          },
                          child: Text('Edit Produk',
                              style: TextStyle(
                                  color: CustomColors.textWarning,
                                  fontSize: SizeConfig.horizontalBlock * 2.75)),
                        ),
                      )
                    ],
                  ),
                  trailing: InkWell(
                    onTap: () async {
                      String namaProduk = dataProduk['nama'];
                      List<TextButton> konfirmasiActions = [
                        TextButton(
                            onPressed: () {
                              Navigator.pop(context, false);
                            },
                            child: Text('Batal')),
                        TextButton(
                            onPressed: () {
                              Navigator.pop(context, true);
                            },
                            child: Text('Lanjutkan'))
                      ];
                      bool? konfirmasiHapus =
                          await ProdukPenjualModel.showKonfirmasi(context,
                              title: 'Konfirmasi Hapus Produk',
                              content:
                                  'Apakah anda yakin akan menghapus produk $namaProduk ini?',
                              actions: konfirmasiActions);

                      if (konfirmasiHapus != null && konfirmasiHapus) {
                        bool statusHapusProduk =
                            await ProdukPenjualModel.hapusProduk(
                                dataProduk['id']);

                        hapusProdukCallback(statusHapusProduk);
                      }
                    },
                    child: Icon(FontAwesomeIcons.trash,
                        color: CustomColors.textDanger,
                        size: SizeConfig.horizontalBlock * 3.5),
                  )),
            ),
          ),
        ),
      ),
    );
  }
}
