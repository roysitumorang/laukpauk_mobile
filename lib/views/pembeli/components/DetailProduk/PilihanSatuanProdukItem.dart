import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/CustomText.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class PilihanSatuanProdukItem extends StatefulWidget {
  final Map<String, dynamic> detailHargaProduk;
  final Map<String, dynamic>? satuanTerpilih;
  final Function(Map<String, dynamic> satuanSelected) onSatuanSelected;

  PilihanSatuanProdukItem(
      this.detailHargaProduk, this.satuanTerpilih, this.onSatuanSelected);

  _PilihanSatuanProdukItemState createState() =>
      _PilihanSatuanProdukItemState();
}

class _PilihanSatuanProdukItemState extends State<PilihanSatuanProdukItem> {
  bool isSatuanSelected = false;
  int quantitySelected = 1;

  Widget build(BuildContext context) {
    Map<String, dynamic> hargaProduk = widget.detailHargaProduk;

    bool _isPromo = false;
    if (hargaProduk.containsKey('isPromo')) {
      _isPromo = hargaProduk.containsKey('isPromo');
    }

    Widget _pilihanSatuanProdukItemWidget;

    String? hargaTerpilih = (widget.satuanTerpilih != null)
        ? widget.satuanTerpilih!['idSatuan']
        : null;

    if (!_isPromo) {
      int harga = int.parse(hargaProduk['harga']);
      int diskon = int.parse(hargaProduk['diskon']);

      int _hargaKotor = harga;
      harga = harga - diskon;

      _pilihanSatuanProdukItemWidget = ListTile(
          onTap: () {
            Map<String, String> hargaDanQuantity = {
              'idSatuan': hargaProduk['id'],
              'quantity': quantitySelected.toString()
            };
            widget.onSatuanSelected(hargaDanQuantity);
          },
          dense: true,
          leading: Radio<String>(
              value: hargaProduk['id'],
              groupValue: hargaTerpilih,
              onChanged: (hargaTerpilih) {
                Map<String, String> hargaDanQuantity = {
                  'idSatuan': hargaProduk['id'],
                  'quantity': quantitySelected.toString()
                };
                widget.onSatuanSelected(hargaDanQuantity);
              }),
          title: Text(hargaProduk['nama'],
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 3.75,
                  fontWeight: FontWeight.w500,
                  color: CustomColors.darkText)),
          subtitle: Padding(
            padding: const EdgeInsets.only(top: 7.5),
            child: RichText(
                text: TextSpan(
                    style: CustomText.textXS()
                        .copyWith(color: CustomColors.darkGreen),
                    text: Currency.formatRupiah(harga, 0) + ' ',
                    children: (diskon >= 1)
                        ? [
                            TextSpan(
                                text:
                                    ' ' + Currency.formatRupiah(_hargaKotor, 0),
                                style: CustomText.textXS().copyWith(
                                    color: CustomColors.textMuted,
                                    decoration: TextDecoration.lineThrough))
                          ]
                        : null)),
          ),
          trailing: (hargaProduk['jenisHarga'].toLowerCase() == 'tidak pas' &&
                  !_isPromo)
              ? DropdownButton(
                  value: quantitySelected,
                  isDense: true,
                  underline: DropdownButtonHideUnderline(child: SizedBox()),
                  items: List.generate(10, (index) => ++index)
                      .map((quantity) => DropdownMenuItem(
                          value: quantity,
                          child: Text(quantity.toString(),
                              style: TextStyle(
                                  fontSize: SizeConfig.horizontalBlock * 3))))
                      .toList(),
                  onChanged: (int? quantity) {
                    if (quantity != null) {
                      Map<String, String> hargaDanQuantity = {
                        'idSatuan': hargaProduk['id'],
                        'quantity': quantity.toString()
                      };
                      widget.onSatuanSelected(hargaDanQuantity);

                      setState(() {
                        quantitySelected = quantity;
                      });
                    }
                  })
              : Text('1 Item (Harga Pas)',
                  style: TextStyle(fontSize: SizeConfig.horizontalBlock * 3)));
    } else {
      TextStyle _textStyle = TextStyle(
          color: CustomColors.textMuted,
          fontSize: SizeConfig.horizontalBlock * 3.0);

      String _idSatuanProduk = hargaProduk['idSatuanProduk'];
      String _namaPromo = hargaProduk['namaPromo'];
      String _namaSatuan = hargaProduk['namaSatuan'];
      String _maxOrder = hargaProduk['maxOrder'];
      int _hargaSatuan = int.parse(hargaProduk['hargaSatuan']);
      int _diskonSatuan = int.parse(hargaProduk['diskonSatuan']);
      int _diskonPromo = int.parse(hargaProduk['diskonPromo']);
      String _idPromo = hargaProduk['idPromo'];

      int _hargaKotor = _hargaSatuan;
      int _hargaBersihSatuan = _hargaSatuan - _diskonSatuan - _diskonPromo;

      // String _hargaBersihSatuanString =
      //     Currency.formatRupiah(_hargaBersihSatuan, 0);

      String _idSatuan = 'promo_' + _idPromo;

      Map<String, dynamic> hargaDanQuantity = {
        'idSatuan': _idSatuan,
        'quantity': '1',
        'isPromo': true,
        'id': _idSatuanProduk,
        'idPromo': _idPromo
      };

      _pilihanSatuanProdukItemWidget = ListTile(
        onTap: () {
          widget.onSatuanSelected(hargaDanQuantity);
        },
        dense: true,
        leading: Radio(
            value: _idSatuan,
            groupValue: hargaTerpilih,
            onChanged: (hargaTerpilih) {
              widget.onSatuanSelected(hargaDanQuantity);
            }),
        title: Text(_namaPromo,
            style: TextStyle(
                fontSize: SizeConfig.horizontalBlock * 3.75,
                fontWeight: FontWeight.w500,
                color: CustomColors.darkText)),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(height: 5.0),
            RichText(
                text: TextSpan(
                    text: _namaSatuan + '  ',
                    style: _textStyle,
                    children: [
                  TextSpan(
                      text: '  ' + Currency.formatRupiah(_hargaBersihSatuan, 0),
                      style: _textStyle.copyWith(
                          color: CustomColors.green,
                          fontWeight: FontWeight.bold)),
                  TextSpan(
                      text:
                          '  ' + Currency.formatRupiah(_hargaKotor, 0) + ' - ',
                      style: _textStyle.copyWith(
                          decoration: TextDecoration.lineThrough)),
                ])),
            SizedBox(height: 2.5),
            Text('Max. Order ' + _maxOrder + 'x')
          ],
        ),
      );
    }

    return _pilihanSatuanProdukItemWidget;
  }
}
