package dorespek.lenlroosterapp;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

public class roosterScreen extends ActionBarActivity {
    public String sourceResult;
    public boolean bool_loggedIn;
    public int int_stepperPoint = 0;
    public int int_dagSelected = 0;
    public Rooster ros_weekRooster;
    public Rooster ros_jaarRooster;
    public String str_weekRooster;
    public String str_jaarRooster;
    public String[] strar_weekRooster;
    public String[] strar_jaarRooster;
    public boolean bool_DataFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooster_screen);
        int_dagSelected = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if(int_dagSelected == -1 || int_dagSelected == 6){
            int_dagSelected = 0;
        }
    }
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        try {
            roosterStepper();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final WebView webBrowser = (WebView) findViewById(R.id.webBrowser);

        WebSettings webSettings = webBrowser.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadsImagesAutomatically(false);

        webBrowser.setWebViewClient(new WebViewClient() {
            boolean bool_Timeout;
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("source://")) {
                    try {
                        String html = URLDecoder.decode(url, "UTF-8").substring(9);
                        if(int_stepperPoint ==0) {
                            if(html.contains("<div id=\"roosterMobile\">") || html.contains("<div id=\"ingelogd\">")){
                                //already logged in
                                int_stepperPoint = 20;
                                roosterStepper();
                            } else if(html.contains("not available")){
                                //already logged in
                                int_stepperPoint = 45;
                                roosterStepper();
                            } else {
                                //not yet logged in
                                int_stepperPoint = 5;
                                roosterStepper();
                            }
                        } else if(int_stepperPoint ==20){
                            //Roosterpage presented
                            if(html.contains("<h3>Weekrooster</h3>")) {
                                sourceResult = html;
                                int_stepperPoint =30;
                                roosterStepper();
                            } else if (html.contains("U moet ingelogd zijn om deze pagina te bekijken.".toLowerCase())) {
                                int_stepperPoint =0;
                                roosterStepper();
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e("example", "failed to decode source", e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    return false;
                }
            }
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                bool_Timeout = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(bool_Timeout) {
                            int_stepperPoint =45;
                            try {
                                roosterStepper();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }
            public void onPageFinished(WebView view, String url) {
                bool_Timeout = false;
                if(int_stepperPoint ==0){
                    //www.lekenlinge.nl loaded
                    webBrowser.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                } else if(int_stepperPoint ==5) {
                    //We're now going to log in
                    int_stepperPoint = 10;
                    try {
                        roosterStepper();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(int_stepperPoint ==10){
                    //We're now logged in
                    int_stepperPoint =20;
                    try {
                        roosterStepper();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(int_stepperPoint ==20){
                    //Roosterpage retrieved
                    webBrowser.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                }
            }
        });
    }
    public void roosterStepper() throws IOException {
        WebView webBrowser = (WebView) findViewById(R.id.webBrowser);
        if(int_stepperPoint ==0) {
            //Not logged in, browsing to lekenlinge.nl
            //int_stepperPoint=45;
            Log.d("stepper", "Started");
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Roosterdata ophalen", Toast.LENGTH_SHORT).show();
                }
            });
            bool_loggedIn=false;
            webBrowser.loadUrl("https://www.lekenlinge.nl/uitloggen");
        }
        if(int_stepperPoint ==5){
            Log.d("stepper", "Again");
            webBrowser.loadUrl("https://www.lekenlinge.nl/mobiel/inloggen");
        }
        if(int_stepperPoint ==10) {
            //Logging in
            Log.d("stepper", "Logging in");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String username =  prefs.getString("lln", "100000");
            String password = prefs.getString("pwd", "Niks.");
            webBrowser.loadUrl("javascript:$(\"input#inlog-username-mobiel\").val(\"" + username + "\");");
            webBrowser.loadUrl("javascript:toonWachtwoordVeld();");
            webBrowser.loadUrl("javascript:$(\"input#inlog-wachtwoord-mobiel\").val(\"" + password + "\");");
            webBrowser.loadUrl("javascript:$(\"#inlogformulierMobiel\").submit();");
        }
        if(int_stepperPoint ==20) {
            //Logged in, going to roosterpage
            Log.d("stepper", "Logged in, going to roosterpage");
            bool_loggedIn =true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            //Log.d("test", prefs.getString("lln", "100000"));
            Random rand = new Random();
            webBrowser.loadUrl("https://www.lekenlinge.nl/mobiel/rooster?q=" + prefs.getString("lln", "100000") + "&rand=" + (Integer.toString(rand.nextInt(50) + 1)));
        }
        if(int_stepperPoint ==30) {
            //Roosterpage source presented
            Log.d("stepper", "Filtering roosterdata step 1");
            String str_tempData = sourceResult.replace('\'', '"');
            //String str_tempData = sourceResult;
            str_tempData = str_tempData.split("<h3>Weekrooster</h3>")[1];
            str_tempData = str_tempData.split("<div id=\"contentBottom\">")[0];
            String[] strar_tempData = str_tempData.split("<h3>Jaarrooster</h3>");
            str_weekRooster = strar_tempData[0];
            str_weekRooster = str_weekRooster.split("vr</td></tr>")[1];
            str_jaarRooster = strar_tempData[1];
            str_jaarRooster = str_jaarRooster.split("vr</td></tr>")[1];
            int_stepperPoint =40;
        }
        if(int_stepperPoint ==40){
            Log.d("stepper", "Writing data to file");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("weekrooster", str_weekRooster);
            editor.putString("jaarrooster", str_jaarRooster);
            editor.commit();
            int_stepperPoint=50;
        }
        if(int_stepperPoint ==45){
            Log.d("stepper", "Taking roosterdata from file");
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "WiFi traag, roosterdata van eerder geladen", Toast.LENGTH_SHORT).show();
                }
            });
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            str_weekRooster =  prefs.getString("weekrooster", "ZZZ zzz z001");
            if(str_weekRooster.equals("ZZZ zzz z001")){
                int_stepperPoint=70;
            } else {
                str_jaarRooster = prefs.getString("jaarrooster", "ZZZ zzz z001");
                int_stepperPoint = 50;
            }
        }
        if(int_stepperPoint==50){
            Log.d("stepper", "Filtering roosterdata step 2");
            strar_weekRooster = filterRooster(str_weekRooster);
            strar_jaarRooster = filterRooster(str_jaarRooster);
            int i=0;
            Log.d("rooster", Arrays.toString(strar_weekRooster));
            Log.d("rooster", Arrays.toString(strar_jaarRooster));

            ros_weekRooster = new Rooster("week");
            ros_weekRooster.setDagen(strar_weekRooster, strar_jaarRooster);

            int_stepperPoint=60;
        }
        if(int_stepperPoint==60){
            Log.d("stepper", "Roosterdata ready");
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Roosterdata geladen", Toast.LENGTH_SHORT).show();
                }
            });
            bool_DataFetched=true;
            roosterSet(findViewById(R.id.dag));
        }
        if(int_stepperPoint==70){
            Log.d("stepper", "Roosterdata empty");
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "Geen internetverbinding en geen data", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    public void roosterSet(View v){
        Log.d("stepper", "Roosterdata requested");
        if(bool_DataFetched) {
            switch(v.getResources().getResourceName(v.getId())){
                case "dorespek.lenlroosterapp:id/buttonMA": int_dagSelected=0; break;
                case "dorespek.lenlroosterapp:id/buttonDI": int_dagSelected=1; break;
                case "dorespek.lenlroosterapp:id/buttonWO": int_dagSelected=2; break;
                case "dorespek.lenlroosterapp:id/buttonDO": int_dagSelected=3; break;
                case "dorespek.lenlroosterapp:id/buttonVR": int_dagSelected=4; break;
                case "dorespek.lenlroosterapp:id/dag": break;
            }

            this.runOnUiThread(new Runnable() {
                public void run() {
                    TextView t;
                    t = (TextView) findViewById(R.id.dag);
                    Log.d("dag", Integer.toString(int_dagSelected));
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getDag());
                    t = (TextView) findViewById(R.id.les1);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(0).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(0).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#e2e2e2")); }
                    t = (TextView) findViewById(R.id.les2);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(1).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(1).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#a09f9f")); }
                    t = (TextView) findViewById(R.id.les3);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(2).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(2).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#e2e2e2")); }
                    t = (TextView) findViewById(R.id.les4);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(3).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(3).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#a09f9f")); }
                    t = (TextView) findViewById(R.id.les5);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(4).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(4).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#e2e2e2")); }
                    t = (TextView) findViewById(R.id.les6);
                    t.setText(ros_weekRooster.getDag(int_dagSelected).getUur(5).getText());
                    if(ros_weekRooster.getDag(int_dagSelected).getUur(5).getVeranderd()){ t.setBackgroundColor(Color.parseColor("#ffdcdc")); } else { t.setBackgroundColor(Color.parseColor("#a09f9f")); }
                }
            });
        }
    }
    public String[] filterRooster(String str_data){
        String[] strar_data = str_data.split("<tr>");
        String[] rooster = new String[30];
        int rC = 0;
        int i = 0;
        while(i < strar_data.length){
            String[] strar_temp = strar_data[i].split("<td style=\"padding: 1px\" class=\"");
            int j = 1;

            while(j < strar_temp.length) {
                strar_temp[j] = strar_temp[j].substring(5);
                strar_temp[j] = android.text.Html.fromHtml(strar_temp[j]).toString();
                if(!(strar_temp[j].length()==0)){
                    String[] strar_tempDag = strar_temp[j].split(" ");
                    if(strar_tempDag[2].length()==7){
                        strar_tempDag[2] = strar_tempDag[2].substring(0,4) + " " + strar_tempDag[2].substring(4);
                        strar_temp[j] = strar_tempDag[0] + " " + strar_tempDag[1] + " " + strar_tempDag[2] + " " + strar_tempDag[3] + " " + strar_tempDag[4];
                    }
                }
                rooster[rC] = strar_temp[j];
                rC++;
                j++;
            }
            i++;
        }

        return rooster;
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rooster_screen, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Context objContext;
            Intent ns = new Intent(this, SettingsActivity.class);
            startActivity(ns);
            return true;
        }
        if (id == R.id.action_refresh) {
            try {
                if(int_stepperPoint==60 || int_stepperPoint==70) {
                    int_stepperPoint = 0;
                    roosterStepper();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
