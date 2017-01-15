package com.example.sibhali.livefeed;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static ImageView jIV;
    public static boolean frameChanged = false;
    public static Bitmap frame = null;
    public static Context context;
    public static EditText jIP;
    public static String servername;

    public Socket socket2;
    public int port2 = 6667;
    public OutputStream out2;
    private static Client client;
    private static boolean storing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        //SharedPreferences.Editor editor = sp.edit();
        String lastIP = sp.getString("Pref_IP", "");
        jIV = (ImageView) findViewById(R.id.xIV);
        jIP = (EditText) findViewById(R.id.xIP);
        jIP.setText(lastIP);

        ToggleButton xL = (ToggleButton) findViewById(R.id.xL);
        final Button xS = (Button) findViewById(R.id.xS);
        final Button xG = (Button) findViewById(R.id.xG);
        assert xL !=null;
        assert xS !=null;
        assert xG !=null;
        servername = jIP.getText().toString();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (frameChanged) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                jIV.setImageBitmap(frame);
                            }
                        });
                        frameChanged = false;
                    }
                }
            }
        });
        t2.start();

        xL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    client = new Client();
                    client.start();
                    xS.setVisibility(View.VISIBLE);
                } else {
                    client.liveFeed = false;
                    client = null;
                    if (storing) {
                        xG.setVisibility(View.GONE);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    socket2 = new Socket(servername,port2);
                                    out2 = socket2.getOutputStream();
                                    out2.write(2);
                                    out2.flush();
                                    socket2.close();
                                }catch (IOException e3){
                                    e3.printStackTrace();
                                }
                            }
                        }).start();

                        storing = false;
                    }
                    xS.setVisibility(View.GONE);
                }
            }
        });

        xS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xS.setVisibility(View.GONE);
                xG.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket2 = new Socket(servername, port2);
                            out2 = socket2.getOutputStream();
                            out2.write(1);
                            out2.flush();
                            socket2.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }).start();

                storing = true;
            }

        });

        xG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xG.setVisibility(View.GONE);
                xS.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            socket2 = new Socket(servername,port2);
                            out2 = socket2.getOutputStream();
                            out2.write(2);
                            out2.flush();
                            socket2.close();
                        }catch (IOException e3){
                            e3.printStackTrace();
                        }
                    }
                }).start();

                storing = false;
            }
        });

    }

    @Override
    protected void onStop() {
        SharedPreferences sp = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (jIP == null) editor.putString("Pref_IP", servername);
        else editor.putString("Pref_IP", jIP.getText().toString());
        editor.commit();

        //stop Live feed if on
        if (client != null){
            client.liveFeed = false;
            client = null;

            //stop storing if on
            if (storing) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            socket2 = new Socket(servername,port2);
                            out2 = socket2.getOutputStream();
                            out2.write(2);
                            out2.flush();
                            socket2.close();
                        }catch (IOException e3){
                            e3.printStackTrace();
                        }
                    }
                }).start();
                storing = false;
            }
        }

        super.onStop();
    }
}
