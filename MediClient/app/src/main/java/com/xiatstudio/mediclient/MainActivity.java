package com.xiatstudio.mediclient;

import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_PATIENT = "com.xiatstudio.mediclient.PATIENT";
    public static final String EXTRA_SERVERADDR = "com.xiatstudio.mediclient.SERVERADDR";
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

                /* 初始化Callable */
                ExecutorService pool = Executors.newFixedThreadPool(1);

                Callable c1 = threadRun.new QueryCallable(serverAddress, queryID);
                /* 准备接收服务器发来的Patient类 */
                Future f1 = pool.submit(c1);

                Patient p;

                /* 接收从服务器发来的Patient类 */
                /* 若无法接收，则建立空Patient以防程序崩溃 */
                try {
                    p = (Patient) f1.get();
                } catch (ExecutionException e) {
                    p = new Patient("NULL");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    p = new Patient("NULL");
                    e.printStackTrace();
                }

                /* 发起新Activity,将Patient类发送给新Activity */
                displayPatient(view, p, serverAddress);

            }
        });

    }


    /* 此方法用来发起DisplayPatientActivity，来显示Patient信息 */
    public void displayPatient(View view, Patient p, String serverAddr) {
        Intent intent = new Intent(this, DisplayPatientActivity.class);

        /* 新建Bundle,使用Serializable给下一个Activity传送Patient类 */
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_PATIENT,p);

        intent.putExtra(EXTRA_SERVERADDR,serverAddr);

        intent.putExtras(bundle);
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

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* 2019年3月3日凌晨更新：此方法用来代替先前使用的Thread */
    /* 同样为线程，Callable可定义返回值 */
    class QueryCallable implements Callable {
        private String serverAddress;
        private String patientID;
        private Patient patient;

        QueryCallable(String serverAddr, String id) {
            this.serverAddress = serverAddr;
            this.patientID = id;
        }

        @Override
        public Patient call() throws Exception {
            try {
                /* 与服务器建立连接 */
                Socket client = new Socket(this.serverAddress, 34167);

                /* 将查询ID发送至服务器 */
                /* NFC版中则是将NFC标签ID发送给服务器，此处代码应该不会有较大改动 */
                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(this.patientID.getBytes("UTF-8"));
                client.shutdownOutput();

                /* 接收从服务器发送的Patient类 */
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

    /* 查询病人线程，具体注释见服务器中的Client.java */
    /* 2019年3月3日凌晨注释：由于Thread不能返回值，故使用Callable代替，此方法在Android客户端中并未被使用 */
    class ClientThread extends Thread {
        private String serverAddress;
        private String patientID;

        /* 新增参数设置，用于与UI交互 */
        public ClientThread(String serverAddress, String patientID) {
            this.serverAddress = serverAddress;
            this.patientID = patientID;
        }

        @Override
        public void run() {
            try {
                Socket client = new Socket(this.serverAddress, 34167);

                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(this.patientID.getBytes("UTF-8"));
                client.shutdownOutput();

                ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
                retrievedPatient = (Patient) inStream.readObject();

                inStream.close();
                outStream.close();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
