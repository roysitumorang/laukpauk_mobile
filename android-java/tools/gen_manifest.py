#!/usr/bin/env python3
"""Generates android-java/app/src/main/AndroidManifest.xml by scanning the Java sources for Activities
(classes that extend BaseActivity or AppCompatActivity directly). Re-run whenever screens are added."""
import os, re, sys

SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "java")
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "app", "src", "main", "AndroidManifest.xml")
LAUNCHER = "id.laukpauk.view.SplashScreenActivity"

acts = []
for root, _, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        text = open(os.path.join(root, fn), encoding="utf-8").read()
        pkg = re.search(r"^package\s+([\w.]+);", text, re.M)
        if not pkg:
            continue
        for m in re.finditer(r"^\s*public\s+(?:abstract\s+)?class\s+(\w+)\s+extends\s+(BaseActivity|AppCompatActivity)\b", text, re.M):
            if "abstract" in m.group(0):
                continue
            acts.append(pkg.group(1) + "." + m.group(1))
acts = sorted(set(acts))
if LAUNCHER not in acts:
    print("WARNING: launcher %s not found among activities" % LAUNCHER, file=sys.stderr)

COMMON = ('android:screenOrientation="portrait" '
          'android:windowSoftInputMode="adjustResize" '
          'android:configChanges="orientation|keyboardHidden|keyboard|screenSize|smallestScreenSize|screenLayout|density|uiMode|fontScale|layoutDirection|locale"')

lines = []
lines.append('<?xml version="1.0" encoding="utf-8"?>')
lines.append('<manifest xmlns:android="http://schemas.android.com/apk/res/android"')
lines.append('    xmlns:tools="http://schemas.android.com/tools">')
lines.append('')
lines.append('    <uses-permission android:name="android.permission.INTERNET" />')
lines.append('    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />')
lines.append('    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />')
lines.append('    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />')
lines.append('    <uses-permission android:name="android.permission.CAMERA" />')
lines.append('    <!-- Android 13+: needed to show OneSignal push notifications -->')
lines.append('    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />')
lines.append('')
lines.append('    <uses-feature android:name="android.hardware.camera" android:required="false" />')
lines.append('    <uses-feature android:name="android.hardware.location.gps" android:required="false" />')
lines.append('')
lines.append('    <queries>')
lines.append('        <intent>')
lines.append('            <action android:name="android.intent.action.VIEW" />')
lines.append('            <category android:name="android.intent.category.BROWSABLE" />')
lines.append('            <data android:scheme="https" />')
lines.append('        </intent>')
lines.append('        <intent>')
lines.append('            <action android:name="android.intent.action.DIAL" />')
lines.append('            <data android:scheme="tel" />')
lines.append('        </intent>')
lines.append('        <intent>')
lines.append('            <action android:name="android.intent.action.SEND" />')
lines.append('            <data android:mimeType="*/*" />')
lines.append('        </intent>')
lines.append('        <intent>')
lines.append('            <action android:name="android.media.action.IMAGE_CAPTURE" />')
lines.append('        </intent>')
lines.append('    </queries>')
lines.append('')
lines.append('    <application')
lines.append('        android:name=".LaukpaukApp"')
lines.append('        android:label="@string/app_name"')
lines.append('        android:icon="@mipmap/ic_launcher"')
lines.append('        android:theme="@style/AppTheme"')
lines.append('        android:allowBackup="false"')
lines.append('        android:supportsRtl="false"')
lines.append('        android:enableOnBackInvokedCallback="true"')
lines.append('        tools:replace="android:allowBackup,android:supportsRtl"')
lines.append('        tools:targetApi="36">')
lines.append('')
lines.append('        <!-- Android 16 (targetSdk 36): predictive back is on and onBackPressed() is never called; screens use')
lines.append('             BaseActivity.setOnBack(). Edge-to-edge cannot be opted out of; BaseActivity applies the insets.')
lines.append('             On screens >= 600dp Android 16 ignores the portrait lock below, so layouts must also work in landscape. -->')
lines.append('')
lines.append('        <!-- Google Maps SDK key: MAPS_API_KEY in laukpauk.properties (never commit the real key) -->')
lines.append('        <meta-data android:name="com.google.android.geo.API_KEY" android:value="${MAPS_API_KEY}" />')
lines.append('')
for a in acts:
    if a == LAUNCHER:
        lines.append('        <activity')
        lines.append('            android:name="%s"' % a)
        lines.append('            android:exported="true"')
        lines.append('            android:launchMode="singleTop"')
        lines.append('            android:theme="@style/LaunchTheme"')
        lines.append('            %s>' % COMMON)
        lines.append('            <intent-filter>')
        lines.append('                <action android:name="android.intent.action.MAIN" />')
        lines.append('                <category android:name="android.intent.category.LAUNCHER" />')
        lines.append('            </intent-filter>')
        lines.append('            <!-- Same hosts as the Flutter app. NOTE: *.page.link Firebase Dynamic Links were shut down by Google;')
        lines.append('                 lp.me links keep working as plain App Links. -->')
        lines.append('            <intent-filter android:label="Laukpauk" android:autoVerify="true">')
        lines.append('                <action android:name="android.intent.action.VIEW" />')
        lines.append('                <category android:name="android.intent.category.DEFAULT" />')
        lines.append('                <category android:name="android.intent.category.BROWSABLE" />')
        lines.append('                <data android:scheme="https" android:host="lp.me" />')
        lines.append('                <data android:scheme="https" android:host="lp2021.page.link" />')
        lines.append('            </intent-filter>')
        lines.append('        </activity>')
    else:
        lines.append('        <activity android:name="%s" android:exported="false"' % a)
        lines.append('            %s />' % COMMON)
lines.append('')
lines.append('        <provider')
lines.append('            android:name="androidx.core.content.FileProvider"')
lines.append('            android:authorities="${applicationId}.fileprovider"')
lines.append('            android:exported="false"')
lines.append('            android:grantUriPermissions="true">')
lines.append('            <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />')
lines.append('        </provider>')
lines.append('')
lines.append('        <!-- Xendit card tokenization / 3-D Secure (same as the Flutter manifest) -->')
lines.append('        <activity android:name="com.xendit.XenditActivity" android:exported="true" />')
lines.append('    </application>')
lines.append('</manifest>')
open(OUT, "w", encoding="utf-8").write("\n".join(lines) + "\n")
print("manifest written: %d activities -> %s" % (len(acts), OUT))
