package com.xiatstudio.mediclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class DisplayPatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* 新建Intent */
        final Intent intent = getIntent();

        /* 接收从MainActivity中传输的Patient类 */
        Bundle bundle = intent.getExtras();
        final Patient patient = (Patient) bundle.getSerializable(MainActivity.EXTRA_PATIENT);

        /* 得到Activity页面中EditText部件id并初始设置为不可编辑 */
        final EditText etPatientName = findViewById(R.id.patientNameText);
        etSetEditable(etPatientName,false);

        final EditText etPatientAge = findViewById(R.id.patientAgeText);
        etSetEditable(etPatientAge,false);

        final EditText etPatientSex = findViewById(R.id.patientSexText);
        etSetEditable(etPatientSex,false);

        final EditText etPatientNFC = findViewById(R.id.patientNFC);
        etSetEditable(etPatientNFC,false);

        final EditText etPatientDoc = findViewById(R.id.patientDoc);
        etSetEditable(etPatientDoc,false);

        /* 将病人信息显示其中 */
        etPatientName.setText(patient.getName());
        etPatientAge.setText(String.valueOf(patient.getAge()));
        etPatientSex.setText(patient.getSex());
        etPatientNFC.setText(patient.getSlotID());
        etPatientDoc.setText(patient.getDoc());etPatientName.setText(patient.getName());
        etPatientAge.setText(String.valueOf(patient.getAge()));
        etPatientSex.setText(patient.getSex());
        etPatientNFC.setText(patient.getSlotID());
        etPatientDoc.setText(patient.getDoc());

        final EditText etBodyTmp = findViewById(R.id.bodyTmpText);
        etSetEditable(etBodyTmp,false);

        final EditText etBodyPulse = findViewById(R.id.bodyPulse);
        etSetEditable(etBodyPulse,false);

        final EditText etBreathFreq = findViewById(R.id.breathFreq);
        etSetEditable(etBreathFreq,false);

        final EditText etBloodRelease = findViewById(R.id.bloodRelease);
        etSetEditable(etBloodRelease,false);

        final EditText etBloodTense = findViewById(R.id.bloodTense);
        etSetEditable(etBloodTense,false);

        final EditText etBloodGas = findViewById(R.id.bGas);
        etSetEditable(etBloodGas,false);

        etBodyTmp.setText(String.valueOf(patient.getBodyTemp()));
        etBodyPulse.setText(String.valueOf(patient.getPulse()));
        etBreathFreq.setText(String.valueOf(patient.getBreath()));
        etBloodRelease.setText(String.valueOf(patient.getReleasePressure()));
        etBloodTense.setText(String.valueOf(patient.getTensePressure()));
        etBloodGas.setText(String.valueOf(patient.getBgAnalysis()));

        /* 设置编辑按钮行为 */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                /* 解锁文本框编辑 */
                etSetEditable(etPatientName,true);
                etSetEditable(etPatientAge,true);
                etSetEditable(etPatientSex,true);
                etSetEditable(etPatientNFC,true);
                etSetEditable(etPatientDoc,true);

                etSetEditable(etBodyTmp,true);
                etSetEditable(etBodyPulse,true);
                etSetEditable(etBreathFreq,true);
                etSetEditable(etBodyPulse,true);
                etSetEditable(etBreathFreq,true);
                etSetEditable(etBloodRelease,true);
                etSetEditable(etBloodTense,true);
                etSetEditable(etBloodGas,true);

                /* 设定发送病人信息按钮行为 */
                FloatingActionButton fab2 = findViewById(R.id.sendPatient);
                /* 使按钮可见 */
                fab2.setVisibility(View.VISIBLE);
                fab2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        /* 建立新的Patient类，准备发送至服务器 */
                        Patient patientToSend = new Patient(etPatientName.getText().toString());
                        patientToSend.setAge(Integer.parseInt(etPatientAge.getText().toString()));
                        patientToSend.setSex(etPatientSex.getText().toString());
                        patientToSend.setSlotID(etPatientNFC.getText().toString());
                        patientToSend.setDoc(etPatientDoc.getText().toString());

                        patientToSend.setBodyTemp(Double.parseDouble(etBodyTmp.getText().toString()));
                        patientToSend.setPulse(Integer.parseInt(etBodyPulse.getText().toString()));
                        patientToSend.setBreath(Integer.parseInt(etBreathFreq.getText().toString()));
                        patientToSend.setReleasePressure(Integer.parseInt(etBloodRelease.getText().toString()));
                        patientToSend.setTensePressure(Integer.parseInt(etBloodTense.getText().toString()));
                        patientToSend.setBgAnalysis(Double.parseDouble(etBloodGas.getText().toString()));

                        /* 初始化并启动发送病人线程 */
                        DisplayPatientActivity threadRun = new DisplayPatientActivity();

                        DisplayPatientActivity.SendPatientThread sendPatient = threadRun.new SendPatientThread(intent.getStringExtra(MainActivity.EXTRA_SERVERADDR),patientToSend);

                        sendPatient.start();


                    }
                });
            }
        });
    }

    public void etSetEditable(EditText et, boolean isEditable){
        if (!isEditable)
            et.setFocusable(false);
        else
            et.setFocusableInTouchMode(true);
    }

    class SendPatientThread extends Thread{
        private String serverAddress;
        private Patient patient;

        /* 新增参数设置，用于与UI交互 */
        public SendPatientThread(String serverAddress, Patient p) {
            this.serverAddress = serverAddress;
            this.patient = p;
        }

        @Override
        public void run(){
            try{
                Socket socket = new Socket(serverAddress, 34168);

                /* 准备发送至服务器的Patient类 */
                ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
                /* 将参数中的Patient写入ObjectOutputStream，并发送至服务器 */
                outStream.writeObject(patient);
                socket.shutdownOutput();

                outStream.close();
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
