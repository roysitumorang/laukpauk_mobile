import 'dart:async';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:laukpauk/models/ADJDModel.dart';

import 'package:laukpauk/libraries/CustomColors.dart';
import 'package:laukpauk/libraries/SizeConfig.dart';

import 'package:laukpauk/views/pembeli/components/HomePage/CariGoogleMaps.dart';

class PinGMAPS extends StatefulWidget {
  final String currentLatitude;
  final String currentLongitude;
  final String appBarText;
  final String formattedAddress;

  PinGMAPS(
      {this.formattedAddress = '',
      this.currentLatitude = '3.5889242',
      this.currentLongitude = '98.6790958',
      this.appBarText = 'Pilih Alamat Pengantaran'});
  //default ke merdeka walk medan
  _PinGMAPSState createState() => _PinGMAPSState();
}

class _PinGMAPSState extends State<PinGMAPS> {
  Map<String, String>? selectedPosition;

  Completer<GoogleMapController> _controllerState = Completer();

  CameraPosition _initialPosition =
      CameraPosition(target: LatLng(3.5889242, 98.6790958), zoom: 15.0);

  Set<Marker> _markers = {
    Marker(
        markerId: MarkerId('defaultMarker'),
        position: LatLng(3.5889242, 98.6790958))
  };

  Future<void> addInitialMarker() async {
    await Future.delayed(Duration(milliseconds: 1));

    double currentLatitude = double.parse(widget.currentLatitude);
    double currentLongitude = double.parse(widget.currentLongitude);
    String formattedAddress = await ADJDModel.reverseGeocoding(
        latitude: currentLatitude, longitude: currentLongitude);
    pindahLokasi(formattedAddress, currentLatitude, currentLongitude);
  }

  Future<void> pindahLokasi(
      String formattedAddress, double latitude, double longitude,
      {double zoom = 14}) async {
    String stringLat = latitude.toString();
    String stringLng = longitude.toString();

    GoogleMapController _controller = await _controllerState.future;

    LatLng newLatLong = LatLng(latitude, longitude);
    CameraPosition _newCameraPosition =
        CameraPosition(target: newLatLong, zoom: 16.0);

    _controller
        .animateCamera(CameraUpdate.newCameraPosition(_newCameraPosition));

    setState(() {
      _markers = {
        Marker(
            markerId: MarkerId(stringLat + ', ' + stringLng),
            position: LatLng(latitude, longitude),
            icon: BitmapDescriptor.defaultMarker)
      };
      selectedPosition = {
        'formattedAddress': formattedAddress,
        'lat': stringLat,
        'lng': stringLng
      };
    });
  }

  void initState() {
    super.initState();
    addInitialMarker();
  }

  Widget build(BuildContext context) {
    SizeConfig().initSize(context);

    return Scaffold(
        appBar: AppBar(
            title: Text(widget.appBarText),
            backgroundColor: CustomColors.green),
        body: Stack(children: [
          GoogleMap(
              initialCameraPosition: _initialPosition,
              markers: _markers,
              onMapCreated: (GoogleMapController controller) {
                _controllerState.complete(controller);
              },
              onLongPress: (newLatLong) async {
                String formattedAddress = await ADJDModel.reverseGeocoding(
                    latitude: newLatLong.latitude,
                    longitude: newLatLong.longitude);
                pindahLokasi(formattedAddress, newLatLong.latitude,
                    newLatLong.longitude);
              }),
          Positioned(
              top: SizeConfig.verticalBlock * 1.5,
              child: CariGoogleMaps(
                onSuggestionSelected:
                    (Map<String, dynamic> selectedSuggestion) {
                  Map<String, dynamic> location =
                      selectedSuggestion['location'];
                  double latitude = location['lat'];
                  double longitude = location['lng'];

                  String formattedAddress =
                      selectedSuggestion['formattedAddress'];
                  pindahLokasi(formattedAddress, latitude, longitude,
                      zoom: 16.5);
                },
              )),
          (selectedPosition != null)
              ? Positioned(
                  bottom: SizeConfig.verticalBlock * 3.5,
                  child: Container(
                    width: SizeConfig.screenWidth,
                    child: Center(
                      child: Material(
                        elevation: 8,
                        color: CustomColors.green,
                        borderRadius: BorderRadius.circular(25),
                        child: InkWell(
                            borderRadius: BorderRadius.circular(25),
                            splashColor: CustomColors.nearlyWhite,
                            onTap: () {
                              Navigator.pop(context, selectedPosition);
                            },
                            child: Container(
                                child: Padding(
                                  padding: EdgeInsets.only(
                                      top: 10, left: 25, bottom: 10, right: 25),
                                  child: Text('Tetapkan Lokasi',
                                      style: TextStyle(
                                          color: Colors.white,
                                          fontSize:
                                              SizeConfig.horizontalBlock * 3.5),
                                      textAlign: TextAlign.center),
                                ),
                                decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(25),
                                ))),
                      ),
                    ),
                  ))
              : SizedBox()
        ]));
  }
}

/*
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class PinGMAPS extends StatefulWidget {
  final String currentLatitude;
  final String currentLongitude;
  final String appBarText;
  final String formattedAddress;

  PinGMAPS(
      {this.formattedAddress = '',
      this.currentLatitude = '3.5889242',
      this.currentLongitude = '98.6790958',
      this.appBarText = 'Pilih Alamat Pengantaran'});
  //default ke merdeka walk medan
  PinGMAPSState createState() => PinGMAPSState();
}

class PinGMAPSState extends State<PinGMAPS> {
  Completer<GoogleMapController> _controller = Completer();

  static final CameraPosition _kGooglePlex = CameraPosition(
    target: LatLng(37.42796133580664, -122.085749655962),
    zoom: 14.4746,
  );

  static final CameraPosition _kLake = CameraPosition(
      bearing: 192.8334901395799,
      target: LatLng(37.43296265331129, -122.08832357078792),
      tilt: 59.440717697143555,
      zoom: 19.151926040649414);

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      body: GoogleMap(
        mapType: MapType.hybrid,
        initialCameraPosition: _kGooglePlex,
        onMapCreated: (GoogleMapController controller) {
          _controller.complete(controller);
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _goToTheLake,
        label: Text('To the lake!'),
        icon: Icon(Icons.directions_boat),
      ),
    );
  }

  Future<void> _goToTheLake() async {
    final GoogleMapController controller = await _controller.future;
    controller.animateCamera(CameraUpdate.newCameraPosition(_kLake));
  }
}

*/
