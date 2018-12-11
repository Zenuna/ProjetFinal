package cgodin.qc.ca.projetfinal;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public String JSESSIONID = new String();
    public String JSESSIONIDETTOUTLERESTE = new String();

    OkHttpClient client = new OkHttpClient();

       private String urlLocalServer = "10.0.2.2";

    public void etablirConnexion(String username,Context context)   {
        Log.d("STOMP", "etablirConnexion()");
        String json = "";
        String url = "http://"+urlLocalServer+":8093/login";
        try {
            post(url ,json,username, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("STOMP", "JSESSIONID=" + JSESSIONID);
    }

    public void terminerConnexion(String username,Context context)   {
        Log.d("STOMP", "terminerConnexion()");
        String json = "";
        String url = "http://"+urlLocalServer+":8093/logout";
        try {
            post(url ,json,username,context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSESSIONID="";
    }

    String post(String url, String json,String username, Context context) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .addEncoded("username", username)
                .addEncoded("password", "Patate123")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("rest","oui")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();


        Response response = client.newCall(request).execute();
        if (response.isSuccessful())
        {
            Log.d("CLASS ", "CLASS OF CONTEXT" + context.getPackageName().getClass().getSimpleName());

            String  cookies = response.header("Set-Cookie");
            MainActivity.SESSIONREST = response.header("SESSIONREST");
            Log.d("STOMP", "mainActivity.SESSIONREST:" + MainActivity.SESSIONREST);
            Log.d("Cookies", "OK : "+cookies);
            if (cookies != null) {
                JSESSIONIDETTOUTLERESTE=cookies;
                String[]  cookiesSplit = cookies.split(";");
                JSESSIONID = "JSESSIONID=PAS DE JSESSIONID";
                for (String cookie : cookiesSplit) {
                    String[] cookieSplit = cookie.split("=");
                    if ((cookieSplit[0] != null) && (cookieSplit[0].trim().matches("JSESSIONID"))) {
                        if (cookieSplit[1] != null)
                            JSESSIONID = cookieSplit[1].trim();
                        else
                            JSESSIONID = "JSESSIONID=VIDE";
                    }
                }
            }
        }
        else{
            JSESSIONID = "ERREUR LOGIN";
        }
        return response.body().string();
    }

}
