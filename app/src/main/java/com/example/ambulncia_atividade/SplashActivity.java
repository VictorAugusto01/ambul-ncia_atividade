package com.example.ambulncia_atividade;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.btnEntrarSplash).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );

        findViewById(R.id.btnCriarContaSplash).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroPacienteActivity.class))
        );
    }
}