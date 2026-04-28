package com.example.ambulncia_atividade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SharedPreferences prefs = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
        String nome   = prefs.getString("nome", "Usuário");
        String email  = prefs.getString("email", "—");
        String perfil = prefs.getString("perfil", "Socorrista");

        // Avatar com iniciais
        TextView tvAvatar = findViewById(R.id.tvAvatar);
        String[] partes = nome.split(" ");
        String iniciais = partes.length >= 2
            ? String.valueOf(partes[0].charAt(0)) + partes[1].charAt(0)
            : nome.substring(0, Math.min(2, nome.length()));
        tvAvatar.setText(iniciais.toUpperCase());

        ((TextView) findViewById(R.id.tvPerfilNome)).setText(nome);
        ((TextView) findViewById(R.id.tvPerfilRole)).setText(
            ("Socorrista".equals(perfil) ? "🚑 " : "🏥 ") + perfil.toUpperCase() + " · SAMU SP"
        );
        ((TextView) findViewById(R.id.tvPerfilEmail)).setText(email);
        ((TextView) findViewById(R.id.tvPerfilTelefone)).setText("(11) 9 9999-0000");
        ((TextView) findViewById(R.id.tvPerfilUnidade)).setText("Base Lapa – Zona Oeste");
        ((TextView) findViewById(R.id.tvPerfilAtendimentos)).setText("127 este mês");

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Botão voltar no header
        findViewById(R.id.btnVoltarPerfil).setOnClickListener(v -> finish());
    }
}
