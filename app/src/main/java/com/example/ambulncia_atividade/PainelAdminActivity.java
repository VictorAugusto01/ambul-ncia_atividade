package com.example.ambulncia_atividade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PainelAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_admin);

        SharedPreferences p = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
        TextView tv = findViewById(R.id.tvAdminBemVindo);
        tv.setText("Olá, " + p.getString("nome", "Admin") + "!");

        findViewById(R.id.btnSairAdmin).setOnClickListener(v -> {
            p.edit().clear().apply();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }
}