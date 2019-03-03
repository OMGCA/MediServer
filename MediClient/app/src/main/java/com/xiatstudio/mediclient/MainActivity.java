package com.xiatstudio.mediclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.xiatstudio.mediclient.MESSAGE";
    public static Patient retrievedPatient = new Patient("NULL");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

        /* 从UI中得到文本区部件 */
        final EditText patientID = findViewById(R.id.patientIDText);
        final EditText serverAdd = findViewById(R.id.serverAddress);

        /* 查询按钮 */
        Button queryButton = findViewById(R.id.queryButton);
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String queryID = patientID.getText().toString(); /* 得到文本区输入的病人ID */
                final String serverAddress = serverAdd.getText().toString(); /* 得到文本区输入的服务器地址 */

                MainActivity threadRun = new MainActivity();

                ExecutorService pool = Executors.newFixedThreadPool(1);

                Callable c1 = threadRun.new QueryCallable(serverAddress,queryID);

                Future f1 = pool.submit(c1);
                //MainActivity.ClientThread clientThread = threadRun.new ClientThread(serverAddress,queryID);
                //clientThread.start();

                /*Patient patientFromServer = new Patient("NULL");

                try {
                    patientFromServer = clientThread.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                Patient p;

                try {
                    p = (Patient) f1.get();
                } catch (ExecutionException e) {
                    p = new Patient("NULL");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    p = new Patient("NULL");
                    e.printStackTrace();
                }
                displayPatient(view,p);

            }
        });

    }

    public void displayPatient(View view, Patient p){
        Intent intent = new Intent(this, DisplayPatientActivity.class);
        String patientName = p.getName();

        intent.putExtra(EXTRA_MESSAGE,patientName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 查询病人线程，具体注释见服务器中的Client.java */
    class ClientThread extends Thread {
        private String serverAddress;
        private String patientID;

        /* 新增参数设置，用于与UI交互 */
        public ClientThread(String serverAddress, String patientID) {
            this.serverAddress = serverAddress;
            this.patientID = patientID;
        }

        @Override
        public void run(){
            try{
                Socket client = new Socket(this.serverAddress,34167);

                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(this.patientID.getBytes("UTF-8"));
                client.shutdownOutput();

                ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
                retrievedPatient = (Patient) inStream.readObject();

                inStream.close();
                outStream.close();
                client.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    class QueryCallable implements Callable {
        private String serverAddress;
        private String patientID;
        private Patient patient;

        QueryCallable(String serverAddr, String id){
            this.serverAddress = serverAddr;
            this.patientID = id;
        }

        @Override
        public Patient call() throws Exception {
            try {
                Socket client = new Socket(this.serverAddress, 34167);

                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(this.patientID.getBytes("UTF-8"));
                client.shutdownOutput();

                ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
                this.patient = (Patient) inStream.readObject();

                inStream.close();
                outStream.close();
                client.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            return this.patient;
        }
    }

}
