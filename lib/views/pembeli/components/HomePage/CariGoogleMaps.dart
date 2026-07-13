import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:http/http.dart';
import 'package:flutter_typeahead/flutter_typeahead.dart';
import 'package:laukpauk/libraries/ClientPath.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/LocalStorage.dart';

import 'package:laukpauk/libraries/SizeConfig.dart';

class CariGoogleMaps extends StatefulWidget {
  final void Function(Map<String, dynamic> suggestion) onSuggestionSelected;

  CariGoogleMaps({required this.onSuggestionSelected});
  _CariGoogleMapsState createState() => _CariGoogleMapsState();
}

class _CariGoogleMapsState extends State<CariGoogleMaps> {
  String apiKeyState = '';

  Future<List> getWilayahFromGMaps({required String string}) async {
    if (string.length >= 1) {
      Uri _url = Uri.parse(
          'https://maps.googleapis.com/maps/api/place/textsearch/json?query=$string&key=$apiKeyState');
      Response httpResponse = await get(_url);

      if (httpResponse.statusCode == 200) {
        Map dataObject = jsonDecode(httpResponse.body);

        List wilayahFromGMaps = dataObject['results'];
        return wilayahFromGMaps;
      }
    }

    return [];
  }

  void initialization() async {
    String apiKey = await LocalStorage.getGoogleMapsApiKey();
    setState(() {
      apiKeyState = apiKey;
    });
  }

  void initState() {
    super.initState();
    initialization();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Container(
      padding: EdgeInsets.only(right: 10, left: 10),
      width: SizeConfig.horizontalBlock * 100,
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(50)),
      child: Material(
        elevation: 5,
        borderRadius: BorderRadius.circular(50),
        child: TypeAheadField(
            keepSuggestionsOnLoading: true,
            noItemsFoundBuilder: (context) {
              return Container(
                width: SizeConfig.horizontalBlock * 100,
                padding: EdgeInsets.symmetric(vertical: 25),
                child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Image.asset(ClientPath.assetsImg + '/empty.png',
                          width: SizeConfig.horizontalBlock * 30),
                      Text('Lokasi yang kamu cari tidak ada',
                          style: TextStyle(
                              fontSize: SizeConfig.horizontalBlock * 3.5))
                    ]),
              );
            },
            textFieldConfiguration: TextFieldConfiguration(
                decoration: InputDecoration(
                    hintStyle:
                        TextStyle(fontSize: SizeConfig.horizontalBlock * 3.25),
                    hintText: 'Cari Alamat di Sini',
                    prefix: SizedBox(width: 25),
                    border: InputBorder.none)),
            suggestionsCallback: (pattern) async {
              List suggestionData = await getWilayahFromGMaps(string: pattern);
              return suggestionData;
            },
            itemBuilder: (context, suggestion) {
              if (suggestion != null) {
                Map<String, dynamic> _suggestion =
                    suggestion as Map<String, dynamic>;

                return Padding(
                  padding: EdgeInsets.only(top: 5),
                  child: ListTile(
                    title: Text(_suggestion['name'],
                        style: TextStyle(
                            color: CustomColors.green,
                            fontWeight: FontWeight.w700,
                            fontSize: 15)),
                    subtitle: Text(_suggestion['formatted_address'],
                        style: TextStyle(fontSize: 12)),
                  ),
                );
              } else {
                return SizedBox();
              }
            },
            onSuggestionSelected: (detailSuggestion) {
              if (detailSuggestion != null) {
                Map<String, dynamic> _dS =
                    detailSuggestion as Map<String, dynamic>;
                Map<String, dynamic> geometry = _dS['geometry'];
                Map<String, dynamic> latLong = geometry['location'];
                String formattedAddress = _dS['formatted_address'];

                Map<String, dynamic> selectedSuggestion = {
                  'location': latLong,
                  'formattedAddress': formattedAddress
                };

                widget.onSuggestionSelected(selectedSuggestion);
              }
            }),
      ),
    );
  }
}
