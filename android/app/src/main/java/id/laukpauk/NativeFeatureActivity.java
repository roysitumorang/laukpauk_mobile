package id.laukpauk;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Temporary native destination while individual buyer/seller Dart screens
 * are ported. It is intentionally honest about features not yet migrated.
 */
public final class NativeFeatureActivity extends Activity {
    public static final String EXTRA_ROUTE = "native.route";
    public static final String EXTRA_QUERY = "native.query";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        if (route == null || route.isEmpty()) route = "home";
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        int pad = Math.round(24 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Laukpauk");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(36, 125, 76));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView routeText = new TextView(this);
        routeText.setText("Rute: " + route + "\n\nLayar ini belum dimigrasikan. Fitur Dart asli tetap tersedia di sumber migrasi.");
        routeText.setTextSize(16);
        routeText.setTextColor(Color.DKGRAY);
        routeText.setGravity(Gravity.CENTER);
        routeText.setPadding(0, pad, 0, 0);
        root.addView(routeText);
        setContentView(root);
    }
}
