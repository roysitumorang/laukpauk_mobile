package id.laukpauk;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NativeLauncherTest {
    @Test public void launcherStartsWithoutFlutterRuntime() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity.findViewById(android.R.id.content));
                assertEquals("id.laukpauk", activity.getPackageName());
            });
        }
    }

    @Test public void buyerRouteOpensNativeDestination() {
        Intent intent = new Intent(androidx.test.core.app.ApplicationProvider.getApplicationContext(),
                NativeFeatureActivity.class);
        intent.putExtra(NativeFeatureActivity.EXTRA_ROUTE, "buyer");
        try (ActivityScenario<NativeFeatureActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> assertNotNull(activity.findViewById(android.R.id.content)));
        }
    }
}
