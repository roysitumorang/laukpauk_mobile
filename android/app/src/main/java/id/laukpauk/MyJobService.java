package id.laukpauk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class MyJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(this, "Toast onStartJon", Toast.LENGTH_SHORT);
        System.out.println("onStartJob is executed");

        System.out.println("end of onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Toast.makeText(this, "Toast onStopJob", Toast.LENGTH_SHORT);
        System.out.println("onStopJob is executed");
        return false;
    }
}
