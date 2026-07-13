package id.laukpauk;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

public class MyIntentService extends IntentService {
    public MyIntentService(){
        super("my-intent-service");
    }
    public static final String ACTION =   "id.laukpauk.MyIntentService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Gson gson = new Gson();


        Intent in = new Intent(ACTION);
        in.putExtra("resultCode", Activity.RESULT_OK);

        System.out.println("onHandleIntent is executed");

        LocalBroadcastManager _localBroadcast = LocalBroadcastManager.getInstance(this);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("FlutterSharedPreferences", MODE_PRIVATE);
                String savedJadwalDeliveryMap = sharedPreferences.getString("flutter.savedJadwalDeliveryMap", null);

                if(savedJadwalDeliveryMap != null){
                    SavedJadwalDeliveryMap _savedADJD = gson.fromJson(savedJadwalDeliveryMap, SavedJadwalDeliveryMap.class);

                    String _id              =   _savedADJD.id;
                    String _dayDelivery     =   _savedADJD.day;
                    String _dayDeliveryLC   =   _dayDelivery.toLowerCase();

                    LocalDateTime _now = LocalDateTime.now();
                    LocalDate _nowDate = _now.toLocalDate();
                    LocalTime _nowTime = _now.toLocalTime();

                    LocalTime _savedADJDMaxTime = LocalTime.parse(_savedADJD.maxTime);
                    LocalDate _savedADJDMaxDay  = LocalDate.parse(_savedADJD.currentDate);

                    if(_dayDeliveryLC.equals("besok")){
                        _savedADJDMaxDay    =   _savedADJDMaxDay.plusDays(1); //1 hari kemudian
                    }
                    if(_dayDeliveryLC.equals("lusa")){
                        _savedADJDMaxDay    =   _savedADJDMaxDay.plusDays(2); //2 hari kemudian
                    }

                    if(_id.equals("1")){
                        if(_dayDeliveryLC.equals("besok") || _dayDeliveryLC.equals("lusa")){
                            /*
                                Khusus untuk pengantaran yang id-nya 1, yaitu pengantaran sessi pagi,
                                terjadi bugs, yaitu pengantaran dilakukan terlebih dahulu lalu proses
                                pembatalan transactionSession dibatalkan, seharusnya yg benar itu pembatalan
                                transaction session dulu dijalankan sebelum terjadi pengantaran.

                                Oleh karena itu, tanggal kadaluarsanya adalah _savedADJDMaxDay - 1 hari.
                            */
                            _savedADJDMaxDay    =   _savedADJDMaxDay.minusDays(1);
                        }
                    }

                    if(_nowDate.isEqual(_savedADJDMaxDay)){
                        if(_nowTime.isAfter(_savedADJDMaxTime)){
                            _localBroadcast.sendBroadcast(in);
                        }
                    }

//                _localBroadcast.sendBroadcast(in);
                }
            }
        }, 0, 5000);


    }
}
