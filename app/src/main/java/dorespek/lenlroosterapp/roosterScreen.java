package dorespek.lenlroosterapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class roosterScreen extends ActionBarActivity {
    public String sourceResult;
    public boolean bool_loggedIn;
    public static int int_stepperPoint = 0;
    public Rooster ros_weekRooster;
    public Rooster ros_jaarRooster;
    public String str_weekRooster;
    public String str_jaarRooster;
    public String[] strar_weekRooster;
    public String[] strar_jaarRooster;
    private boolean bool_DataFetched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooster_screen);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if((prefs.getString("lln", "100000").equals("100000")) && (prefs.getString("pwd", "Niks.").equals("Niks."))){
            Intent ns = new Intent(this, SettingsActivity.class);
            startActivity(ns);
        }
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
                        if(int_stepperPoint ==0) {
                            if(html.contains("<div id=\"roosterMobile\">") || html.contains("<div id=\"ingelogd\">")){
                                //already logged in
                                int_stepperPoint = 20;
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //timeout=true;
                        android.os.SystemClock.sleep(10000);
                        if(timeout) {
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
                timeout=false;
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
            bool_loggedIn =false;
            webBrowser.loadUrl("https://www.lekenlinge.nl");
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
            webBrowser.loadUrl("https://www.lekenlinge.nl/mobiel/rooster?q=" + prefs.getString("lln", "100000"));
        }
        if(int_stepperPoint ==30) {
            //Roosterpage source presented
            Log.d("stepper", "Filtering roosterdata");
            String str_tempData = sourceResult.replace('\'', '"');
            str_tempData = str_tempData.split("<h3>Weekrooster</h3>")[1];
            String[] strar_tempData = str_tempData.split("<h3>Jaarrooster</h3>");
            str_weekRooster = strar_tempData[0];
            str_jaarRooster = strar_tempData[1];
            str_jaarRooster = str_jaarRooster.split("<br/>Directe link: <a href='")[0];
            str_jaarRooster = str_jaarRooster.split("<p></p></div><div id=\"lestijden\"")[0];
            strar_weekRooster = filterRooster(str_weekRooster);
            strar_jaarRooster = filterRooster(str_jaarRooster);
            int i = 0;
            while(i< strar_jaarRooster.length){
                Log.d(String.valueOf(i), strar_weekRooster[i]);
                i++;
            }
            ros_weekRooster = new Rooster("week");
            ros_weekRooster.setDagen(strar_weekRooster, strar_jaarRooster);
            ros_jaarRooster = new Rooster("jaar");
            ros_jaarRooster.setDagen(strar_jaarRooster, strar_weekRooster);
            int_stepperPoint =40;
        }
        if(int_stepperPoint ==40){
            Log.d("stepper", "Writing data to file");
            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("weekrooster.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(str_weekRooster);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

            try {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("jaarrooster.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(str_jaarRooster);
                outputStreamWriter.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
            int_stepperPoint=50;
        }
        if(int_stepperPoint ==45){
            Log.d("stepper", "Taking roosterdata from file");
            str_weekRooster = "";
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
                    str_weekRooster = stringBuilder.toString();
                }
            }
            catch (FileNotFoundException e) {
                Log.e("Roosterfile", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("Roosterfile", "Can not read file: " + e.toString());
            }

            str_jaarRooster = "";
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
                    str_jaarRooster = stringBuilder.toString();
                }
            }
            catch (FileNotFoundException e) {
                Log.e("Roosterfile", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("Roosterfile", "Can not read file: " + e.toString());
            }
        }
        if(int_stepperPoint==50){
            Log.d("stepper", "Roosterdata ready");
            bool_DataFetched=true;
        }
    }
    public void roosterSet(View v){
        Log.d("stepper", "Roosterdata requested");
        if(bool_DataFetched) {
            int t_dag;
            t_dag = 0;
            switch(v.getResources().getResourceName(v.getId())){
                case "dorespek.lenlroosterapp:id/buttonMA": t_dag=0; break;
                case "dorespek.lenlroosterapp:id/buttonDI": t_dag=1; break;
                case "dorespek.lenlroosterapp:id/buttonWO": t_dag=2; break;
                case "dorespek.lenlroosterapp:id/buttonDO": t_dag=3; break;
                case "dorespek.lenlroosterapp:id/buttonVR": t_dag=4; break;
            }

            TextView t;
            t = (TextView) findViewById(R.id.dag);
            t.setText(ros_weekRooster.getDag(t_dag).getDag());
            t = (TextView) findViewById(R.id.les1);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(0).getUur());
            t = (TextView) findViewById(R.id.les2);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(1).getUur());
            t = (TextView) findViewById(R.id.les3);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(2).getUur());
            t = (TextView) findViewById(R.id.les4);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(3).getUur());
            t = (TextView) findViewById(R.id.les5);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(4).getUur());
            t = (TextView) findViewById(R.id.les6);
            t.setText(ros_weekRooster.getDag(t_dag).getUur(5).getUur());
        }
    }
    public String[] filterRooster(String str_data){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        str_data = str_data.replace("<table id=\"w" + prefs.getString("lln", "100000") + "\" class=\"roostertabel week\" cols=\"6\" cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%\"><tbody><tr><td class=\"headercell\"></td>", "");
        str_data = str_data.replace("<table id=\"j" + prefs.getString("lln", "100000") + "\" class=\"roostertabel week\" cols=\"6\" cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%\"><tbody><tr><td class=\"headercell\"></td>", "");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">1</td>","");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">2</td>","");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">3</td>","");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">4</td>","");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">5</td>","");
        str_data = str_data.replace("<td class=\"headercell\" style=\"text-align: center; \">6</td>","");
        str_data = str_data.replace("<table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"></table>", "<table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr style=\"border: 0px; padding: 0px\"><td style=\"width: 100%;text-align: center;border: 0px; padding: 0px 1px 0px 0px\"><a href=\"https://www.lekenlinge.nl/mobiel/rooster?q=ZZZ\">ZZZ</a> zzz <a href=\"https://www.lekenlinge.nl/mobiel/rooster?q=Z001\">Z001</a></td></tr></tbody></table>");
        String[] strar_data = str_data.split("Directe link:");
        str_data = strar_data[0];
        str_data = android.text.Html.fromHtml(str_data).toString();
        str_data = str_data.replace("madiwodovr", "");



        //data = data.replace("<td style=\"padding: 1px\"><table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"></table></td>", "<td style=\"padding: 1px\"><table cols=\"3\" style=\"font-size: .75em; border: 0px; width: 100%\" cellspacing=\"0\" cellpadding=\"0\"><tbody><tr style=\"border: 0px; padding: 0px\"><td style=\"width: 100%;text-align: center;border: 0px; padding: 0px 1px 0px 0px\"><a href=\"https://www.lekenlinge.nl/mijn/roosters/rooster_zoeken.php?q=ZZZ\">ZZZ</a> zetl_A <a href=\"https://www.lekenlinge.nl/mijn/roosters/rooster_zoeken.php?q=Z001\">Z001</a></td></tr></tbody></table></td>");
        String[] sortedData = str_data.split(" ");
        String[] dataDone = new String[90];
        int i = 0;
        int j = 0;
        while(i < sortedData.length){
            str_data = android.text.Html.fromHtml(sortedData[i]).toString();
            int dataL = str_data.length();
            //Log.d("stepper", String.valueOf(dataL));
            if(dataL==7) {
                dataDone[j] = str_data.substring(0, 4);
                //Log.d("stepperdata", dataDone[j]);
                j++;
                dataDone[j] = str_data.substring(4);
                //Log.d("stepperdata", dataDone[j]);
                j++;
                i++;
            } else {
                dataDone[j] = str_data;
                //Log.d("stepperdata", dataDone[j]);
                j++;
                i++;
            }
        }

        return dataDone;
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
        return super.onOptionsItemSelected(item);
    }
}
