package com.xiatstudio.mediclient;

import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/* 病哩病哩客户端——安卓部分
 * 该客户端会通过TCP/IP与已经部署好的病哩病哩服务器进行连接
 * 该客户端有两个功能：发送NFC标签字段向服务器索取病人信息
 * 以及将一套完整的病人信息发送至服务器
 * 
 * MainActivity为打开App时的主界面，将要求使用者输入服务器地址和标签ID
 * 此外还可以直接与NFC标签建立连接，自动填写ID
 * 
 * © 2019 XiatStudio & 英国约克大学电子工程系医疗工程研究实验室 & 江苏大学京江学院 & 上海市复旦大学医学院
 * 项目设计：顾瑾
 * 程序设计：Tian-Sebastian 'OMGCA' Xia
 * 医疗顾问：Ray 'SennaAndProst' Qian
 */

public class MainActivity extends BaseNfcActivity {
    /* 预设EXTRA字段，用于发送至DisplayPatientActivity */
    public static final String EXTRA_PATIENT = "com.xiatstudio.mediclient.PATIENT";
    public static final String EXTRA_SERVERADDR = "com.xiatstudio.mediclient.SERVERADDR";
    /* 预置空Patient类 */
    public static Patient retrievedPatient = new Patient("NULL");
    /* EditText类，用于显示来自NFC标签的数据 */
    private EditText patientID;
    /* mTagText用于存储NFC标签数据 */
    private String mTagText;

    private Button addButton;
    /* 查询按钮 */
    private Button queryButton;

    EditText serverAdd;
    View contextView;

    /*
     * Override onCreate方法 该方法在MainActivity启用时调用 作为程序的主入口
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 程序UI顶部纯色工具栏 */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addButton = findViewById(R.id.addButton);
        queryButton = findViewById(R.id.queryButton);
        serverAdd = findViewById(R.id.serverAddress);
        contextView = findViewById(R.id.myCoordinatorLayout);

        /* 设定UI中的"添加病人"按钮部件事件 */
        setAddPatientAction();

        /* 得到NFCID输入框部件 */
        patientID = findViewById(R.id.patientIDText);

        /* 检查NFC状态提示栏 */
        Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.nfcChecking, Snackbar.LENGTH_SHORT).show();
        displayNFCStatus();

        setQueryPatientAction();

    }

    /* 设定查询按钮事件 */
    public void setQueryPatientAction() {
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 得到文本区输入的服务器地址 */
                final String serverAddress = serverAdd.getText().toString();
                /* 得到文本区输入的病人ID */
                final String queryID = patientID.getText().toString();

                /* 打开与服务器的连接 */
                establishQueryCallable();

            }
        });
    }

    public void establishQueryCallable(){
        /* 初始化查询病人线程 */
        MainActivity threadRun = new MainActivity();

        /* 初始化Callable */
        ExecutorService pool = Executors.newFixedThreadPool(1);

        Callable c1 = threadRun.new QueryCallable(serverAdd.getText().toString(), patientID.getText().toString());
        /* 准备接收服务器发来的Patient类 */
        Future f1 = pool.submit(c1);

        Patient p;

        /* 接收从服务器发来的Patient类 */
        /* 若无法接收，则建立空Patient以防程序崩溃 */
        try {
            p = (Patient) f1.get();
            /* 发起新Activity,将Patient类发送给新Activity */
            displayPatient(contextView, p, serverAdd.getText().toString());
        }
        /* 服务器连接失败时或连接时间过长时弹出提示消息 */
        catch (ExecutionException e) {
            p = new Patient("NULL");
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.serverOut, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (InterruptedException e) {
            p = new Patient("NULL");
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.serverOut, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setAddPatientAction() {
        /* 设定添加按钮事件 */
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 由于是添加一个新的病人信息，所以使用一个空的Patient类传递到下一个Activity */
                Patient emptyPatient = new Patient(" ");
                /* 将首页输入或扫描到的ID赋予这个Patient类 */
                emptyPatient.setSlotID(patientID.getText().toString());
                /* 加载下一个Activity */
                displayPatient(view, emptyPatient, serverAdd.getText().toString());
            }
        });
    }

    public void displayNFCStatus() {
        /* 若该设备不支持NFC，则弹出提示消息 */
        if (!isNFCSupported()) {
            /* 初始化Snacbar提示栏(底部提示栏) */
            Snackbar snack = Snackbar.make(contextView, R.string.nfcWarning, Snackbar.LENGTH_LONG);
            /* 设定弹出消息颜色为黄色 */
            TextView tv = (TextView) snack.getView().findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.parseColor("#fcf653"));
            /* 弹出消息 */
            snack.show();
        }
    }

    /**
     * 此方法用来发起DisplayPatientActivity，来显示Patient信息
     * 
     * @param view,p,serverAddr
     * @return
     */
    public void displayPatient(View view, Patient p, String serverAddr) {
        Intent intent = new Intent(this, DisplayPatientActivity.class);

        /* 新建Bundle,使用Serializable给下一个Activity传送Patient类 */
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_PATIENT, p);
        intent.putExtras(bundle);

        /* 向Activity发送服务器地址，用于添加/修改病人 */
        intent.putExtra(EXTRA_SERVERADDR, serverAddr);
        /* 启动DisplayPatientActivity */
        startActivity(intent);
    }

    /* 检查设备是否支持NFC的方法 */
    public boolean isNFCSupported() {
        /*
         * 调取设备NFC模块信息 若设备不支持NFC，则返回null
         */
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null)
            return true;
        else
            return false;
    }

    /* 以下NFC相关代码摘自https://blog.csdn.net/qq_39582021/article/details/83069244 */
    @Override
    public void onNewIntent(Intent intent) {
        /* 获取Tag对象 */
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        /* 获取NDEF实例 */
        Ndef ndef = Ndef.get(detectedTag);

        if (ndef != null) {
            /* 得到NFC标签内容并赋予mTagText */
            mTagText = ndef.getType() + "\nmaxsize:" + ndef.getMaxSize() + "bytes\n\n";
            readNfcTag(intent);

            /* 自动填写NFC ID文本框 */
            patientID.setText(mTagText);
        } else {
            /* 若没有获得NDEF实例，弹出提示消息 */
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.ndefGetFail, Snackbar.LENGTH_SHORT).show();
        }
    }

    /* 读取NFC标签数据主要方法 */
    private void readNfcTag(Intent intent) {
        /* 当检测到NDEF实例时 */
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            /* 使用Parcelable储存元数据 */
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            /* 初始化一个NdefMessage对象 */
            NdefMessage msgs[] = null;

            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }

            try {
                if (msgs != null) {
                    NdefRecord record = msgs[0].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagText += textRecord + "\n\ntext\n" + contentSize + " bytes";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * 
     * @param ndefRecord
     * @return
     */
    public static String parseTextRecord(NdefRecord ndefRecord) {
        /* 判断数据是否为NDEF格式 */
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }

        /* 判断可变长度类型 */
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }

        try {
            /* 获取字节数组 */
            byte[] payload = ndefRecord.getPayload();

            /**
             * 下面开始NDEF文本数据第一个字节，状态字节
             * 判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1
             * 其他位都是0，所以进行"位与"运算后就会保留最高位
             */
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";

            /* 3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位 */
            int langCodeLength = payload[0] & 0x3f;

            /**
             * 下面开始NDEF文本数据第二个字节，语言编码 获得语言编码
             */
            String langCode = new String(payload, 1, langCodeLength, "US-ASCII");

            /* 下面开始NDEF文本数据后面的字节，解析出文本 */
            String textRecord = new String(payload, langCodeLength + 1, payload.length - langCodeLength - 1,
                    textEncoding);

            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    /* QueryCallable类，用于与服务器建立连接并返回得到的Patient类 */
    class QueryCallable implements Callable {
        private String serverAddress;
        private String patientID;
        private Patient patient;

        /* 初始化Callable */
        QueryCallable(String serverAddr, String id) {
            this.serverAddress = serverAddr;
            this.patientID = id;
        }

        @Override
        public Patient call() throws Exception {
            try {
                /* 与服务器建立连接 */
                Socket client = new Socket();
                /* 设定timeout为2000 ms */
                client.connect(new InetSocketAddress(this.serverAddress, 34167), 2000);

                /* 将查询ID发送至服务器 */
                OutputStream outStream = client.getOutputStream();
                client.getOutputStream().write(this.patientID.getBytes("UTF-8"));
                /* 关闭输出端口 */
                client.shutdownOutput();

                /* 接收从服务器发送的Patient类 */
                ObjectInputStream inStream = new ObjectInputStream(client.getInputStream());
                /* 将patient类赋予this，准备返回 */
                this.patient = (Patient) inStream.readObject();

                /* 关闭端口及连接 */
                inStream.close();
                outStream.close();
                client.close();

            } catch (Exception e) {
                /* 若连接失败则弹出消息 */
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.serverOut, Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return this.patient;
        }
    }

    /*
     * 查询病人线程， 由于Thread不能返回值，故使用Callable代替，该类并未被使用 若不需要返回Patient类则可以使用该类
     */
    @Deprecated
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

    /* 底下两个方法为自动生成，目前没有被使用 */
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

}
