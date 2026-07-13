import 'package:laukpauk/models/UserModel.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SplashScreenModel {
  static Future<bool> checkHasAppStarted() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    await Future.delayed(Duration(seconds: 1));
    bool isAppStarted = false;

    bool? _isAppStartedSavedStorage = _sP.getBool('isAppStarted') ?? null;
    if (_isAppStartedSavedStorage != null) {
      if (_isAppStartedSavedStorage) {
        isAppStarted = _isAppStartedSavedStorage;
      }
    }

    return isAppStarted;
  }

  static Future<bool> checkHasUserLoggedIn() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    bool hasUserLoggedIn = false;
    if (_sP.getString('authToken') != null) {
      hasUserLoggedIn = true;
    }

    return hasUserLoggedIn;
  }

  static Future<bool> checkHasOTPSent() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    bool otpHasSent = _sP.getBool('otpHasSent') ?? false;

    return otpHasSent;
  }

  static Future<bool> checkHasOTPConfirmed() async {
    SharedPreferences _sP = await SharedPreferences.getInstance();

    bool otpHasConfirmed = _sP.getBool('otpHasConfirmed') ?? false;

    return otpHasConfirmed;
  }

  static Future<bool> hasOTPConfirmed() async {
    dynamic _detailUser = await UserModel.getDetailActiveUser();
    if (_detailUser != false) {
      String _isOTPConfirmed = _detailUser['statusOTP'];
      return (_isOTPConfirmed == '1') ? true : false;
    }

    return false;
  }
}
