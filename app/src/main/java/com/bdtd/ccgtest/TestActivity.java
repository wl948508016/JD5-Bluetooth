package com.bdtd.ccgtest;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bdtd.ccg.CcgHelperKt;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CcgHelperKt.Companion.getInstance().openBluetoothDialog(TestActivity.this, false, new CcgHelperKt.OnCcgHelperKtListener() {
                    @Override
                    public void onResultData(@NonNull String methaneVal, @NonNull String co, @NonNull String o2, @NonNull String temp, @NonNull String co2) {
                        Toast.makeText(TestActivity.this, "methaneVal-->${methaneVal}," +
                            "co-->${co}," +
                            "o2-->${o2}," +
                            "temp-->${temp}," +
                            "co2-->${co2}", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
