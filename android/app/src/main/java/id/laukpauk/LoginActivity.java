package id.laukpauk;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class LoginActivity extends Activity {
    private EditText phone, password;
    private TextView status;
    private Button submit;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        int pad = dp(24);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad,pad,pad,pad);
        root.setBackgroundColor(Color.WHITE);
        TextView title = new TextView(this);
        title.setText("Masuk ke Laukpauk");
        title.setTextColor(Color.rgb(36,125,76));
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        phone = new EditText(this);
        phone.setSingleLine(true);
        phone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        phone.setHint("Nomor HP");
        root.addView(phone,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(54)));
        password = new EditText(this);
        password.setSingleLine(true);
        password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.setHint("Kata sandi");
        root.addView(password,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(54)));
        submit = new Button(this);
        submit.setAllCaps(false);
        submit.setText("Masuk");
        submit.setTextColor(Color.WHITE);
        submit.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(36,125,76)));
        submit.setOnClickListener(v -> login());
        root.addView(submit,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(52)));
        status = new TextView(this);
        status.setTextColor(Color.DKGRAY);
        status.setPadding(0,dp(16),0,0);
        root.addView(status);
        setContentView(root);
    }
    private void login() {
        String p=phone.getText().toString().trim(), pw=password.getText().toString();
        if(p.isEmpty()||pw.isEmpty()){status.setText("Isi nomor HP dan kata sandi.");return;}
        submit.setEnabled(false);
        status.setText("Memeriksa akun…");
        try {
            NativeAuthService.login(this,p,pw,new NativeAuthService.BooleanCallback(){
                @Override public void onResult(boolean ok) {
                    submit.setEnabled(true);
                    if(ok) { status.setText("Login berhasil."); finish(); }
                    else status.setText("Login gagal. Periksa nomor HP dan kata sandi.");
                }
                @Override public void onError(Exception e) {
                    submit.setEnabled(true);
                    status.setText("Login gagal: "+e.getMessage()+"\nPeriksa konfigurasi LAUKPAUK_API_BASE_URL.");
                }
            });
        } catch(Exception e) { submit.setEnabled(true); status.setText("Konfigurasi API belum siap: "+e.getMessage()); }
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
