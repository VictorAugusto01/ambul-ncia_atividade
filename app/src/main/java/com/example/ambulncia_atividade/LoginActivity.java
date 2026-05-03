package com.example.ambulncia_atividade;


import com.example.ambulncia_atividade.domain.security.SessionManager;
import com.example.ambulncia_atividade.domain.security.PasswordHelper;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

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
            selecionarChip(chipPaciente); // força paciente se tiver criando conta
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

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues userValues = new ContentValues();
            String saltAleatorio = PasswordHelper.generateSalt();
            String senhaHasheada = PasswordHelper.hashPassword(senha, saltAleatorio);

            userValues.put("email", email);
            userValues.put("senha_hash", senhaHasheada);
            userValues.put("salt", saltAleatorio);
            userValues.put("role", perfilSelecionado);

            long userId = db.insert("usuarios", null, userValues);
            if (userId == -1) {
                Toast.makeText(this, "E-mail já existe.", Toast.LENGTH_SHORT).show();
                return;
            }

            // workaround provisorio: enfia o cara na tabela pacientes com um RG gerado pra n estourar FK
            if (perfilSelecionado.equals("PACIENTE")) {
                ContentValues pValues = new ContentValues();
                pValues.put("id_usuario", userId);
                pValues.put("nome_completo", extrairNome(email));
                pValues.put("rg", "PENDENTE-" + userId);
                db.insert("pacientes", null, pValues);
            }

            Toast.makeText(this, "Criado com sucesso! Pode logar.", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Erro fatal", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) return;

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Busca apenas pelo e-mail para resgatar o Sal e o Hash
        Cursor cursor = db.rawQuery("SELECT role, senha_hash, salt FROM usuarios WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            String roleBanco = cursor.getString(0);
            String hashBanco = cursor.getString(1);
            String saltBanco = cursor.getString(2);

            // Valida a senha digitada contra o Hash+Salt salvo
            if (PasswordHelper.checkPassword(senha, hashBanco, saltBanco)) {
                salvarPrefs(email, roleBanco);
                rotearApp(roleBanco);
            } else {
                Toast.makeText(this, "Senha incorreta.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_LONG).show();
        }

        cursor.close();
        db.close();
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
            intent = new Intent(this, TriagemActivity.class);
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