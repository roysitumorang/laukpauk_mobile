import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/ServerPath.dart';

class VoucherItemCheckout extends StatelessWidget {
  final Map<String, dynamic> dataVoucher;
  final Map<String, dynamic>? voucherTerpilih;
  final Function(Map<String, dynamic> selectedVoucher) onVoucherSelected;
  VoucherItemCheckout(
      this.dataVoucher, this.voucherTerpilih, this.onVoucherSelected);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    int nilaiVoucher = int.parse(dataVoucher['nilai']);
    int minimumBelanja = int.parse(dataVoucher['minimumBelanja']);

    String voucherImage = ServerPath.baseURL +
        '/' +
        ServerPath.uploadGambarVoucher +
        '/compress/' +
        dataVoucher['foto'];

    bool isSelectedVoucher = (voucherTerpilih != null)
        ? (voucherTerpilih!['id'] == dataVoucher['id'])
        : false;

    return Container(
      decoration: BoxDecoration(
          borderRadius: (isSelectedVoucher)
              ? BorderRadius.circular(10)
              : BorderRadius.zero,
          color: (isSelectedVoucher) ? Color(0x6666CC99) : null),
      margin: EdgeInsets.only(bottom: 5),
      padding: (isSelectedVoucher)
          ? EdgeInsets.symmetric(horizontal: 5, vertical: 10)
          : EdgeInsets.zero,
      child: ListTile(
        onTap: () {
          onVoucherSelected(dataVoucher);
        },
        leading: ClipRRect(
          borderRadius: BorderRadius.circular(50),
          child: Image.network(voucherImage,
              width: SizeConfig.horizontalBlock * 12.5,
              height: SizeConfig.horizontalBlock * 12.5),
        ),
        subtitle: Padding(
          padding: EdgeInsets.only(top: 5),
          child: Text(
              'Potongan ' +
                  Currency.formatRupiah(nilaiVoucher, 0) +
                  ' dengan minimal belanja ' +
                  Currency.formatRupiah(minimumBelanja, 0),
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 3,
                  fontWeight: FontWeight.w500)),
        ),
        title: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(dataVoucher['nama'],
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 4,
                  fontWeight: FontWeight.w500)),
          Text('Voucher untuk mengurangi ' + dataVoucher['jenis'],
              style: TextStyle(
                  fontSize: SizeConfig.horizontalBlock * 3,
                  fontWeight: FontWeight.w500))
        ]),
      ),
    );
  }
}
