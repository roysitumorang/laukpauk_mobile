package id.laukpauk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(36, 125, 76);
    private static final int INK = Color.rgb(36, 45, 39);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NativePreferences.migrateKnownLegacyValues(getApplicationContext());
        getWindow().setStatusBarColor(GREEN);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showHome();
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void showHome() {
        int pad = dp(22);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(pad, pad, pad, pad);
        page.setBackgroundColor(Color.WHITE);

        TextView brand = new TextView(this);
        brand.setText("Laukpauk");
        brand.setTextColor(GREEN);
        brand.setTextSize(30);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        page.addView(brand, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));

        TextView tagline = new TextView(this);
        tagline.setText("Belanja kebutuhan dapur, lebih mudah.");
        tagline.setTextColor(INK);
        tagline.setTextSize(17);
        tagline.setPadding(0, dp(8), 0, dp(24));
        page.addView(tagline);

        addAction(page, "Mulai belanja", "Pembeli", "buyer");
        addAction(page, "Kelola toko", "Penjual", "seller");

        TextView note = new TextView(this);
        note.setText("Aplikasi Android native Java");
        note.setTextColor(Color.GRAY);
        note.setTextSize(12);
        note.setPadding(0, dp(24), 0, 0);
        page.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        setContentView(scroll);
    }

    private void addAction(LinearLayout parent, String title, String subtitle, String route) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(title + "  ·  " + subtitle);
        button.setTextColor(Color.WHITE);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));
        button.setOnClickListener(v -> {
            Intent intent = new Intent(this, NativeFeatureActivity.class);
            intent.putExtra(NativeFeatureActivity.EXTRA_ROUTE, route);
            startActivity(intent);
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        params.bottomMargin = dp(12);
        parent.addView(button, params);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent == null ? null : intent.getData();
        if (data == null) return;
        String route = data.getPath();
        if (route == null || route.isEmpty() || "/".equals(route)) return;
        Intent destination = new Intent(this, NativeFeatureActivity.class);
        destination.putExtra(NativeFeatureActivity.EXTRA_ROUTE, route);
        destination.putExtra(NativeFeatureActivity.EXTRA_QUERY, data.getQuery());
        startActivity(destination);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
