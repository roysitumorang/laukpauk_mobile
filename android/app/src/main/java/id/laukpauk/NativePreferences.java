package id.laukpauk;

import android.content.Context;
import android.content.SharedPreferences;

/** Native preference wrapper. Do not store payment card details here. */
public final class NativePreferences {
    private final SharedPreferences preferences;
    public NativePreferences(Context context) {
        preferences=context.getApplicationContext().getSharedPreferences("laukpauk_preferences",Context.MODE_PRIVATE);
    }
    public String getAuthToken() { return preferences.getString("auth_token",""); }
    public void saveAuthToken(String token) { preferences.edit().putString("auth_token",token==null?"":token).apply(); }
    public void clearSession() { preferences.edit().remove("auth_token").apply(); }
    public String getString(String key,String fallback) { return preferences.getString(key,fallback); }
    public void putString(String key,String value) { preferences.edit().putString(key,value).apply(); }
}
