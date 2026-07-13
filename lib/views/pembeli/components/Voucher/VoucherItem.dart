import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/Currency.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/DetailVoucher.dart';

class VoucherItem extends StatelessWidget {
  final Map dataVoucher;
  VoucherItem(this.dataVoucher);

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    final double containerHeight = SizeConfig.verticalBlock * 20;

    return Container(
      margin: EdgeInsets.only(bottom: 5),
      child: Stack(
        children: [
          Card(
            shape:
                RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            child: Column(
              children: [
                InkWell(
                  onTap: () {
                    Navigator.push(
                        context,
                        MaterialPageRoute(
                            builder: (_) => DetailVoucher(dataVoucher['id'])));
                  },
                  child: Container(
                      height: containerHeight,
                      decoration: BoxDecoration(
                          borderRadius: BorderRadius.only(
                              topLeft: Radius.circular(10),
                              topRight: Radius.circular(10)),
                          image: DecorationImage(
                              fit: BoxFit.cover,
                              image: NetworkImage(ServerPath.baseURL +
                                  '/' +
                                  ServerPath.uploadGambarVoucher +
                                  '/' +
                                  dataVoucher['foto'])))),
                ),
                Padding(
                  padding: EdgeInsets.symmetric(horizontal: 10, vertical: 15),
                  child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Berlaku hingga ' + dataVoucher['berlakuSampai'],
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.5)),
                        Text(
                            'Min. Belanja ' +
                                Currency.formatRupiah(
                                    int.parse(dataVoucher['minimumBelanja']),
                                    0),
                            style: TextStyle(
                                fontSize: SizeConfig.horizontalBlock * 3.5))
                      ]),
                )
              ],
            ),
          ),
          Positioned(
              top: containerHeight / 2,
              left: -15,
              child: Container(
                  height: 30,
                  width: 30,
                  decoration: BoxDecoration(
                      color: CustomColors.aliceBlue,
                      borderRadius: BorderRadius.circular(50)))),
          Positioned(
              top: containerHeight / 2,
              right: -15,
              child: Container(
                  height: 30,
                  width: 30,
                  decoration: BoxDecoration(
                      color: CustomColors.aliceBlue,
                      borderRadius: BorderRadius.circular(50))))
        ],
      ),
    );
  }
}
