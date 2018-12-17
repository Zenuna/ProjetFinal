package cgodin.qc.ca.projetfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Historique extends AppCompatActivity {

    String username= "";
    private String urlLocalServer = "10.0.2.2";
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.historique_layout);
        username = getIntent().getExtras().getString("username");
        infoHistorique();

    }

    public void infoHistorique()   {
//https://square.github.io/okhttp/
        Log.d("COMPTE", "INFO COMPTE");
        String json = "";
        String url = "http://"+urlLocalServer+":8093/Historique/"+username;
        try {
           post2(url ,json);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    String post2(String url, String json) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        final String responseData = response.body().string();
        if (response.isSuccessful()) {
            new Historique.DownloadHistorique().execute(responseData);
        }
        return responseData;
    }
    private class DownloadHistorique extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String urldisplay = urls[0];


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        TextView txtHistorique = findViewById(R.id.txtHistorique);
                        txtHistorique.setText(Html.fromHtml(urldisplay));
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            return urldisplay;
        }
        protected void onPostExecute(String result) {

        }


    }
}
