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
        setContentView(R.layout.activity_login);
        phone = findViewById(R.id.login_phone);
        password = findViewById(R.id.login_password);
        status = findViewById(R.id.login_status);
        submit = findViewById(R.id.login_submit);
        submit.setOnClickListener(v -> login());
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
