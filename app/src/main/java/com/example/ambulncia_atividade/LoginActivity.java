package com.example.ambulncia_atividade;


import com.example.ambulncia_atividade.domain.security.SessionManager;
import com.example.ambulncia_atividade.domain.security.PasswordHelper;
import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import com.example.ambulncia_atividade.domain.database.entity.Usuario;
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
    private LinearLayout chipSocorrista, chipHospital, chipPaciente;

    private String perfilSelecionado = "SOCORRISTA";
    private boolean isModoCadastro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvEsqueceu = findViewById(R.id.tvEsqueceu);
        chipSocorrista = findViewById(R.id.chipSocorrista);
        chipHospital = findViewById(R.id.chipHospital);
        chipPaciente = findViewById(R.id.chipPaciente);

        isModoCadastro = "cadastro".equals(getIntent().getStringExtra("modo"));
        if (isModoCadastro) {
            btnEntrar.setText("CRIAR CONTA");
            selecionarChip(chipPaciente);
            perfilSelecionado = "PACIENTE";
        } else {
            selecionarChip(chipSocorrista);
        }

        configurarChips();

        btnEntrar.setOnClickListener(v -> {
            if (isModoCadastro) realizarCadastro();
            else realizarLogin();
        });

        // TODO: criar tela de "recuperacao de senha" validando o RG no banco local
        tvEsqueceu.setOnClickListener(v -> Toast.makeText(this, "Funcionalidade em desenvolvimento...", Toast.LENGTH_SHORT).show());
    }

    private void configurarChips() {
        chipSocorrista.setOnClickListener(v -> alterarPerfil("SOCORRISTA", chipSocorrista, chipHospital, chipPaciente));
        chipHospital.setOnClickListener(v -> alterarPerfil("HOSPITAL", chipHospital, chipSocorrista, chipPaciente));
        chipPaciente.setOnClickListener(v -> alterarPerfil("PACIENTE", chipPaciente, chipSocorrista, chipHospital));
    }

    private void alterarPerfil(String role, LinearLayout selecionado, LinearLayout... desselecionados) {
        perfilSelecionado = role;
        selecionarChip(selecionado);
        for (LinearLayout chip : desselecionados) desselecionarChip(chip);
    }

    private void selecionarChip(LinearLayout chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_selecionado);
        for (int i = 0; i < chip.getChildCount(); i++) {
            if (chip.getChildAt(i) instanceof TextView) ((TextView) chip.getChildAt(i)).setTextColor(Color.parseColor("#EF5350"));
        }
    }

    private void desselecionarChip(LinearLayout chip) {
        chip.setBackgroundResource(R.drawable.bg_chip_normal);
        for (int i = 0; i < chip.getChildCount(); i++) {
            if (chip.getChildAt(i) instanceof TextView) ((TextView) chip.getChildAt(i)).setTextColor(Color.parseColor("#666666"));
        }
    }

    private void realizarCadastro() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha tudo", Toast.LENGTH_SHORT).show();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        try {
            String saltAleatorio = PasswordHelper.generateSalt();
            String senhaHasheada = PasswordHelper.hashPassword(senha, saltAleatorio);

            Usuario novoUser = new Usuario();
            novoUser.email = email;
            novoUser.senhaHash = senhaHasheada;
            novoUser.salt = saltAleatorio;
            novoUser.role = perfilSelecionado;

            long userId = db.usuarioDao().inserir(novoUser);

            if (userId == -1) {
                Toast.makeText(this, "E-mail já existe.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (perfilSelecionado.equals("PACIENTE")) {
                Paciente novoPac = new Paciente();
                novoPac.idUsuario = (int) userId;
                novoPac.nomeCompleto = extrairNome(email);
                novoPac.rg = "PENDENTE-" + userId;
                db.pacienteDao().inserir(novoPac);
            }

            Toast.makeText(this, "Criado com sucesso! Pode logar.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro fatal no banco.", Toast.LENGTH_SHORT).show();
        }
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) return;

        AppDatabase db = AppDatabase.getInstance(this);

        Usuario userBanco = db.usuarioDao().getUsuarioPorEmail(email);

        if (userBanco != null) {
            // Valida a senha digitada contra o Hash+Salt salvo no objeto
            if (PasswordHelper.checkPassword(senha, userBanco.senhaHash, userBanco.salt)) {
                salvarPrefs(email, userBanco.role);
                rotearApp(userBanco.role);
            } else {
                Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarPrefs(String email, String role) {
        SessionManager.getSecurePrefs(this).edit()
                .putString("email", email)
                .putString("perfil", role)
                .putString("nome", extrairNome(email))
                .putBoolean("logado", true)
                .apply();
    }

    private void rotearApp(String role) {
        Intent intent;
        if (role.equalsIgnoreCase("PACIENTE")) {
            intent = new Intent(this, PerfilActivity.class);
        } else if (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("HOSPITAL")) {
            intent = new Intent(this, PainelAdminActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // quebra-galho pra pegar o nome a partir do email antes do cara atualizar o perfil
    private String extrairNome(String email) {
        String parte = email.contains("@") ? email.split("@")[0] : email;
        String[] partes = parte.split("[._-]");
        StringBuilder nome = new StringBuilder();
        for (String p : partes) {
            if (!p.isEmpty()) nome.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase()).append(" ");
        }
        return nome.toString().trim();
    }
}