package com.example.temp_test;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class MyActivity extends Activity implements View.OnClickListener {
    /**
     * Called when the activity is first created.
     */


    private static final String TAG = "tst";

    MyImageParser parser;


    void ParseResourse(int res)
    {
        /**
         * Now we should decode png file to wave and play it
         *
         * */

        parser =  new MyImageParser(getResources(), res, this);
        parser.start();

    }


    void SavePrefs(Bundle bundle)
    {
        SharedPreferences mPrefs = getSharedPreferences("FormParams", MODE_PRIVATE);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("cntClicked", cntClicked);
        ed.commit();
    }

    void LoadPrefs(Bundle savedInstanceState)
    {
        SharedPreferences mPrefs = getSharedPreferences("FormParams", MODE_PRIVATE);
        cntClicked = mPrefs.getInt("cntClicked", 0);
    }

    CheckBox chBox1;
    Button btnFirst, btnF06, btnF07;

    Integer cntClicked = 0;
    Toast toast1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        toast1 = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);

        btnFirst = (Button)findViewById(R.id.first);
        btnF06 = (Button)findViewById(R.id.f06);
        btnF07 = (Button)findViewById(R.id.f07);



        btnFirst.setOnClickListener(this);
        btnF06.setOnClickListener(this);
        btnF07.setOnClickListener(this);


        LoadPrefs(savedInstanceState);


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        SavePrefs(outState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.first:
                ParseResourse(R.drawable.first);
                break;
            case R.id.f06:
                ParseResourse(R.drawable.f06);
                break;
            case R.id.f07:
                ParseResourse(R.drawable.f07);
                break;
            default:
                Log.e(TAG, "Unknown element clicked");
                break;

        }
    }
}
