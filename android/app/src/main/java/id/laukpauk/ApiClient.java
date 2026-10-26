package id.laukpauk;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Framework-free HTTP client. Supply the verified backend base URL before use. */
public final class ApiClient {
    public interface Callback {
        void onSuccess(int statusCode, JSONObject body);
        void onError(Exception error);
    }
    private static final int TIMEOUT_MS=15000;
    private final ExecutorService executor=Executors.newFixedThreadPool(3);
    private final Handler main=new Handler(Looper.getMainLooper());
    private final String baseUrl;
    private final NativePreferences preferences;
    public ApiClient(String baseUrl,NativePreferences preferences) {
        if(baseUrl==null||baseUrl.trim().isEmpty()) throw new IllegalArgumentException("API base URL must be configured");
        this.baseUrl=baseUrl.replaceAll("/+$",""); this.preferences=preferences;
    }
    public void get(String path,Callback callback) { request("GET",path,null,callback); }
    public void post(String path,JSONObject body,Callback callback) { request("POST",path,body,callback); }
    private void request(String method,String path,JSONObject body,Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection=null;
            try {
                URL url=new URL(baseUrl+(path.startsWith("/")?path:"/"+path));
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod(method); connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
                String token=preferences==null?"":preferences.getAuthToken();
                if(!token.isEmpty()) connection.setRequestProperty("Authorization","Bearer "+token);
                if(body!=null&&"POST".equals(method)) {
                    connection.setDoOutput(true);
                    try(OutputStream out=connection.getOutputStream()) {
                        out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                int status=connection.getResponseCode();
                InputStream stream=status>=200&&status<400?connection.getInputStream():connection.getErrorStream();
                StringBuilder text=new StringBuilder();
                if(stream!=null) try(BufferedReader reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
                    String line; while((line=reader.readLine())!=null) text.append(line);
                }
                JSONObject json=text.length()==0?new JSONObject():new JSONObject(text.toString());
                main.post(() -> callback.onSuccess(status,json));
            } catch(Exception e) { main.post(() -> callback.onError(e)); }
            finally { if(connection!=null) connection.disconnect(); }
        });
    }
    public void shutdown() { executor.shutdown(); }
}
