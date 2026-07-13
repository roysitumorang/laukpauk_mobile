import 'package:flutter/material.dart';

import 'package:laukpauk/libraries/CustomColors.dart';

import 'package:laukpauk/models/HistoriPencarianModel.dart';

class HistoriPencarian extends StatefulWidget {
  _HistoriPencarianState createState() => _HistoriPencarianState();
}

class _HistoriPencarianState extends State<HistoriPencarian> {
  List _historiPencarianState = [];

  Future<void> _initialization() async {
    List _historiPencarian = await HistoriPencarianModel.getHistoriPencarian();
    setState(() {
      _historiPencarianState = _historiPencarian;
    });
  }

  void initState() {
    super.initState();
    _initialization();
  }

  Widget build(BuildContext context) {
    bool _hPSExist = _historiPencarianState.length >= 1;

    List<Widget> _historiPencarianChildren = (_hPSExist)
        ? _historiPencarianState
            .map((histori) => GestureDetector(
                  onTap: () async {
                    await HistoriPencarianModel.addHistoriPencarian(
                        histori['title']);
                  },
                  child: Padding(
                    padding:
                        EdgeInsets.only(bottom: 10.0, left: 5.0, right: 5.0),
                    child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(histori['title'],
                              style: TextStyle(color: CustomColors.grey)),
                          SizedBox(height: 3.0),
                          Text(histori['onTime'],
                              style: TextStyle(
                                  fontSize: 11.5,
                                  color: CustomColors.textMuted))
                        ]),
                  ),
                ))
            .toList()
        : [];

    return Container(
        padding: EdgeInsets.all(10.0),
        decoration: BoxDecoration(
            color: Color(0xFFF1F1F1),
            borderRadius: BorderRadius.circular(10.0)),
        child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(Icons.history, color: CustomColors.grey),
                  SizedBox(width: 10.0),
                  Text('Histori Pencarian',
                      style: TextStyle(color: CustomColors.nearlyBlack)),
                ],
              ),
              SizedBox(height: 10.0),
              (_hPSExist)
                  ? Column(
                      mainAxisSize: MainAxisSize.min,
                      children: _historiPencarianChildren)
                  : Text('Belum ada pencarian ..',
                      style:
                          TextStyle(color: CustomColors.grey, fontSize: 11.5))
            ]));
  }
}
