package com.example.ambulncia_atividade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etSenha;
    private Button btnEntrar;
    private TextView tvEsqueceu;
    private LinearLayout chipSocorrista, chipHospital, chipAdmin;

    private String perfilSelecionado = "Socorrista";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail         = findViewById(R.id.etEmail);
        etSenha         = findViewById(R.id.etSenha);
        btnEntrar       = findViewById(R.id.btnEntrar);
        tvEsqueceu      = findViewById(R.id.tvEsqueceu);
        chipSocorrista  = findViewById(R.id.chipSocorrista);
        chipHospital    = findViewById(R.id.chipHospital);
        chipAdmin       = findViewById(R.id.chipAdmin);

        // Verifica se veio no modo cadastro
        String modo = getIntent().getStringExtra("modo");
        if ("cadastro".equals(modo)) {
            btnEntrar.setText("CRIAR CONTA");
        }

        configurarChips();
        configurarListeners();
    }

    private void configurarChips() {
        selecionarChip(chipSocorrista);

        chipSocorrista.setOnClickListener(v -> {
            perfilSelecionado = "Socorrista";
            selecionarChip(chipSocorrista);
            desselecionarChip(chipHospital);
            desselecionarChip(chipAdmin);
        });

        chipHospital.setOnClickListener(v -> {
            perfilSelecionado = "Hospital";
            selecionarChip(chipHospital);
            desselecionarChip(chipSocorrista);
            desselecionarChip(chipAdmin);
        });

        chipAdmin.setOnClickListener(v -> {
            perfilSelecionado = "Admin";
            selecionarChip(chipAdmin);
            desselecionarChip(chipSocorrista);
            desselecionarChip(chipHospital);
        });
    }

    private void selecionarChip(LinearLayout chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_selecionado);
        // Muda cor dos TextViews filhos para vermelho
        for (int i = 0; i < chip.getChildCount(); i++) {
            if (chip.getChildAt(i) instanceof TextView) {
                ((TextView) chip.getChildAt(i)).setTextColor(Color.parseColor("#EF5350"));
            }
        }
    }

    private void desselecionarChip(LinearLayout chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_normal);
        for (int i = 0; i < chip.getChildCount(); i++) {
            if (chip.getChildAt(i) instanceof TextView) {
                ((TextView) chip.getChildAt(i)).setTextColor(Color.parseColor("#666666"));
            }
        }
    }

    private void configurarListeners() {
        btnEntrar.setOnClickListener(v -> realizarLogin());

        tvEsqueceu.setOnClickListener(v ->
            Toast.makeText(this, "Recuperação de senha enviada para o e-mail.", Toast.LENGTH_SHORT).show()
        );
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Informe o e-mail");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(senha)) {
            etSenha.setError("Informe a senha");
            etSenha.requestFocus();
            return;
        }

        // Salva sessão simples em SharedPreferences
        SharedPreferences prefs = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
        prefs.edit()
            .putString("email", email)
            .putString("perfil", perfilSelecionado)
            .putString("nome", extrairNomeDoEmail(email))
            .putBoolean("logado", true)
            .apply();

        Toast.makeText(this, "Bem-vindo, " + extrairNomeDoEmail(email) + "!", Toast.LENGTH_SHORT).show();

        // Vai para a tela principal
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String extrairNomeDoEmail(String email) {
        String parte = email.contains("@") ? email.split("@")[0] : email;
        String[] partes = parte.split("[._-]");
        StringBuilder nome = new StringBuilder();
        for (String p : partes) {
            if (!p.isEmpty()) {
                nome.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1).toLowerCase())
                    .append(" ");
            }
        }
        return nome.toString().trim();
    }
}
