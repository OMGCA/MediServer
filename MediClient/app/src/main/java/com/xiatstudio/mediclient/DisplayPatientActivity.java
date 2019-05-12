package com.xiatstudio.mediclient;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;

import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/*
 * DisplayPatientActivity定义
 * 该Activity会显示病人信息，并可以进行编辑和上传
 */
public class DisplayPatientActivity extends AppCompatActivity {
    /* 三组用来获取UI的EditText部件的ArrayList */
    private List<EditText> demograEtGrp = new ArrayList<EditText>();
    private List<EditText> pathologyEtGrp=  new ArrayList<EditText>();
    private List<EditText> noteEtGrp = new ArrayList<EditText>();

    /* 定义常量，个人信息7个，病理数据8个，备注5个 */
    protected int demograSize = 7;
    protected int patholoSize = 8;
    protected int noteSize = 5;

    /* 用于存储findViewById返回的int型 */
    private int[] demograEditText = new int[demograSize];
    private int[] pathologyEditText = new int[patholoSize];
    private int[] noteEditText = new int[noteSize];

    /* 病人信息转String */
    private String[] pDemogra = new String[demograSize];
    private String[] pPathology = new String[patholoSize];
    private String[] pNote = new String[noteSize];

    /* 准备发送至服务器的Patient类 */
    private Patient patientToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* 新建Intent */
        final Intent intent = getIntent();

        /* 接收从MainActivity中传输的Patient类 */
        Bundle bundle = intent.getExtras();
        final Patient patient = (Patient) bundle.getSerializable(MainActivity.EXTRA_PATIENT);

        /* 将Patient里的属性数据转String保存在local变量中 */
        setPatientValue(patient);

        /* 定义UI部件变量名 */
        defineEditTextComponents();

        /* 链接UI和代码中的EditText类 */
        registerEditText();

        isLockEdit(true);

        editPatientButtonAction(intent);
        
    }

    public void editPatientButtonAction(final Intent intent){
        /* 设置编辑按钮行为 */
        FloatingActionButton fab = findViewById(R.id.fab);

        /* 赋予动画效果 */
        setFABAnimation(fab, R.anim.shake);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                /* 解锁文本框编辑 */
                isLockEdit(false);

                /* 初始化发送病人按钮 */
                sendPatientButtonAction(intent);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public void sendPatientButtonAction(final Intent intent){
        /* 设定发送病人信息按钮行为 */
        FloatingActionButton fab2 = findViewById(R.id.sendPatient);

        /* 使按钮可见 */
        fab2.setVisibility(View.VISIBLE);

        /* 设定动画效果 */
        setFABAnimation(fab2, R.anim.shake);

        /* 设定事件 */
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 将UI中填写好的患者信息更新至本地Patient类 */
                updatedPatientFromUI();

                /* 链接并发送至服务器 */
                new SendPatientAsyncTask(DisplayPatientActivity.this,
                        intent.getStringExtra(MainActivity.EXTRA_SERVERADDR), patientToSend).execute();

            }
        });
    }

    /* 设定FloatingActionButton动画 */
    public void setFABAnimation(FloatingActionButton fab, int animation){
        Animation anim = android.view.animation.AnimationUtils.loadAnimation(fab.getContext(), animation);
        anim.setDuration(200L);
        fab.startAnimation(anim);
    }

    /* 注册UI部件id函数 */
    public void defineEditTextComponents(){
        /* 个人信息 */
        demograEditText[0] = R.id.patientNameText;
        demograEditText[1] = R.id.patientAgeText;
        demograEditText[2] = R.id.patientSexText;
        demograEditText[3] = R.id.patientNFC;
        demograEditText[4] = R.id.patientDoc;
        demograEditText[5] = R.id.patientLevel;
        demograEditText[6] = R.id.patientTaboo;

        /* 病理数据 */
        pathologyEditText[0] = R.id.bodyTmpText;
        pathologyEditText[1] = R.id.bodyPulse;
        pathologyEditText[2] = R.id.breathFreq;
        pathologyEditText[3] = R.id.bloodRelease;
        pathologyEditText[4] = R.id.bloodTense;
        pathologyEditText[5] = R.id.bGas;
        pathologyEditText[6] = R.id.bloodNa;
        pathologyEditText[7] = R.id.bloodK;


        /* 其他备注 */
        noteEditText[0] = R.id.opPending;
        noteEditText[1] = R.id.pathoReport;
        noteEditText[2] = R.id.imaging;
        noteEditText[3] = R.id.ECG;
        noteEditText[4] = R.id.docNote;
    }

    /* 将UI部件链接至EditText类中 */
    public void registerEditText(){
        for(int i = 0; i < demograEditText.length; i++){
            /* 链接至临时EditText类 */
            EditText tmpEt = findViewById(demograEditText[i]);

            /* 将临时EditText类添加至ArrayList中 */
            demograEtGrp.add(tmpEt);

            /* 设定初始内容 */
            demograEtGrp.get(i).setText(pDemogra[i]);
        }

        for(int i = 0; i < pathologyEditText.length; i++){
            EditText tmpEt = findViewById(pathologyEditText[i]);
            pathologyEtGrp.add(tmpEt);
            pathologyEtGrp.get(i).setText(pPathology[i]);
        }

        for(int i = 0; i < noteEditText.length; i++){
            EditText tmpEt = findViewById(noteEditText[i]);
            noteEtGrp.add(tmpEt);
            noteEtGrp.get(i).setText(pNote[i]);
        }

    }

    /* 提取Patient类属性数据并存储至String组中 */
    public void setPatientValue(Patient p){
        pDemogra[0] = p.getName();
        pDemogra[1] = String.valueOf(p.getAge());
        pDemogra[2] = p.getSex();
        pDemogra[3] = p.getSlotID();
        pDemogra[4] = p.getDoc();
        pDemogra[5] = p.getLevel();
        pDemogra[6] = p.getTaboo();

        pPathology[0] = String.valueOf(p.getBodyTemp());
        pPathology[1] = String.valueOf(p.getPulse());
        pPathology[2] = String.valueOf(p.getBreath());
        pPathology[3] = String.valueOf(p.getReleasePressure());
        pPathology[4] = String.valueOf(p.getTensePressure());
        pPathology[5] = String.valueOf(p.getBgAnalysis());
        pPathology[6] = String.valueOf(p.getBloodNa());
        pPathology[7] = String.valueOf(p.getBloodK());

        pNote[0] = p.getOpPending();
        pNote[1] = p.getPathologyResult();
        pNote[2] = p.getImaging();
        pNote[3] = p.getECG();
        pNote[4] = p.getDocNote();
    }

    /* 提取App页面填写的信息并链接至Patient类中 */
    public void updatedPatientFromUI(){
        patientToSend = new Patient(demograEtGrp.get(0).getText().toString());
        patientToSend.setAge(Integer.parseInt(demograEtGrp.get(1).getText().toString()));
        patientToSend.setSex(demograEtGrp.get(2).getText().toString());
        patientToSend.setSlotID(demograEtGrp.get(3).getText().toString());
        patientToSend.setDoc(demograEtGrp.get(4).getText().toString());
        patientToSend.setLevel(demograEtGrp.get(5).getText().toString());
        patientToSend.setTaboo(demograEtGrp.get(6).getText().toString());

        patientToSend.setBodyTemp(Double.parseDouble(pathologyEtGrp.get(0).getText().toString()));
        patientToSend.setPulse(Integer.parseInt(pathologyEtGrp.get(1).getText().toString()));
        patientToSend.setBreath(Integer.parseInt(pathologyEtGrp.get(2).getText().toString()));
        patientToSend.setReleasePressure(Integer.parseInt(pathologyEtGrp.get(3).getText().toString()));
        patientToSend.setTensePressure(Integer.parseInt(pathologyEtGrp.get(4).getText().toString()));
        patientToSend.setBgAnalysis(Double.parseDouble(pathologyEtGrp.get(5).getText().toString()));
        patientToSend.setBloodNa(Integer.parseInt(pathologyEtGrp.get(6).getText().toString()));
        patientToSend.setBloodK(Double.parseDouble(pathologyEtGrp.get(7).getText().toString()));

        patientToSend.setOpPending(noteEtGrp.get(0).getText().toString());
        patientToSend.setPathologyResult(noteEtGrp.get(1).getText().toString());
        patientToSend.setImaging(noteEtGrp.get(2).getText().toString());
        patientToSend.setECG(noteEtGrp.get(3).getText().toString());
        patientToSend.setDocNote(noteEtGrp.get(4).getText().toString());
    }

    /* (解)锁页面所有文本框方法 */
    public void isLockEdit(boolean isLock){

        for(int i = 0; i < demograEtGrp.size(); i++){
            etSetEditable(demograEtGrp.get(i),!isLock);
        }

        for(int i = 0; i < pathologyEtGrp.size(); i++){
            etSetEditable(pathologyEtGrp.get(i),!isLock);
        }

        for(int i = 0; i < noteEtGrp.size(); i++){
            etSetEditable(noteEtGrp.get(i),!isLock);
        }

    }

    /* 设定单个EditText是否可编辑 */
    public void etSetEditable(EditText et, boolean isEditable) {
        if (!isEditable)
            et.setFocusable(false);
        else
            et.setFocusableInTouchMode(true);
    }

    /* 用于代替之前的Thread以显示进度条 */
    private class SendPatientAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog dialog;
        private String serverAddress;
        private Patient patient;

        public SendPatientAsyncTask(DisplayPatientActivity activity, String serverAddress, Patient patient) {
            this.dialog = new ProgressDialog(activity);
            this.serverAddress = serverAddress;
            this.patient = patient;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                /* 与服务器建立链接 */
                Socket socket = new Socket();

                /* 设定timeout为2000 ms */
                socket.connect(new InetSocketAddress(this.serverAddress, 34168), 2000);

                /* 准备发送至服务器的Patient类 */
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());

                /* 将参数中的Patient写入ObjectOutputStream，并发送至服务器 */
                outStream.writeObject(this.patient);
                socket.shutdownOutput();

                /* 关闭端口 */
                outStream.close();
                socket.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        /* 执行上端的doInBackground方法是同时执行的方法 */
        /* 用于显示上传进度，上传完成后终止 */
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage(getString(R.string.sendingPatient));
            this.dialog.setCancelable(false);
            this.dialog.show();
        }

        /* 上传完成后底部提示框 */
        /* 若doInBackground返回true，提示上传成功，否则提示上传失败 */
        @Override
        protected void onPostExecute(Boolean result) {
            /* 关闭onPreExecute()打开的进度提示框 */
            if (this.dialog.isShowing())
                this.dialog.dismiss();

            AlertDialog patientSent;
            patientSent = new AlertDialog.Builder(DisplayPatientActivity.this).create();
            patientSent.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            /* 显示提示消息 */
            if (result)
                patientSent.setMessage(getString(R.string.patientSent));
                //Snackbar.make(findViewById(R.id.myCoordinatorLayout), R.string.patientSent, Snackbar.LENGTH_LONG)
                        //.show();
            else
                patientSent.setMessage(getString(R.string.serverOut));

            patientSent.show();
        }
    }

    /* 由于显示进度框需要AsyncTask类，该线程不予使用 */
    @Deprecated
    class SendPatientThread extends Thread {
        private String serverAddress;
        private Patient patient;

        /* 新增参数设置，用于与UI交互 */
        public SendPatientThread(String serverAddress, Patient p) {
            this.serverAddress = serverAddress;
            this.patient = p;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(this.serverAddress, 34168), 2000);

                /* 准备发送至服务器的Patient类 */
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                /* 将参数中的Patient写入ObjectOutputStream，并发送至服务器 */
                outStream.writeObject(patient);
                socket.shutdownOutput();

                outStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
