package victimssimulation.pwr;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.String.valueOf;

public class StartMenu extends Activity {

    private TextView status;
    private Button btnConnect;
    private ListView listView;
    private TextView textEx;
    private Dialog dialog;
    private TextInputLayout inputLayout;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;
    private BluetoothAdapter bluetoothAdapter;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;



    TextView timer;
    private int seconds;
    private boolean running;
    private static final int FILE_CODE = 1;
    Uri uri = null;
    final String LOG_TAG = "myLogs";

    final String DIR_SD = "psDownload";
    final String FILENAME_SD = "dane.txt";

    private String filename = "dane.txt";
    private String filepath = "psDownload";
    File myExternalFile;
    String myData = "";

    List<String> beatArray = new ArrayList<String>();
    List<String> systolicArray = new ArrayList<String>();
    List<String> diastolicArray = new ArrayList<String>();
    List<String> breathArray = new ArrayList<String>();
    List<String> capArray = new ArrayList<String>();
    List<String> walkArray = new ArrayList<String>();
    List<String> answerArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        status = (TextView) findViewById(R.id.status);
        btnConnect = (Button) findViewById(R.id.connect);

        //check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        //show bluetooth devices dialog when click connect button
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrinterPickDialog();
            }
        });
        //set chat adapter
        chatMessages = new ArrayList<>();
        chatAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, chatMessages);
     //   listView.setAdapter(chatAdapter);
        //textt.setText(chatMessages.get(chatMessages.size()-1));

        runTimer();
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(true);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(true);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatMessages.add("Me: " + writeMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatMessages.add(connectingDevice.getName() + ":  " + readMessage);
                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });


    /// Metoda wybór urządzenia w dialogu
    public void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }


    private void setStatus(String s) {
        status.setText(s);
    }

    ////Metoda połączenia do innego urządzenia
    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
//// Metoda do wysyłania danych przez Bluetooth
    private void sendMessage(String message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }


    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };



    public void StartSim(View view) {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath()+"/psDownload/");
        if(dir.exists()) {
            File heartbeat = new File(dir, "heartBeat.txt");
            FileOutputStream os = null;
            ////////////////////// PUTTING DATA FROM TEXT FILES .txt /////////////////////
            StringBuilder heartText = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(heartbeat));
                String line;
                while ((line = br.readLine())!= null) {
                    heartText.append(line);
                    heartText.append('\n');

                    beatArray = Arrays.asList(line.trim().split(","));
                }
                br.close();
            } catch (IOException ignored) { }

            File systolic = new File(dir, "systolicPress.txt");
            StringBuilder systolicText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(systolic));
                String line;
                while ((line = br.readLine())!= null) {
                    systolicText.append(line);
                    systolicText.append('\n');
                    systolicArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            File diastolic = new File(dir, "diastolicPress.txt");
            StringBuilder diastolicText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(diastolic));
                String line;
                while ((line = br.readLine())!= null) {
                    diastolicText.append(line);
                    diastolicText.append('\n');
                    diastolicArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            File breathing = new File(dir, "breathing.txt");
            StringBuilder breatheText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(breathing));
                String line;
                while ((line = br.readLine())!= null) {
                    breatheText.append(line);
                    breatheText.append('\n');
                    breathArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            File capilarRefill = new File(dir, "capilarRefill.txt");
            StringBuilder capilarText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(capilarRefill));
                String line;
                while ((line = br.readLine())!= null) {
                    capilarText.append(line);
                    capilarText.append('\n');
                    capArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            File walking = new File(dir, "walking.txt");
            StringBuilder walkingText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(walking));
                String line;
                while ((line = br.readLine())!= null) {
                    walkingText.append(line);
                    walkingText.append('\n');
                    walkArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }

            File answer = new File(dir, "answer.txt");
            StringBuilder answerText = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(answer));
                String line;
                while ((line = br.readLine())!= null) {
                    answerText.append(line);
                    answerText.append('\n');
                    answerArray = Arrays.asList(line.trim().split(","));

                }
                br.close();
            } catch (IOException e) {
                //You'll need to add proper error handling here
            }


            ////////////////////// PUTTING DATA FROM TEXT FILES .txt /////////////////////

        running = true;
    }
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
        final TextView breathing = findViewById(R.id.breath);
        final TextView capilarText = findViewById(R.id.capilar);
        final TextView walkingT = findViewById(R.id.walking);
        final TextView answer = findViewById(R.id.answer);


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

                if(beatArray.size()==seconds)
                {
                    running=false;
                    seconds=0;
                }else {
                   // new EddystoneURL(valueOf(beatArray.get(seconds))).start();
                    bts.setText(valueOf(beatArray.get(seconds))+" bpm");
                    systolic.setText(valueOf(systolicArray.get(seconds)));
                    diastolic.setText(valueOf(diastolicArray.get(seconds)));
                    breathing.setText(valueOf(breathArray.get(seconds)));
                    capilarText.setText(valueOf(capArray.get(seconds)));
                    //answer.setText(valueOf(answerArray.get(seconds)));
                    //walkingT.setText(valueOf(walkArray.get(seconds)));


                    if(walkArray.get(seconds).equals("1")) {
                        walkingT.setText("TAK");
                    }else
                        walkingT.setText("NIE");

                    if(answerArray.get(seconds).equals("1")) {
                        answer.setText("TAK");
                    }else
                        answer.setText("NIE");

                    sendMessage(valueOf(beatArray.get(seconds)+","+systolicArray.get(seconds)+","
                            +diastolicArray.get(seconds)+","+breathArray.get(seconds)+","
                            +capArray.get(seconds)+","+walkArray.get(seconds)+","+answerArray.get(seconds)));

                }
            }
        });
    }

    public void OpenSim(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, FILE_CODE);
    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
//            // The document selected by the user won't be returned in the intent.
//            // Instead, a URI to that document will be contained in the return intent
//            // provided to this method as a parameter.
//            // Pull that URI using resultData.getData().
//            uri = null;
//            if (resultData != null) {
//                uri = resultData.getData();
//                openPath(uri);
//            }
//        }
//    }
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

