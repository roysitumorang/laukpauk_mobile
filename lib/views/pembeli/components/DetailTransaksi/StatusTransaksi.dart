import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:laukpauk/libraries/CustomColors.dart';

class StatusTransaksi extends StatelessWidget {
  final String statusTransaksi;
  final double iconWidth, iconSize, iconLabelSize;
  final bool showIconLabel;
  StatusTransaksi(this.statusTransaksi,
      {this.iconWidth = 45.0,
      this.iconSize = 15,
      this.showIconLabel = false,
      this.iconLabelSize = 10});

  final List<IconData> deliveryIcons = [
    FontAwesomeIcons.checkCircle,
    FontAwesomeIcons.boxOpen,
    FontAwesomeIcons.shippingFast,
    FontAwesomeIcons.peopleCarry
  ];

  final List<String> iconLabels = ['Diterima', 'Proses', 'Kirim', 'Selesai'];

  final GlobalKey currentWidget = GlobalKey();

  Widget build(BuildContext statusTransaksiContext) {
    double totalIconWidth = iconWidth * 4;

    double totalJarakSeluruhIcon = totalIconWidth;
    double jarakPerIcon = (totalJarakSeluruhIcon / 3) - iconWidth;

    int statusDeliveryInt = 0;
    String statusTransaksiLS = statusTransaksi.toLowerCase();
    if (statusTransaksiLS == 'approved') {
      statusDeliveryInt = 1;
    }
    if (statusTransaksiLS == 'processing') {
      statusDeliveryInt = 2;
    }
    if (statusTransaksiLS == 'otw') {
      statusDeliveryInt = 3;
    }
    if (statusTransaksiLS == 'received') {
      statusDeliveryInt = 4;
    }

    int currentIndex = 1;
    List<Widget> statusTransaksiWidget = deliveryIcons.map((icon) {
      Color widgetColor = (currentIndex <= statusDeliveryInt)
          ? CustomColors.green
          : CustomColors.textMuted;
      String iconLabel = iconLabels[currentIndex - 1];

      currentIndex++;

      return Column(children: [
        Row(children: [
          Container(
              width: iconWidth,
              height: iconWidth,
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(50), color: widgetColor),
              child: Icon(
                icon,
                size: iconSize,
                color: CustomColors.nearlyWhite,
              )),
          (currentIndex <= deliveryIcons.length)
              ? Container(
                  height: 3,
                  color: (currentIndex <= statusDeliveryInt)
                      ? CustomColors.green
                      : CustomColors.textMuted,
                  width: jarakPerIcon)
              : SizedBox()
        ]),
        (showIconLabel)
            ? Padding(
                padding: EdgeInsets.only(top: 2.5),
                child: Text(iconLabel,
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: iconLabelSize)),
              )
            : SizedBox()
      ]);
    }).toList();

    return Container(
        child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: statusTransaksiWidget));
  }
}
