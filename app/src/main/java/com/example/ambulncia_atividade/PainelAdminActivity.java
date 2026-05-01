package com.example.ambulncia_atividade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PainelAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painel_admin);

        // Resgata os dados da sessão salva no Login
        SharedPreferences prefs = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
        String nome = prefs.getString("nome", "Administrador");

        // Atualiza a interface com o nome do admin
        TextView tvBemVindo = findViewById(R.id.tvAdminBemVindo);
        tvBemVindo.setText("Olá, " + nome + "!");

        // Configura o botão de Sair
        Button btnSair = findViewById(R.id.btnSairAdmin);
        btnSair.setOnClickListener(v -> {
            // Limpa a sessão para não ficar logado para sempre
            prefs.edit().clear().apply();
            
            // Redireciona de volta para a tela de Login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}