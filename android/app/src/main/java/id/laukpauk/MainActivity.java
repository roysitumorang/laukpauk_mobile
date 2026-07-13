package id.laukpauk;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;

import org.jetbrains.annotations.NotNull;

import com.google.android.play.core.appupdate.AppUpdateManager;

import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.TokenCallback;
import com.xendit.Xendit;

import com.google.gson.Gson;


//private final String devPublicKey =  "xnd_public_development_qjQ94sUBTX1EUO8Eu0GBeMaYoYkBEtRUG3IY9zIX894mtLdUkaBtazgoWTX5Y7";
//private final String prodPublicKey =  "xnd_public_production_wG8Fg0RNVSaCVYwOGWyZ2DdquAvAmQcqTCaErxJ7bXivEwekU2q7Xb8Hk0M3a";
//devPublicKey dan prodPublicKey diambil dari dashboard xendit akun id.laukpauk@gmail.com

public class MainActivity extends FlutterActivity{
    final String nativeCode = "native.code";
    final String xenditChannel = "laukpauk.xendit";
    final String CHANNEL = "https://lp2021.page.link/channel";
    final String EVENTS = "https://lp2021.page.link/events";
    String startString;
    String routeName;
    String routeParams;
    BroadcastReceiver linkReceiver;
    MyJobService myJobService;

    private AppUpdateManager _appUpdateManager;
    private static final int RC_APP_UPDATE = 100;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        myJobService = new MyJobService();

        Intent intent = getIntent();
        if(intent.getData() != null){
            routeName   = intent.getData().getPath();
            routeParams = intent.getData().getQuery();
        }

        if(intent.getDataString() != null){
            startString = intent.getDataString();
        }

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            JobInfo jobInfo = new JobInfo.Builder(11, new ComponentName(this, MyJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();
            jobScheduler.schedule(jobInfo);
        }

        Intent _myIntentService = new Intent(this, MyIntentService.class);
        _myIntentService.putExtra("foo", "Falentino Djoka");

        startService(_myIntentService);

        _appUpdateManager = AppUpdateManagerFactory.create(this);
        _appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                System.out.println("onSuccess update aplikasi");
                if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    // try {
                        (new MethodChannel(
                        (BinaryMessenger) getFlutterEngine().getDartExecutor().getBinaryMessenger(),
                        nativeCode)).invokeMethod("updateAvailable", null);
                    // } catch (IntentSender.SendIntentException e) {
                    //     e.printStackTrace();
                    // }
                }
            }
        });
        _appUpdateManager.registerListener(_installStateUpdateListener);
    }

    @Override
    protected void onStop() {
        if(_appUpdateManager != null){
            _appUpdateManager.unregisterListener(_installStateUpdateListener);
        }
        super.onStop();
    }

    private final InstallStateUpdatedListener _installStateUpdateListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(@NonNull InstallState state) {
            if(state.installStatus() == InstallStatus.DOWNLOADED){
                System.out.println("From mainactivity => Downloded !");
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_APP_UPDATE && resultCode != RESULT_OK){
            System.out.println("Tidak");
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if(intent.getAction() == "android.intent.action.VIEW"){
            if(linkReceiver != null){
                linkReceiver.onReceive(getApplicationContext(), intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(MyIntentService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(_myCustomBroadcast, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_myCustomBroadcast);
    }

    private BroadcastReceiver _myCustomBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int _resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            if(_resultCode == RESULT_OK){
                (new MethodChannel(
                        (BinaryMessenger) getFlutterEngine().getDartExecutor().getBinaryMessenger(),
                        nativeCode)).invokeMethod("jadwalDeliveryKadaluarsa", null);
            }
        }
    };



    public final BroadcastReceiver createChangeReceiver(@NotNull final EventChannel.EventSink events) {
        return (BroadcastReceiver)(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent != null){
                    Object dataString = intent.getDataString();
                    if(dataString == null){
                        events.error("UNAVAILABLE", "Link Unavailable", null);
                        dataString = Unit.INSTANCE;
                    }

                    events.success(dataString);
                }
            }
        });
   }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        (new MethodChannel((BinaryMessenger)flutterEngine.getDartExecutor(), CHANNEL))
                .setMethodCallHandler((MethodChannel.MethodCallHandler)(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                if(Intrinsics.areEqual(call.method, "initialLink")){
                    if(startString != null){
                        result.success(startString);
                    }
                }
            }
        }));

        (new EventChannel((BinaryMessenger)flutterEngine.getDartExecutor(), EVENTS))
                .setStreamHandler((EventChannel.StreamHandler)(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                linkReceiver = createChangeReceiver(events);
            }

            @Override
            public void onCancel(Object arguments) {
                linkReceiver = null;
            }
        }));

        (new MethodChannel((BinaryMessenger)flutterEngine.getDartExecutor(), xenditChannel))
                .setMethodCallHandler((MethodChannel.MethodCallHandler)(new MethodChannel.MethodCallHandler() {
            public final void onMethodCall(@NotNull MethodCall call, @NotNull MethodChannel.Result result) {

                if(Intrinsics.areEqual(call.method, "createCreditCartToken")){
                    Gson gson       =   new Gson();
                    Xendit xendit   =   new Xendit(getApplicationContext(), "xnd_public_production_wG8Fg0RNVSaCVYwOGWyZ2DdquAvAmQcqTCaErxJ7bXivEwekU2q7Xb8Hk0M3a", MainActivity.this);

                    Object argument = call.arguments;
                    String encodedArgument = argument.toString();
                    TokenizationArgument decodedArgument = gson.fromJson(encodedArgument, TokenizationArgument.class);

                    int _totalTagihan   =   decodedArgument.totalTagihan;
                    String _totalTagihanString  =   Integer.toString(_totalTagihan);

                    Card card = new Card(decodedArgument.cardNumber, decodedArgument.expiryMonth, decodedArgument.expiryYear, decodedArgument.cardCVN);                    
                    TokenCallback _tokenCallback =   new TokenCallback() {
                        public void onSuccess(Token token){
                            if(token != null){
                                String tokenID             =  token.getId();
                                String authenticationID    =  token.getAuthenticationId();
                                String cardCVN             =  decodedArgument.cardCVN;
                                String returnedData        =  "{\"tokenID\": \""+tokenID+"\", \"authenticationID\":\""+authenticationID+"\", \"cardCVN\":\""+cardCVN+"\"}";

                                result.success(returnedData);
                            }
                        }

                        @Override
                        public void onError(XenditError xenditError) {
                            System.out.println("Terjadi Kesalahan");
                            System.out.println(xenditError.getErrorMessage());
                            result.success(null);
                        }
                    };

                    xendit.createSingleUseToken(card, _totalTagihanString, true, "IDR", _tokenCallback);
                }
            }
        }));
    }
}