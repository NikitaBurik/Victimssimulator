package victimssimulation.pwr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import static android.widget.Toast.LENGTH_LONG;
import static java.lang.String.valueOf;

public class StartMenu extends Activity {

    TextView timer;
    private int seconds;
    private boolean running;
    private static final int FILE_CODE = 1;
    Uri uri = null;
    final String LOG_TAG = "myLogs";



    int beats [] = {81,80,79,75,73,72,70,71,69,68};
    int sys [] = {121,120,120,118,119,117,115,111,104,100};
    int dia [] = {80,82,84,89,94,85,81,78,73,70};
    List<String> textRead = new ArrayList<String >();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        runTimer();
    }

    public void StartSim(View view) {

        if (uri == null) {
            Toast.makeText(this,"OPEN THE FILE",LENGTH_LONG).show();
        } else {
            InputStream is = null;
            try {

                // открываем поток для чтения
                is = getContentResolver().openInputStream(uri);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                // List<String> readText = new ArrayList<>();

                String str = "";
                // читаем содержимое
                while ((str = br.readLine()) != null) {
                    Log.d(LOG_TAG, str);
                    //  Toast.makeText(getBaseContext(), str,Toast.LENGTH_SHORT).show();
                    //txt.setText(str);
                    textRead = Arrays.asList(str.trim().split(","));

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(this,valueOf(textRead.size()),LENGTH_LONG).show();

        running = true;

    }
    public void StopSim(View view) {
    running=false;
    }


    public void ResetSim(View view) {
        running=false;
        seconds=0;
    }

    private void runTimer(){
        final TextView tmr = findViewById(R.id.time);
        final TextView bts = findViewById(R.id.beat);
        final TextView systolic = findViewById(R.id.sysstolic);
        final TextView diastolic = findViewById(R.id.diastolic);


        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int secon = seconds;

                String time = String.format("%d",secon);
                tmr.setText(time);
                if(running){
                    seconds++;
                }
                handler.postDelayed(this,1000);

                if(textRead.size()==seconds)
                {
                    running=false;
                    seconds=0;
                }else {
                    bts.setText(valueOf(textRead.get(seconds))+" bpm");
                }

//                if(sys.length==seconds && dia.length==seconds){
//                    running=false;
//                    seconds=0;
//                }
//                else{
//                    systolic.setText(valueOf(sys[seconds]));
//                    diastolic.setText(valueOf(dia[seconds]));
//
//                }

            }
        });
    }

    public void OpenSim(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, FILE_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                openPath(uri);
            }
        }
    }
    public void openPath(Uri uri) {
        InputStream is = null;
        String str = "";
        StringBuffer buf = new StringBuffer();
        try {
            is = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n");

                }

                //Convert your stream to data here
                is.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Toast.makeText(this,buf,LENGTH_LONG).show();


//        showText.setText(buf);
//        textL = buf;

    }


}

