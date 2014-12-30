package dorespek.lenlroosterapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.io.IOException;


public class roosterScreen extends ActionBarActivity {
    public String sourceResult;
    public String temp_weekrooster;
    public String temp_jaarrooster;
    public boolean loggedIn;
    public static int stepperPoint = 0;
    public Rooster weekRooster;
    public Rooster jaarRooster;
    public int temp;
    public String t_wR;
    public String t_jR;
    public String[] wR;
    public String[] jR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooster_screen);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
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

        webBrowser.setWebViewClient(new WebViewClient() {
            boolean timeout=false;
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("source://")) {
                    try {
                        String html = URLDecoder.decode(url, "UTF-8").substring(9);
                        if(stepperPoint==0) {
                            if(html.contains("<div id=\"roosterMobile\">") || html.contains("<div id=\"ingelogd\">")){
                                //already logged in
                                stepperPoint = 20;
                                roosterStepper();
                            } else {
                                //not yet logged in
                                stepperPoint = 5;
                                roosterStepper();
                            }
                        } else if(stepperPoint==20){
                            //Roosterpage presented
                            if(html.contains("<h3>Weekrooster</h3>")) {
                                sourceResult = html;
                                stepperPoint=30;
                                roosterStepper();
                            } else if (html.contains("U moet ingelogd zijn om deze pagina te bekijken.".toLowerCase())) {
                                stepperPoint=0;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //timeout=true;
                        android.os.SystemClock.sleep(10000);
                        if(timeout) {
                            stepperPoint=45;
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
                timeout=false;
                if(stepperPoint==0){
                    //www.lekenlinge.nl loaded
                    webBrowser.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                } else if(stepperPoint==5) {
                    //We're now going to log in
                    stepperPoint = 10;
                    try {
                        roosterStepper();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(stepperPoint==10){
                    //We're now logged in
                    stepperPoint=20;
                    try {
                        roosterStepper();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if(stepperPoint==20){
                    //Roosterpage retrieved
                    webBrowser.loadUrl("javascript:this.document.location.href = 'source://' + encodeURI(document.documentElement.outerHTML);");
                }
            }
        });

    }
    public void roosterStepper() throws IOException {
        WebView webBrowser = (WebView) findViewById(R.id.webBrowser);
        if(stepperPoint==0) {
            //Not logged in, browsing to lekenlinge.nl
            //stepperPoint=45;
            Log.d("stepper", "Started");
            loggedIn=false;
            webBrowser.loadUrl("https://www.lekenlinge.nl");
        }
        if(stepperPoint==5){
            Log.d("stepper", "Again");
            webBrowser.loadUrl("https://www.lekenlinge.nl/mobiel/inloggen");
        }
        if(stepperPoint==10) {
            //Logging in
            Log.d("stepper", "Logging in");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d("stepper", prefs.getString("lln", "114684"));
            Log.d("stepper", prefs.getString("pwd", "Bufitiranik."));
            String username =  prefs.getString("lln", "114684");
            String password = prefs.getString("pwd", "Bufitiranik.");
            webBrowser.loadUrl("javascript:$(\"input#inlog-username-mobiel\").val(\"" + username + "\");");
            webBrowser.loadUrl("javascript:toonWachtwoordVeld();");
            webBrowser.loadUrl("javascript:$(\"input#inlog-wachtwoord-mobiel\").val(\"" + password + "\");");
            webBrowser.loadUrl("javascript:$(\"#inlogformulierMobiel\").submit();");
        }
        if(stepperPoint==20) {
            //Logged in, going to roosterpage
            Log.d("stepper", "Logged in, going to roosterpage");
            loggedIn=true;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            webBrowser.loadUrl("https://www.lekenlinge.nl/mobiel/rooster?q=" + prefs.getString("lln", "114684"));
        }
        if(stepperPoint==30) {
            //Roosterpage source presented
            Log.d("stepper", "Filtering roosterdata");
            String tempData = sourceResult.replace('\'', '"');
            tempData = tempData.split("<h3>Weekrooster</h3>")[1];
            String[] tempDataa = tempData.split("<h3>Jaarrooster</h3>");
            temp_weekrooster = tempDataa[0];
            temp_jaarrooster = tempDataa[1];
            temp_jaarrooster = temp_jaarrooster.split("<br/>Directe link: <a href='")[0];
            temp_jaarrooster = temp_jaarrooster.split("<p></p></div><div id=\"lestijden\"")[0];
            t_wR=temp_weekrooster;
            t_jR=temp_jaarrooster;
            stepperPoint=40;
        }
        if(stepperPoint==40){
            Log.d("stepper", "Going to roosterscreen and writing data to file");
            Intent inent = new Intent(this, roosterScreen.class);

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("weekrooster.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(temp_weekrooster);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("jaarrooster.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(temp_jaarrooster);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

            inent.putExtra("wR", temp_weekrooster);
            inent.putExtra("jR", temp_jaarrooster);
            startActivity(inent);
        }
        if(stepperPoint==45){
            Log.d("stepper", "Taking roosterdata from file and going to roosterscreen");
            String tempW = "";
            try {
                InputStream inputStream = openFileInput("weekrooster.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    tempW = stringBuilder.toString();
                }
            }
            catch (FileNotFoundException e) {
                Log.e("Roosterfile", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("Roosterfile", "Can not read file: " + e.toString());
            }

            String tempJ = "";
            try {
                InputStream inputStream = openFileInput("weekrooster.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        stringBuilder.append(receiveString);
                    }

                    inputStream.close();
                    tempJ = stringBuilder.toString();
                }
            }
            catch (FileNotFoundException e) {
                Log.e("Roosterfile", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("Roosterfile", "Can not read file: " + e.toString());
            }
            t_wR=tempW;
            t_jR=tempJ;

        }
        if(stepperPoint==50){
            //Roosterdata naar scherm laden
            Log.d("stepper", "Upping roosterdata to screen");
            wR = filterRooster(t_wR);
            jR = filterRooster(t_jR);
            int i = 0;
            while(i<jR.length){
                Log.d(String.valueOf(i), wR[i]);
                i++;
            }
            weekRooster = new Rooster("week");
            weekRooster.setDagen(wR, jR);
            jaarRooster = new Rooster("jaar");
            jaarRooster.setDagen(jR, wR);



        }
    }
    public void roosterSet(View v){
        int t_dag;
        t_dag=0;
        switch(v.getResources().getResourceName(v.getId())){
            case "buttonMA": t_dag=0;
            case "buttonDI": t_dag=1;
            case "buttonWO": t_dag=2;
            case "buttonDO": t_dag=3;
            case "buttonVR": t_dag=4;
        }

        TextView t;
        t = (TextView) findViewById(R.id.les1);
        t.setText(weekRooster.getDag(t_dag).getUur(0).getUur());
        t = (TextView) findViewById(R.id.les2);
        t.setText(weekRooster.getDag(t_dag).getUur(1).getUur());
        t = (TextView) findViewById(R.id.les3);
        t.setText(weekRooster.getDag(t_dag).getUur(2).getUur());
        t = (TextView) findViewById(R.id.les4);
        t.setText(weekRooster.getDag(t_dag).getUur(3).getUur());
        t = (TextView) findViewById(R.id.les5);
        t.setText(weekRooster.getDag(t_dag).getUur(4).getUur());
        t = (TextView) findViewById(R.id.les6);
        t.setText(weekRooster.getDag(t_dag).getUur(5).getUur());
    }


    public String[] filterRooster(String data){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        data = data.replace("<table id=\"w" + prefs.getString("lln", "114684") + "\" class=\"roostertabel week\" cols=\"6\" cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%\"><tbody><tr><td class=\"headercell\"></td>", "");
        data = data.replace("<table id=\"j" + prefs.getString("lln", "114684") + "\" class=\"roostertabel week\" cols=\"6\" cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%\"><tbody><tr><td class=\"headercell\"></td>", "");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">1</td>","");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">2</td>","");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">3</td>","");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">4</td>","");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">5</td>","");
        data = data.replace("<td class=\"headercell\" style=\"text-align: center; \">6</td>","");
        data = data.replace("<table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"></table>", "<table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr style=\"border: 0px; padding: 0px\"><td style=\"width: 100%;text-align: center;border: 0px; padding: 0px 1px 0px 0px\"><a href=\"https://www.lekenlinge.nl/mobiel/rooster?q=ZZZ\">ZZZ</a> zzz <a href=\"https://www.lekenlinge.nl/mobiel/rooster?q=Z001\">Z001</a></td></tr></tbody></table>");
        String[] dataa = data.split("Directe link:");
        data = dataa[0];
        data = android.text.Html.fromHtml(data).toString();
        data = data.replace("madiwodovr", "");



        //data = data.replace("<td style=\"padding: 1px\"><table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"></table></td>", "<td style=\"padding: 1px\"><table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr style=\"border: 0px; padding: 0px\"><td style=\"width: 100%;text-align: center;border: 0px; padding: 0px 1px 0px 0px\"><a href=\"https://www.lekenlinge.nl/mijn/roosters/rooster_zoeken.php?q=ZZZ\">ZZZ</a> zetl_A <a href=\"https://www.lekenlinge.nl/mijn/roosters/rooster_zoeken.php?q=Z001\">Z001</a></td></tr></tbody></table></td>");
        String[] sortedData = data.split(" ");
        String[] dataDone = new String[90];
        int i = 0;
        int j = 0;
        while(i < sortedData.length){
            data = android.text.Html.fromHtml(sortedData[i]).toString();
            int dataL = data.length();
            //Log.d("stepper", String.valueOf(dataL));
            if(dataL==7) {
                dataDone[j] = data.substring(0, 4);
                //Log.d("stepperdata", dataDone[j]);
                j++;
                dataDone[j] = data.substring(4);
                //Log.d("stepperdata", dataDone[j]);
                j++;
                i++;
            } else {
                dataDone[j] = data;
                //Log.d("stepperdata", dataDone[j]);
                j++;
                i++;
            }
        }

        return dataDone;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rooster_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
