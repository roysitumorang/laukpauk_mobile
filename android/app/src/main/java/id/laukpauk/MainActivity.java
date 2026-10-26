package id.laukpauk;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/** Native Android entry point. Feature screens are migrated incrementally. */
public final class MainActivity extends AppCompatActivity {
    private static final int INK = Color.rgb(35,45,38);
    private static final int GREEN = Color.rgb(35,126,76);
    private static final int MUTED = Color.rgb(104,115,106);
    private LinearLayout content;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showHome();
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22),dp(20),dp(22),dp(28));
        content.setBackgroundColor(Color.rgb(250,250,247));
        scroll.addView(content);

        content.addView(label("Laukpauk",30,GREEN,true), matchWrap());
        TextView subtitle = label("Belanja kebutuhan dapur, lebih mudah.",15,MUTED,false);
        LinearLayout.LayoutParams sub = matchWrap(); sub.topMargin=dp(6);
        content.addView(subtitle,sub);

        TextView hero = label("Bahan segar untuk masakan hari ini",22,Color.WHITE,true);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(20),dp(22),dp(20),dp(22));
        hero.setBackground(background(GREEN,18));
        LinearLayout.LayoutParams hp=matchWrap(); hp.topMargin=dp(28);
        content.addView(hero,hp);

        TextView heading=label("Mulai dari sini",19,INK,true);
        LinearLayout.LayoutParams sh=matchWrap(); sh.topMargin=dp(26); sh.bottomMargin=dp(8);
        content.addView(heading,sh);
        addAction("Masuk / Daftar","Akses akun dan pesanan Anda","Layar autentikasi sedang dalam proses migrasi ke Java.");
        addAction("Cari produk","Jelajahi produk dan pasar sekitar","Katalog produk sedang dalam proses migrasi ke Java.");
        addAction("Keranjang","Tinjau belanjaan sebelum checkout","Keranjang dan checkout sedang dalam proses migrasi ke Java.");
        addAction("Pesanan saya","Lihat status dan riwayat pesanan","Pesanan sedang dalam proses migrasi ke Java.");

        TextView note=label("Versi native Android · Java",12,MUTED,false); note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np=matchWrap(); np.topMargin=dp(30); content.addView(note,np);
        setContentView(scroll);
    }

    private void addAction(String title,String description,String message) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(14)); card.setBackground(background(Color.WHITE,14));
        card.setClickable(true); card.setFocusable(true);
        card.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Laukpauk")
            .setMessage(message).setPositiveButton("OK",null).show());
        card.addView(label(title,16,INK,true),matchWrap());
        TextView detail=label(description,13,MUTED,false);
        LinearLayout.LayoutParams dp=matchWrap(); dp.topMargin=this.dp(4); card.addView(detail,dp);
        LinearLayout.LayoutParams cp=matchWrap(); cp.topMargin=dp(8); content.addView(card,cp);
    }
    private TextView label(String text,int size,int color,boolean bold) {
        TextView v=new TextView(this); v.setText(text); v.setTextSize(size); v.setTextColor(color);
        if(bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }
    private GradientDrawable background(int color,int radius) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT);
    }
    private int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
}
