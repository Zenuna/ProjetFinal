package cgodin.qc.ca.projetfinal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public String JSESSIONID = new String();
    public String JSESSIONIDETTOUTLERESTE = new String();

    OkHttpClient client = new OkHttpClient();
    public static String SESSIONREST = new String();
    Button btnConnexion;
    String usernameSelect ="";
    Login login = new Login();
    private List<String> username  = new ArrayList<>();
    private List<String> avatar  = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);
        btnConnexion = findViewById(R.id.btnConnexion);
        recyclerView = findViewById(R.id.recyclerComptes);
        loadComptes();


        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Le username: "+usernameSelect ,Toast.LENGTH_SHORT).show();
                login.etablirConnexion(usernameSelect,getApplicationContext());

                Intent action = new Intent(MainActivity.this, Actions.class);
                action.putExtra("username",usernameSelect);
                startActivity(action);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            usernameSelect = intent.getStringExtra("username");
        }
    };


    public void loadComptes()   {
//https://square.github.io/okhttp/
        Log.d("STOMP", "loadComptes()");
        String json = "";
        String url = "http://10.0.2.2:8093/Comptes";
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
        //Attention, on ne peut pas lire plus d'une fois la valeur de response.body().string()????
        //Voilà pourquoi, j'utilise et retourne responseData.

        final String responseData = response.body().string();
        if (response.isSuccessful()) {
            new DownloadCompteTask().execute(responseData);
        }
        return responseData;
    }



    private class DownloadCompteTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String urldisplay = urls[0];
            String compte = "";
            JSONObject obj = null;
            try {
                JSONArray jsonArr = new JSONArray(urldisplay);

                for (int i = 0; i < jsonArr.length(); i++)
                {
                    JSONObject jsonObj = jsonArr.getJSONObject(i);
                    username.add(jsonObj.getString("username").toString());
                    avatar.add(jsonObj.getString("avatar").toString());
                    Log.d("STOMP", "Points"+jsonObj.getString("points").toString());
                    Log.d("STOMP", "Credits"+jsonObj.getString("credits").toString());
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return compte;
        }
        protected void onPostExecute(String result) {
            setupRecyclerView(username,avatar);
        }


    }

    private void setupRecyclerView(List<String> username, List<String> avatar) {
        Adapter myAdapter = new Adapter(this,username,avatar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);
    }
}
