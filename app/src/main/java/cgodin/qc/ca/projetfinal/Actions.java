package cgodin.qc.ca.projetfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class Actions extends AppCompatActivity {
    // Image tuile
    ImageView imgTuile1;
    ImageView imgTuile2;
    ImageView imgTuile3;
    ImageView imgTuile4;
    ImageView imgTuile5;
    ImageView imgTuile6;
    ImageView imgTuile7;

    TextView txtCourriel;
    String username = "";
    TextView txtPoints;
    TextView txtCredits;
    TextView txtGroupe;
    TextView txtRole;
    ImageView imgCompte;
    Button btnMessagePrive;
    TextView txtMsgPrive;
    Button btnMessagePublique;
    TextView txtMsgPublique;
    Button btnDeconnexion;
    TextView txtDerniereCommande;

    Login login = new Login();


    //Choix
    private RadioGroup rg;
    private CheckBox cbArbitre;
    private String urlLocal = "10.0.2.2";
    private String urlLocalServer = "10.0.2.2";


    //Liste pour content
    private List<String> usernameAilleur  = new ArrayList<>();
    private List<String> avatarAilleur  = new ArrayList<>();
    private List<String> usernameAttente  = new ArrayList<>();
    private List<String> avatarAttente  = new ArrayList<>();
    private List<String> usernameSpectateur  = new ArrayList<>();
    private List<String> avatarSpectateur  = new ArrayList<>();
    private List<String> usernameArbitre  = new ArrayList<>();
    private List<String> avatarArbitre  = new ArrayList<>();

    //Temps
    String t = "";

    private StompClient mStompClient;
    OkHttpClient client = new OkHttpClient();
    String userRouge = "";
    String userBlanc = "";
    String userArbitre = "";
    String AvatarRouge = "";
    String AvatarBlanc = "";
    String AvatarArbitre = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.actions_layout);

        txtCourriel = findViewById(R.id.txtCouriel);
        txtPoints = findViewById(R.id.txtPoints);
        txtCredits = findViewById(R.id.txtCredits);
        txtGroupe = findViewById(R.id.txtGroupe);
        txtRole = findViewById(R.id.txtRole);
        imgCompte = findViewById(R.id.imgCompte);
        rg = findViewById(R.id.endroitCombattant);
        cbArbitre = findViewById(R.id.checkBox);
        btnMessagePrive = findViewById(R.id.btnMsgPrive);
        txtMsgPrive = findViewById(R.id.txtMsgPrive);
        btnMessagePublique = findViewById(R.id.btnMsgPublique);
        txtMsgPublique = findViewById(R.id.txtMsgPublique);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
        txtDerniereCommande = findViewById(R.id.txtDerniereCommande);
        imgTuile1 = findViewById(R.id.tatami1);
        imgTuile2 = findViewById(R.id.tatami2);
        imgTuile3 = findViewById(R.id.tatami3);
        imgTuile4 = findViewById(R.id.tatami4);
        imgTuile5 = findViewById(R.id.tatami5);
        imgTuile6 = findViewById(R.id.tatami6);
        imgTuile7 = findViewById(R.id.tatami7);

        username = getIntent().getExtras().getString("username").toString();
        infoCompte(false);

        t =  Long.toString(System.currentTimeMillis());

        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://"+urlLocal+":8093/webSocket/websocket");
        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {

                case OPENED:
                    Log.d("STOMP", "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e("STOMP", "Error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    Log.d("STOMP", "Stomp connection closed");
                    break;
            }
        });
        mStompClient.connect();
        mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"AILLEURS\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        //Force à retirer un nouvel arrivant de la liste arbitre (Pour une raison quand on restart l'app il était jamais retirer (Passait pas par onDestroy)
        mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"ARBITRE-NO\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();


        // DÉBUTE 2.5S APRES LA CRÉATION ET RAFRAICHIT A CHAQUE 1.5S
        EnRond boucle = new EnRond(2000,2000);
        boucle.start();

        //Action clique
        cbArbitre.setOnClickListener(v -> checkBoxClick());
        rg.setOnCheckedChangeListener((group, checkedId) -> radioGroupClick());


        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.terminerConnexion(username,getBaseContext());
                finish();
            }
        });

        mStompClient.topic("/sujet/debutCombat").subscribe(topicMessage -> {

            //Log.d("STOMP", topicMessage.getPayload());
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String[] strTexte = jsonObj.getString("texte").split("-A-");
            for(int i = 0;i < strTexte.length;i++){
                String urldisplay = strTexte[i];
                try {
                        JSONObject objDansCombat = new JSONObject(urldisplay);
                        switch(i) {
                            case 0:
                                userRouge = objDansCombat.getString("courriel");
                                AvatarRouge = getAvatar(objDansCombat.getString("avatar"));
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        byte[] decodedString = Base64.decode(AvatarRouge, Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        imgTuile7.setImageBitmap(bmp);
                                        if(username.equals(userRouge))  {;
                                            imgTuile7.setPadding(1,2,1,2);
                                            imgTuile7.setBackgroundColor(Color.RED);
                                        }
                                    }
                                });
                                break;
                            case 1:
                                userBlanc = objDansCombat.getString("courriel");
                                AvatarBlanc = getAvatar(objDansCombat.getString("avatar"));
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        byte[] decodedString = Base64.decode(AvatarBlanc, Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        imgTuile1.setImageBitmap(bmp);
                                        if(username.equals(userBlanc)) {
                                            imgTuile1.setPadding(1,2,1,2);
                                            imgTuile1.setBackgroundColor(Color.RED);
                                        }
                                    }
                                });
                                break;
                            case 2:
                                userArbitre = objDansCombat.getString("courriel");
                                AvatarArbitre = getAvatar(objDansCombat.getString("avatar"));
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        byte[] decodedString = Base64.decode(AvatarArbitre, Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        imgTuile4.setImageBitmap(bmp);
                                        if(username.equals(userArbitre)) {
                                            imgTuile4.setPadding(1,2,1,2);
                                            imgTuile4.setBackgroundColor(Color.RED);
                                        }
                                    }
                                });
                                break;
                        }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

        mStompClient.topic("/sujet/envoyerChiffre").subscribe(topicMessage -> {
//
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String[] strTexte = jsonObj.getString("texte").split("-A-");
            for(int i = 0;i < strTexte.length;i++){
                String urldisplay = strTexte[i];
                try {
                    JSONObject objDansCombat = new JSONObject(urldisplay);
                    switch(i) {
                        case 0:
                            userRouge = objDansCombat.getString("courriel");
                            AvatarRouge = getAvatar(objDansCombat.getString("avatar"));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    byte[] decodedString = Base64.decode(AvatarRouge, Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgTuile7.setImageBitmap(bmp);
                                    if(username.equals(userRouge)) {
                                        imgTuile7.setPadding(1,2,1,2);
                                        imgTuile7.setBackgroundColor(Color.RED);
                                    }
                                }
                            });
                            break;
                        case 1:
                            userBlanc = objDansCombat.getString("courriel");
                            AvatarBlanc = getAvatar(objDansCombat.getString("avatar"));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    byte[] decodedString = Base64.decode(AvatarBlanc, Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgTuile1.setImageBitmap(bmp);
                                    if(username.equals(userBlanc)) {
                                        imgTuile1.setPadding(1,2,1,2);
                                        imgTuile1.setBackgroundColor(Color.RED);
                                    }
                                }
                            });
                            break;
                        case 2:
                            userArbitre = objDansCombat.getString("courriel");
                            AvatarArbitre = getAvatar(objDansCombat.getString("avatar"));
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    byte[] decodedString = Base64.decode(AvatarArbitre, Base64.DEFAULT);
                                    Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    imgTuile4.setImageBitmap(bmp);System.out.println(username);
                                    if(username.equals(userArbitre)) {
                                        imgTuile4.setPadding(1,2,1,2);
                                        imgTuile4.setBackgroundColor(Color.RED);
                                    }
                                }
                            });
                            break;
                    }
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String[] combat = jsonObj.getString("avatar").split("-A-");
            for(int i =0;i<combat.length;i++){
                placerImage(i,combat[i]);
            }
            Thread.sleep(2000);
           switch(quiGagne(combat[0],combat[1])){
               case "NULLE":
                   runOnUiThread(new Runnable() {

                       @Override
                       public void run() {
                           imgTuile3.setImageResource(R.mipmap.drapeau);
                           imgTuile3.setBackgroundColor(Color.WHITE);
                           imgTuile5.setImageResource(R.mipmap.drapeau);
                           imgTuile5.setBackgroundColor(Color.WHITE);
                       }});
                   break;
               case "ROUGE":
                   runOnUiThread(new Runnable() {

                       @Override
                       public void run() {
                           imgTuile5.setImageResource(R.mipmap.drapeau);
                           imgTuile5.setBackgroundColor(Color.WHITE);
                       }});
                   break;
               case "BLANC":
                   runOnUiThread(new Runnable() {

                       @Override
                       public void run() {
                           imgTuile3.setImageResource(R.mipmap.drapeau);
                           imgTuile3.setBackgroundColor(Color.WHITE);
                       }});
                   break;
           }
        });
        mStompClient.topic("/sujet/finCombat").subscribe(topicMessage -> {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    imgTuile1.setBackgroundColor(Color.WHITE);
                    imgTuile4.setBackgroundColor(Color.WHITE);
                    imgTuile7.setBackgroundColor(Color.WHITE);
                    imgTuile1.setImageResource(R.mipmap.tatamivide);
                    imgTuile2.setImageResource(R.mipmap.tatamivide);
                    imgTuile3.setImageResource(R.mipmap.tatamivide);
                    imgTuile4.setImageResource(R.mipmap.tatamivide);
                    imgTuile5.setImageResource(R.mipmap.tatamivide);
                    imgTuile6.setImageResource(R.mipmap.tatamivide);
                    imgTuile7.setImageResource(R.mipmap.tatamivide);
                    infoCompte(true);
                }
            });
        });
    }

    public String quiGagne(String chiffreBlanc, String chiffreRouge){
        if(chiffreBlanc.equals(chiffreRouge)){
            return "NULLE";
        }
        else{
            switch(chiffreBlanc){
                case "1":
                    if(chiffreRouge.equals("2")) return "ROUGE";
                    else return "BLANC";
                case "2":
                    if(chiffreRouge.equals("3")) return "ROUGE";
                    else return "BLANC";
                case "3":
                    if(chiffreRouge.equals("1")) return  "ROUGE";
                    else return "BLANC";
            }
            return "BUG";
        }
    }
    public int trouverMain(String mainJouee){
        switch(mainJouee){
            case "1":
                return R.mipmap.roche;
            case "2":
                return R.mipmap.papier;
            case "3":
                return R.mipmap.ciseau;
            default:
                return 0;
        }
    }

    public void placerImage(int indice, String mainJouee){
        switch(indice){
            case 0:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        imgTuile2.setImageResource(trouverMain(mainJouee));
                        imgTuile2.setBackgroundColor(Color.WHITE);
                    }
                });
                break;
            case 1:
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        imgTuile6.setImageResource(trouverMain(mainJouee));
                        imgTuile6.setBackgroundColor(Color.WHITE);
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        //Quitter sur fermeture de la page
        t =  Long.toString(System.currentTimeMillis());
        mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"PEACE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        mStompClient.disconnect();
        super.onDestroy();
    }

    public void infoCompte(Boolean enCours)   {
//https://square.github.io/okhttp/
        Log.d("COMPTE", "INFO COMPTE");
        String json = "";
        String url = "http://"+urlLocalServer+":8093/CompteSelect/"+username;
        try {
            if(enCours){
                post3(url,json);
            }
            else{
                post2(url ,json);
            }
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
            new DownloadCompteTask().execute(responseData);
        }
        return responseData;
    }

    public String getAvatar(String strNomAvatar)   {
//https://square.github.io/okhttp/
        String json = "";
        System.out.println("AVATAR : " + strNomAvatar);
        String url = "http://"+urlLocalServer+":8093/TrouverAvatarSupreme/"+strNomAvatar;
            try{

                return(postAvatar(url,json).replace("data:image/jpeg;base64,",""));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        return "";
    }
    String postAvatar(String url, String json) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        final String responseData = response.body().string();
        if (response.isSuccessful()) {
            System.out.println("Retour " + responseData);
            return responseData;
        }
        else{

            return responseData;
        }
    }
    private class DownloadCompteTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String urldisplay = urls[0];
            String compte = "";
            Bitmap bmp;
            JSONObject obj = null;
            try {
                obj = new JSONObject(urldisplay);
                byte[] decodedString = Base64.decode(obj.getString("avatar").toString(), Base64.DEFAULT);
                bmp = BitmapFactory.decodeByteArray(decodedString, 0,decodedString.length);
                imgCompte.setImageBitmap(bmp);
               txtCourriel.setText(obj.getString("username").toString());
               txtPoints.setText("Points : "+obj.getString("points").toString() );
               txtCredits.setText("Crédits : "+obj.getString("credits").toString());
               txtGroupe.setText("Ceinture : "+obj.getString("groupe").toString());
               txtRole.setText("Role : "+obj.getString("role").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return compte;
        }
        protected void onPostExecute(String result) {

        }


    }
    public class EnRond extends CountDownTimer {

        public EnRond(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            this.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            //Appel au liste
            ListeAilleur();
            ListeAttente();
            ListeSpectateur();
            ListeArbitre();
            infoCompte(true);
        }

    }

    //Action sur choix dans le radiogroup
    public void radioGroupClick(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        String valRg =  ((RadioButton) rg.getChildAt(rg.indexOfChild(rg.findViewById(rg.getCheckedRadioButtonId())))).getText().toString().toUpperCase();

        if(valRg.equals("AILLEURS")){
            mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"ARBITRE-NO\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
            cbArbitre.setEnabled(false);
            cbArbitre.setChecked(false);
        }
        else{
            cbArbitre.setEnabled(true);
        }
        t =  Long.toString(System.currentTimeMillis());


        mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\""+valRg+"\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();

    }

    //Action checkboxArbitre
    public void checkBoxClick(){
        t =  Long.toString(System.currentTimeMillis());
        if(cbArbitre.isChecked()){
            mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"ARBITRE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        }
        else{
            mStompClient.send("/app/listePosition",  "{\"de\":\""+username+"\",\"texte\":\"ARBITRE-NO\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        }
    }

    private class RempliListTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            String liste = urls[0].substring(0,3);
            if(liste.equals("AIL")||liste.equals("ATT")||liste.equals("SPE")||liste.equals("ARB")){
                String urldisplay = urls[0].substring(3,urls[0].length());
                try {
                    JSONArray jsonArr = new JSONArray(urldisplay);

                    for (int i = 0; i < jsonArr.length(); i++)
                    {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);
                        switch(liste) {
                            case "AIL":
                                usernameAilleur.add(jsonObj.getString("courriel"));
                                avatarAilleur.add(jsonObj.getString("avatarChaine"));
                                break;
                            case "ATT":
                                usernameAttente.add(jsonObj.getString("courriel"));
                                avatarAttente.add(jsonObj.getString("avatarChaine"));
                                break;
                            case "SPE":
                                usernameSpectateur.add(jsonObj.getString("courriel"));
                                avatarSpectateur.add(jsonObj.getString("avatarChaine"));
                                break;
                            case "ARB":
                                usernameArbitre.add(jsonObj.getString("courriel"));
                                avatarArbitre.add(jsonObj.getString("avatarChaine"));
                        }
                    }
                    if(jsonArr.length()==0){
                        switch(liste) {
                            case "AIL":
                                usernameAilleur.add("");
                                avatarAilleur.add("");
                                break;
                            case "ATT":
                                usernameAttente.add("");
                                avatarAttente.add("");
                                break;
                            case "SPE":
                                usernameSpectateur.add("");
                                avatarSpectateur.add("");
                                break;
                            case "ARB":
                                usernameArbitre.add("");
                                avatarArbitre.add("");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                String urldisplay = urls[0];
                Bitmap bmp;
                try {
                        final JSONObject obj = new JSONObject(urldisplay);
                        byte[] decodedString = Base64.decode(obj.getString("avatar").toString(), Base64.DEFAULT);
                        bmp = BitmapFactory.decodeByteArray(decodedString, 0,decodedString.length);
                        imgCompte.setImageBitmap(bmp);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    txtCourriel.setText(obj.getString("username").toString());
                                    txtPoints.setText("Points : " + obj.getString("points").toString());
                                    txtCredits.setText("Crédits : " + obj.getString("credits").toString());
                                    txtGroupe.setText("Ceinture : " + obj.getString("groupe").toString());
                                    txtRole.setText("Role : " + obj.getString("role").toString());
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        liste="false";
//                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return liste;
        }
        protected void onPostExecute(String result) {
            if(!result.equals("false")){
                RecyclerView recyclerView = null;

                if(usernameAilleur.size() > 0 && avatarAilleur.size() > 0 ){
                    recyclerView = findViewById(R.id.recyclerAilleurs);
                    setupRecyclerView(recyclerView,usernameAilleur,avatarAilleur);
                }
                if(usernameSpectateur.size() > 0 && avatarSpectateur.size() > 0){
                    recyclerView = findViewById(R.id.recyclerSpectateur);
                    setupRecyclerView(recyclerView,usernameSpectateur,avatarSpectateur);
                }

                if(usernameAttente.size() > 0 && avatarAttente.size() > 0){
                    recyclerView = findViewById(R.id.recyclerAttente);
                    setupRecyclerView(recyclerView,usernameAttente,avatarAttente);
                }

                if(usernameArbitre.size() > 0 && avatarArbitre.size() > 0) {
                    recyclerView = findViewById(R.id.recyclerArbitre);
                    setupRecyclerView(recyclerView,usernameArbitre,avatarArbitre);
                }
            }
        }



    }
    private void setupRecyclerView(RecyclerView recyclerView, List<String> username, List<String> avatar) {
        if(username.size() > 0 & avatar.size() > 0){
            Adapter myAdapter = new Adapter(this,username,avatar);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(myAdapter);
        }
    }

    //Obtention liste des combattants "ailleurs"
    public void ListeAilleur()   {
        usernameAilleur.clear();
        avatarAilleur.clear();
        String json = "";
        String url = "http://"+urlLocalServer+":8093/ListeAilleur";
        try {
            post3(url ,json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Obtention lsite des combattants en "attente"
    public void ListeAttente()   {
        usernameAttente.clear();
        avatarAttente.clear();
        String json = "";
        String url = "http://"+urlLocalServer+":8093/ListeAttente";
        try {
            post3(url ,json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Obtention liste des combattants "spectateur"
    public void ListeSpectateur()   {
        usernameSpectateur.clear();
        avatarSpectateur.clear();
        String json = "";
        String url = "http://"+urlLocalServer+":8093/ListeSpectateur";
        try {
            post3(url ,json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Obtention liste des combatants "arbitre"
    public void ListeArbitre()   {
        usernameArbitre.clear();
        avatarArbitre.clear();
        String json = "";
        String url = "http://"+urlLocalServer+":8093/ListeArbitre";
        try {
            post3(url ,json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String post3(String url, String json) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        //Attention, on ne peut pas lire plus d'une fois la valeur de response.body().string()????
        //Voilà pourquoi, j'utilise et retourne responseData.

        final String responseData = response.body().string();
        if (response.isSuccessful() && !responseData.trim().equals("")) {
            new Actions.RempliListTask().execute(responseData);
        }
        else {
            new Actions.RempliListTask().execute("Vous n'êtes pas connecté!");
        }
        return responseData;
    }

}