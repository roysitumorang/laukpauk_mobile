import 'package:flutter/material.dart';

import 'package:laukpauk/global-components/Pencarian.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

import 'package:laukpauk/views/pembeli/components/Promo/PromoItem.dart';
import 'package:laukpauk/models/PromoModel.dart';

class ProdukPromo extends StatefulWidget {
  _ProdukPromoState createState() => _ProdukPromoState();
}

class _ProdukPromoState extends State<ProdukPromo> {
  List produkPromoState = [];
  List produkPromoImmutableState = [];
  String pencarianString = '';

  Future<void> initialization() async {
    List produkPromo = await PromoModel.getProdukPromo();
    setState(() {
      produkPromoState = produkPromo;
      produkPromoImmutableState = produkPromo;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  void didUpdateWidget(oldWidget) {
    super.didUpdateWidget(oldWidget);
    initialization();
  }

  Widget build(BuildContext context) {
    List<Widget> produkPromoWidget = [];
    if (produkPromoState.length >= 1) {
      produkPromoWidget = produkPromoState.map((produk) {
        return IntrinsicHeight(
            child: PromoItem(produk,
                widgetWidth: SizeConfig.horizontalBlock * 25));
      }).toList();
    }

    return Expanded(
      child: Column(
        children: [
          Padding(
            padding: EdgeInsets.only(right: 10.0, left: 10.0, top: 10.0),
            child: Pencarian(
                onSearch: (String string) {
                  String stringLC = string.toLowerCase();

                  List produkPromoFiltered =
                      produkPromoImmutableState.where((produk) {
                    String namaProduk = produk['namaProduk'];
                    String namaProdukLC = namaProduk.toLowerCase();

                    return namaProdukLC.indexOf(stringLC) >= 0;
                  }).toList();

                  setState(() {
                    pencarianString = string;
                    produkPromoState = produkPromoFiltered;
                  });
                },
                onClose: () {
                  setState(() {
                    pencarianString = '';
                    produkPromoState = produkPromoImmutableState;
                  });
                },
                hint: 'Cari Produk Promo Di Sini',
                text: pencarianString),
          ),
          Expanded(
            child: LayoutBuilder(builder: (context, constraints) {
              return RefreshIndicator(
                onRefresh: () async {
                  await initialization();
                },
                child: (produkPromoState.length >= 1)
                    ? Container(
                        constraints:
                            BoxConstraints(minHeight: constraints.maxHeight),
                        child: Padding(
                          padding: EdgeInsets.only(
                              left: 10.0, right: 10.0, top: 10.0),
                          child: GridView.count(
                              shrinkWrap: true,
                              padding: EdgeInsets.zero,
                              crossAxisCount: 3,
                              childAspectRatio: 0.45,
                              mainAxisSpacing: 0,
                              crossAxisSpacing: 0,
                              children: produkPromoWidget),
                        ),
                      )
                    : Container(
                        constraints:
                            BoxConstraints(minHeight: constraints.maxHeight),
                        padding: EdgeInsets.symmetric(
                            horizontal: SizeConfig.horizontalBlock * 20.0),
                        child: Center(
                          child: ListView(shrinkWrap: true, children: [
                            Image.asset(
                                ClientPath.assetsImg + '/tidak-ada-promo.png')
                          ]),
                        )),
              );
            }),
          ),
        ],
      ),
    );
  }
}
