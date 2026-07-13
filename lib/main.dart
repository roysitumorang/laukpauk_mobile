import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:laukpauk/models/MainModel.dart';
import 'package:provider/provider.dart';

import 'package:laukpauk/view-models/HeaderViewModel.dart';

import 'package:laukpauk/libraries/Routing.dart';
import 'package:shared_preferences/shared_preferences.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Firebase.initializeApp();
  SystemChrome.setSystemUIOverlayStyle(
      SystemUiOverlayStyle(statusBarColor: Colors.transparent));
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> with WidgetsBindingObserver {
  final GlobalKey<NavigatorState> _navigatorKey = GlobalKey<NavigatorState>();

  void initState() {
    super.initState();

    WidgetsBinding.instance.addObserver(this);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initialization();
    });
  }

  Future<void> _initialization() async {
    await MainModel.firebaseOnLink(_navigatorKey, context);
    await MainModel.initOneSignal(_navigatorKey, context);

    SharedPreferences _sP = await SharedPreferences.getInstance();
    _sP.setBool('showMakeSureADJD', true);
    _sP.setBool('showAlertGift', true);
  }

  void dispose() {
    super.dispose();
    WidgetsBinding.instance.removeObserver(this);
    _dispose();
  }

  Future<void> _dispose() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();
    _sP.remove('showMakeSureADJD');
    _sP.remove('showAlertGift');
  }

  Widget build(BuildContext context) {
    return ChangeNotifierProvider<HeaderViewModel>(
        create: (context) => HeaderViewModel(),
        builder: (BuildContext context, _) {
          return MaterialApp(
              debugShowCheckedModeBanner: false,
              title: 'Laukpauk',
              theme: ThemeData(platform: TargetPlatform.iOS),
              navigatorKey: _navigatorKey,
              onGenerateRoute: Routing.onGenerateRoute);
        });
  }
}
