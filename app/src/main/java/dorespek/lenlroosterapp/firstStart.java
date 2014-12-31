package dorespek.lenlroosterapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via email/password.
 */
public class firstStart extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!(prefs.getString("lln", "100000").equals("100000")) && !(prefs.getString("pwd", "Niks.").equals("Niks."))){
            Intent ns = new Intent(this, roosterScreen.class);
            startActivity(ns);
        }
    }

    public void saveData(View v) {
        EditText lln = (EditText) findViewById(R.id.lln);
        EditText pwd = (EditText) findViewById(R.id.wachtwoord);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lln", lln.getText().toString());
        editor.putString("pwd", pwd.getText().toString());
        editor.commit();

        Intent ns = new Intent(this, roosterScreen.class);
        startActivity(ns);
    }
}



