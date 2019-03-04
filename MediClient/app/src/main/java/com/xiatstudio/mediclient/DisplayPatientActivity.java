package com.xiatstudio.mediclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class DisplayPatientActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_patient);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* 新建Intent */
        Intent intent = getIntent();

        /* 接收从MainActivity中传输的Patient类 */
        Bundle bundle = intent.getExtras();
        Patient patient = (Patient) bundle.getSerializable(MainActivity.EXTRA_PATIENT);

        /* 得到新页面文本部件ID */
        EditText etPatientName = findViewById(R.id.patientNameText);
        EditText etPatientAge = findViewById(R.id.patientAgeText);

        etPatientName.setText(patient.getName());
        etPatientAge.setText(String.valueOf(patient.getAge()));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
