package com.example.ambulncia_atividade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnEntrar = findViewById(R.id.btnEntrarSplash);
        Button btnCriarConta = findViewById(R.id.btnCriarContaSplash);

        btnEntrar.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        btnCriarConta.setOnClickListener(v -> {
            // Futura tela de cadastro — por ora redireciona ao login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("modo", "cadastro");
            startActivity(intent);
        });
    }
}
