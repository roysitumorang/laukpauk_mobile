import 'dart:convert';

import 'package:flutter/material.dart';

import 'package:laukpauk/views/SplashScreen.dart';
import 'package:laukpauk/views/pembeli/HistoriPoin.dart';
import 'package:laukpauk/views/pembeli/ResponseRPD.dart';
import 'package:laukpauk/views/pembeli/DetailTransaksi.dart';
import 'package:laukpauk/views/pembeli/HistoriDompet.dart';
import 'package:laukpauk/views/penjual/TokoSaya.dart';
import 'package:laukpauk/views/pembeli/Voucher.dart';
import 'package:laukpauk/views/penjual/ProsesPesanan.dart';
import 'package:laukpauk/views/pembeli/DetailPaket.dart';
import 'package:laukpauk/views/pembeli/DetailProduk.dart';
import 'package:laukpauk/views/pembeli/DetailPromoItem.dart';
import 'package:laukpauk/views/pembeli/DetailResep.dart';
import 'package:laukpauk/views/pembeli/DetailVoucher.dart';
import 'package:laukpauk/views/pembeli/BuatOrderanBerhasil.dart';

class Routing {
  static Route onGenerateRoute(RouteSettings routeSettings) {
    Widget _widgetToRoute = routeWidget(routeSettings);
    return MaterialPageRoute(builder: (_) => _widgetToRoute);
  }

  static Widget routeWidget(RouteSettings routeSettings) {
    switch (routeSettings.name) {
      case '/':
        return SplashScreen();

      case '/dompet':
        String encodedArgument = routeSettings.arguments as String;
        Map<String, dynamic> decodedArgument = jsonDecode(encodedArgument);
        String jenisDompet = decodedArgument['jenisDompet'];
        return HistoriDompet(jenisDompet: jenisDompet);

      case '/orderan':
        String encodedArgument = routeSettings.arguments as String;
        Map<String, dynamic> decodedArgument = jsonDecode(encodedArgument);
        String idTransaksi = decodedArgument['idTransaksi'].toString();
        return DetailTransaksi(idTransaksi);

      case '/voucher':
        String? stringArguments = routeSettings.arguments as String?;
        Widget widget = Voucher();
        if (stringArguments != null) {
          if (stringArguments.length >= 1) {
            Map<String, dynamic> arguments = jsonDecode(stringArguments);
            String idVoucher = arguments['voucher'].toString();
            widget = DetailVoucher(idVoucher);
          }
        }

        return widget;

      case '/produk':
        Map<String, dynamic>? arguments =
            routeSettings.arguments as Map<String, dynamic>?;
        Widget widget = SizedBox();
        if (arguments != null) {
          String idProduk = arguments['produk'];
          widget = DetailProduk(idProduk);
        }

        return widget;

      case '/paket':
        Map<String, dynamic>? arguments =
            routeSettings.arguments as Map<String, dynamic>?;
        Widget widget = SizedBox();
        if (arguments != null) {
          String idPaket = arguments['paket'];
          widget = DetailPaket(idPaket);
        }

        return widget;

      case '/rpd':
        String encodedArgument = routeSettings.arguments as String;
        Map<String, dynamic> decodedArguments = jsonDecode(encodedArgument);
        Map<String, dynamic> dataPenjual = decodedArguments['dataPenjual'];
        Map<String, dynamic> dataRPD = decodedArguments['dataRPD'];
        return ResponseRPD(dataPenjual, dataRPD);

      case '/rmp':
        return TokoSaya();

      case '/poin':
        return HistoriPoin();

      case '/promo':
        Map<String, dynamic> arguments =
            routeSettings.arguments as Map<String, dynamic>;
        String? idPromoItem = arguments['idPromoItem']; //dari push notfi
        if (idPromoItem == null) {
          idPromoItem = arguments['promo']; //dari banner konten
        }

        return DetailPromoItem(idPromoItem!);

      case '/resep':
        Map<String, dynamic> arguments =
            routeSettings.arguments as Map<String, dynamic>;
        String idResep = arguments['resep'];

        return DetailResep(idResep);

      case '/penjualan':
        String stringArguments = routeSettings.arguments as String;
        Map<String, dynamic> arguments = jsonDecode(stringArguments);
        String idTransaksi = arguments['idTransaksi'].toString();
        return ProsesPesanan(idTransaksi);

      case '/buatOrderBerhasil':
        Map<String, dynamic> arguments =
            routeSettings.arguments as Map<String, dynamic>;
        String idTransaksi = arguments['idTransaksi'].toString();

        Map<String, dynamic> dataFromCheckout = {
          'responseCheckout': {'idTransaksi': idTransaksi}
        };

        return BuatOrderanBerhasil(
            returnedDataFromCheckout: dataFromCheckout,
            removeTransactionSession: true);

      default:
        return SplashScreen();
    }
  }
}
