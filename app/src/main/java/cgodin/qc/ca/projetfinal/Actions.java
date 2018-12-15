package cgodin.qc.ca.projetfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    String messageCommande = "";

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

        //BtnCombat
        Button btnCombat1 = findViewById(R.id.btnCombat1);
        Button btnCombat2 = findViewById(R.id.btnCombat2);
        Button btnCombat3 = findViewById(R.id.btnCombat3);

        //BtnArbitre
        Button btnArbitre1 = findViewById(R.id.btnArbitrer1);
        Button btnArbitre2 = findViewById(R.id.btnArbitrer2);


        //BtnExamen
        Button btnExamen1 = findViewById(R.id.btnExamen1);
        Button btnExamen2 = findViewById(R.id.btnExamen2);

        //BtnAncien
        Button btnAncien = findViewById(R.id.btnAncien);


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

    }

    public void afficherMessageCommande(final String message){
        final String s = message;

        txtDerniereCommande.post(new Runnable() {
            @Override
            public void run() {
                txtDerniereCommande.setText(s);
            }
        });
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

                   /* username.add(jsonObj.getString("username").toString());
                    avatar.add(jsonObj.getString("avatar").toString());
                    Log.d("STOMP", "Points"+jsonObj.getString("points").toString());
                    Log.d("STOMP", "Credits"+jsonObj.getString("credits").toString());
*/


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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                String urldisplay = urls[0];
                String compte = "";
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return liste;
        }
        protected void onPostExecute(String result) {
            if(!result.equals("false")){
                RecyclerView recyclerView = null;

                recyclerView = findViewById(R.id.recyclerAilleurs);
                setupRecyclerView(recyclerView,usernameAilleur,avatarAilleur);

                recyclerView = findViewById(R.id.recyclerSpectateur);
                setupRecyclerView(recyclerView,usernameSpectateur,avatarSpectateur);

                recyclerView = findViewById(R.id.recyclerAttente);
                setupRecyclerView(recyclerView,usernameAttente,avatarAttente);

                recyclerView = findViewById(R.id.recyclerArbitre);
                setupRecyclerView(recyclerView,usernameArbitre,avatarArbitre);
            }
        }



    }
    private void setupRecyclerView(RecyclerView recyclerView, List<String> username, List<String> avatar) {
        Adapter myAdapter = new Adapter(this,username,avatar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);
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
        if (response.isSuccessful()) {
            new Actions.RempliListTask().execute(responseData);
        }
        else {
            new Actions.RempliListTask().execute("Vous n'êtes pas connecté!");
        }
        return responseData;
    }

    // Nouvelles methodes

    public void subscribeComabt(String strCombat){
        mStompClient.topic("/sujet/Combat").subscribe(topicMessage -> {
            //Log.d("STOMP", topicMessage.getPayload());
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String strTexte = jsonObj.getString("texte").toString();
            if(strTexte.equals("OK")){
                afficherMessageCommande("Combat généré. ("+strCombat+")");
            }else
                afficherMessageCommande(strTexte);

        });
    }

    public void subsribeArbitre(String strCombat){
        mStompClient.topic("/sujet/Arbitre").subscribe(topicMessage -> {
            //Log.d("STOMP", topicMessage.getPayload());
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String strTexte = jsonObj.getString("texte").toString();
            if(strTexte.equals("OK")){
                afficherMessageCommande("Combat généré. ("+strCombat+")");
            }else
                afficherMessageCommande(strTexte);

        });
    }

    public void subsribeExamen(String strExamen){
        mStompClient.topic("/sujet/Examen").subscribe(topicMessage -> {
            //Log.d("STOMP", topicMessage.getPayload());
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String strTexte = jsonObj.getString("texte").toString();
            if(strTexte.equals("OK")){
                afficherMessageCommande("Examen généré. ("+strExamen+")");
            }else
                afficherMessageCommande(strTexte);

        });
    }

    public void subsribeAncien(){
        mStompClient.topic("/sujet/Ancien").subscribe(topicMessage -> {
            //Log.d("STOMP", topicMessage.getPayload());
            JSONObject jsonObj = new JSONObject(topicMessage.getPayload());
            //Log.d("de", jsonObj.getString("de").toString());
            String strTexte = jsonObj.getString("texte").toString();
            if(strTexte.equals("OK")){
                afficherMessageCommande("L'utilisateur est maintenant devenu un ancien.");
            }else
                afficherMessageCommande(strTexte);

        });
    }

    public void btnCombat1_Click() {
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subscribeComabt("Rouge gagnant");
        mStompClient.send("/app/Combat",  "{\"de\":\""+username+"\",\"texte\":\"TRUE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }

    public void btnCombat2_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subscribeComabt("Blanc gagnant");
        mStompClient.send("/app/Combat",  "{\"de\":\""+username+"\",\"texte\":\"FALSE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }
    public void btnCombat3_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subscribeComabt("Match nul");
        mStompClient.send("/app/Combat",  "{\"de\":\""+username+"\",\"texte\":\"NULLE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }

    public void btnArbitre1_Click() {
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subsribeArbitre("Arbitre Sans Erreur");
        mStompClient.send("/app/Arbitre",  "{\"de\":\""+username+"\",\"texte\":\"TRUE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }

    public void btnArbitre2_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subsribeArbitre("Arbitre Erreur");
        mStompClient.send("/app/Arbitre",  "{\"de\":\""+username+"\",\"texte\":\"FAUTE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);

    }
    public void btnExamen1_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subsribeExamen("Examen réussi");
        mStompClient.send("/app/Examen",  "{\"de\":\""+username+"\",\"texte\":\"TRUE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }
    public void btnExamen2_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subsribeExamen("Examen échoué");
        mStompClient.send("/app/Examen",  "{\"de\":\""+username+"\",\"texte\":\"FALSE\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }

    public void btnAncien_Click(){
        // On assure la connection
        if(!mStompClient.isConnected()){
            mStompClient.reconnect();
        }
        t =  Long.toString(System.currentTimeMillis());
        subsribeAncien();
        mStompClient.send("/app/Ancien",  "{\"de\":\""+username+"\",\"texte\":\"ANCIEN\",\"creation\":" + t + ",\"id_avatar\":\""+username+"\"}").subscribe();
        infoCompte(true);
    }



}