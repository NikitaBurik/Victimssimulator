package victimssimulation.pwr;

import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;

import static java.lang.String.valueOf;

public class StartMenu extends Activity {

    TextView timer;
    private int seconds;
    private boolean running;

    int beats [] = {81,80,79,75,73,72,70,71,69,68};
    int sys [] = {121,120,120,118,119,117,115,111,104,100};
    int dia [] = {80,82,84,89,94,85,81,78,73,70};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        runTimer();
    }

    public void StartSim(View view) {
    running=true;
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

                if(beats.length==seconds)
                {
                    running=false;
                    seconds=0;
                }else {
                    bts.setText(valueOf(beats[seconds])+" bpm");
                }

                if(sys.length==seconds && dia.length==seconds){
                    running=false;
                    seconds=0;
                }
                else{
                    systolic.setText(valueOf(sys[seconds]));
                    diastolic.setText(valueOf(dia[seconds]));

                }

            }
        });
    }
}

