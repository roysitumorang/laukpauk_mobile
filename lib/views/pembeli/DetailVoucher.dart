import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';
import 'package:laukpauk/libraries/ServerPath.dart';
import 'package:laukpauk/libraries/ClientPath.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/Currency.dart';

import 'package:laukpauk/global-components/CustomButton.dart';

import 'package:laukpauk/models/VoucherModel.dart';
import 'package:shared_preferences/shared_preferences.dart';

class DetailVoucher extends StatefulWidget {
  final String idVoucher;
  DetailVoucher(this.idVoucher);

  _DetailVoucherState createState() => _DetailVoucherState();
}

class _DetailVoucherState extends State<DetailVoucher> {
  Map<String, dynamic>? usedVoucherState;

  void initialization() async {
    String? usedVoucherString = await LocalStorage.getUsedVoucher();
    Map<String, dynamic> usedVoucher = (usedVoucherString != null)
        ? await VoucherModel.getVoucherDetail(usedVoucherString)
        : null;

    setState(() {
      usedVoucherState = usedVoucher;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  void toggleVoucher(
      BuildContext context, Map<String, dynamic> dataVoucher) async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    String idVoucher = widget.idVoucher;
    bool isThisVoucherUsed = false;
    if (usedVoucherState != null) {
      isThisVoucherUsed = idVoucher == usedVoucherState!['id'];
    }
    String namaVoucher = dataVoucher['nama'];
    String message;

    if (isThisVoucherUsed) {
      _sP.remove('usedVoucher');
      message = 'Penggunaan voucher $namaVoucher dibatalkan!';
    } else {
      _sP.setString('usedVoucher', idVoucher);
      message = 'Voucher $namaVoucher digunakan!';
    }

    initialization();

    SnackBar usedVoucherSnackbar = SnackBar(content: Text(message));
    // Scaffold.of(context).showSnackBar(usedVoucherSnackbar);

    ScaffoldMessenger.of(context).showSnackBar(usedVoucherSnackbar);
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    String idVoucher = widget.idVoucher;

    return Scaffold(
        appBar: AppBar(
          title: Text('Detail Voucher'),
          backgroundColor: CustomColors.green,
        ),
        backgroundColor: CustomColors.nearlyWhite,
        body: FutureBuilder(
            future: VoucherModel.getVoucherDetail(idVoucher),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                dynamic detailVoucher = snapshot.data;
                if (detailVoucher != false) {
                  String idVoucher = widget.idVoucher;

                  int usageCount = detailVoucher['usageCount'];
                  int maximumUsage = int.parse(detailVoucher['maximumUsage']);
                  int sisaPenggunaan = maximumUsage - usageCount;

                  String penjelasanVoucher =
                      (detailVoucher['jenis'].toLowerCase() == 'ongkir')
                          ? 'Voucher untuk mengurangi ongkos kirim'
                          : 'Voucher untuk mengurangi total belanja';

                  bool isThisVoucherUsed = false;
                  if (usedVoucherState != null) {
                    isThisVoucherUsed = idVoucher == usedVoucherState!['id'];
                  }

                  return RefreshIndicator(
                    onRefresh: () async {
                      initialization();
                    },
                    child: ListView(padding: EdgeInsets.zero, children: [
                      Container(
                          height: SizeConfig.horizontalBlock * 60,
                          decoration: BoxDecoration(
                              image: DecorationImage(
                                  fit: BoxFit.fill,
                                  image: NetworkImage(ServerPath.baseURL +
                                      '/' +
                                      ServerPath.uploadGambarVoucher +
                                      '/' +
                                      detailVoucher['foto'])))),
                      Container(
                        padding:
                            EdgeInsets.symmetric(horizontal: 15, vertical: 15),
                        width: MediaQuery.of(context).size.width,
                        child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                  mainAxisAlignment:
                                      MainAxisAlignment.spaceBetween,
                                  children: [
                                    Expanded(
                                      child: Column(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Text(detailVoucher['nama'],
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        5.0,
                                                    fontWeight: FontWeight.w700,
                                                    color: CustomColors.green)),
                                            SizedBox(height: 5),
                                            Text(detailVoucher['jenis']
                                                .toUpperCase()),
                                            Text(penjelasanVoucher,
                                                style: TextStyle(
                                                    fontSize: SizeConfig
                                                            .horizontalBlock *
                                                        3.25)),
                                          ]),
                                    ),
                                    SizedBox(width: 10.0),
                                    Text(
                                        Currency.formatRupiah(
                                            int.parse(detailVoucher['nilai']),
                                            0),
                                        style: TextStyle(
                                            fontSize:
                                                SizeConfig.horizontalBlock *
                                                    4.5,
                                            fontWeight: FontWeight.w700,
                                            color: CustomColors.green))
                                  ]),
                              Container(
                                  height: 0.5,
                                  margin: EdgeInsets.symmetric(vertical: 15.0),
                                  decoration: BoxDecoration(
                                      color: CustomColors.nearlyBlack)),
                              Text('Kode Voucher',
                                  style: TextStyle(
                                      fontWeight: FontWeight.w700,
                                      fontSize:
                                          SizeConfig.horizontalBlock * 4)),
                              SizedBox(height: 3),
                              Text(detailVoucher['kode'].toUpperCase(),
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10),
                              Text('Minimum Belanja',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  Currency.formatRupiah(
                                      int.parse(
                                          detailVoucher['minimumBelanja']),
                                      0),
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10),
                              Text('Maksimum Penggunaan',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  Currency.formatSatuan(maximumUsage,
                                      satuan: 'Kali Penggunaan'),
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10),
                              Text('Jumlah Penggunaan',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  (usageCount >= 1)
                                      ? Currency.formatSatuan(usageCount,
                                          satuan: 'Kali Penggunaan')
                                      : 'Belum ada penggunaan',
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10),
                              Text('Masa Berlaku',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3),
                              Text(
                                  detailVoucher['berlakuMulai'] +
                                      ' s/d ' +
                                      detailVoucher['berlakuSampai'],
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 10.0),
                              Text('Deskripsi Voucher',
                                  style: TextStyle(
                                      fontSize: SizeConfig.horizontalBlock * 4,
                                      fontWeight: FontWeight.w700)),
                              SizedBox(height: 3.0),
                              Text(detailVoucher['deskripsi'],
                                  textAlign: TextAlign.left,
                                  style: TextStyle(
                                      fontSize:
                                          SizeConfig.horizontalBlock * 3.25)),
                              SizedBox(height: 15.0),
                              (sisaPenggunaan >= 1)
                                  ? CustomButton.button(
                                      context,
                                      (isThisVoucherUsed)
                                          ? 'Batal Gunakan Voucher'
                                          : 'Gunakan Voucher', () {
                                      toggleVoucher(context, detailVoucher);
                                    },
                                      color: (isThisVoucherUsed)
                                          ? CustomButton.dangerColor
                                          : CustomButton.themeColor,
                                      isBlock: true)
                                  : Text(
                                      'Voucher tidak dapat digunakan karena sudah mencapai batas maksimum penggunaan',
                                      textAlign: TextAlign.center,
                                      style: TextStyle(
                                          color: CustomColors.textDanger,
                                          fontStyle: FontStyle.italic)),
                              SizedBox(height: 5)
                            ]),
                      )
                    ]),
                  );
                } else {
                  return LayoutBuilder(builder: (context, constraints) {
                    return RefreshIndicator(
                      onRefresh: () async {
                        setState(() {});
                      },
                      child: ListView(padding: EdgeInsets.zero, children: [
                        Container(
                          constraints:
                              BoxConstraints(minHeight: constraints.maxHeight),
                          child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Image.asset(
                                  ClientPath.assetsImg + '/empty.png',
                                  width: SizeConfig.horizontalBlock * 35,
                                ),
                                SizedBox(height: 20),
                                Text('Detail Voucher Tidak Ditemukan',
                                    style: TextStyle(
                                        color: CustomColors.textMuted,
                                        fontSize:
                                            SizeConfig.horizontalBlock * 3.5,
                                        fontWeight: FontWeight.w600))
                              ]),
                        )
                      ]),
                    );
                  });
                }
              } else {
                return Center(
                    child: SizedBox(
                        width: 25,
                        height: 25,
                        child: CircularProgressIndicator(strokeWidth: 1.5)));
              }
            }));
  }
}
