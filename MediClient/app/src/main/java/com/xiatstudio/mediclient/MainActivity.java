package com.xiatstudio.mediclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.xiatstudio.mediclient.Patient;

public class MainActivity extends AppCompatActivity {

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
                new Thread(new ClientThread(serverAddress,queryID)).start();
            }
        });

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
    class ClientThread implements Runnable{
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
                Socket client = new Socket(serverAddress,34167);

                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(patientID.getBytes("UTF-8"));
                client.shutdownOutput();

                ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
                Patient p = (Patient) inStream.readObject();

                inStream.close();
                outStream.close();
                client.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
