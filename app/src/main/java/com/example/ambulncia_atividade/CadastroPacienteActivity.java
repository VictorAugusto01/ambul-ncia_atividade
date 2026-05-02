package com.example.ambulncia_atividade;

import com.example.ambulncia_atividade.domain.security.PasswordHelper;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;


public class CadastroPacienteActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etSenha, etRg, etSangue, etAlergias;
    private Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_paciente);

        etNome = findViewById(R.id.etCadNome);
        etEmail = findViewById(R.id.etCadEmail);
        etSenha = findViewById(R.id.etCadSenha);
        etRg = findViewById(R.id.etCadRg);
        etSangue = findViewById(R.id.etCadSangue);
        etAlergias = findViewById(R.id.etCadAlergias);
        btnCadastrar = findViewById(R.id.btnFinalizarCadastro);

        btnCadastrar.setOnClickListener(v -> processarCadastro());
    }

    private void processarCadastro() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String rg = etRg.getText().toString().trim();
        String sangue = etSangue.getText().toString().trim();
        String alergias = etAlergias.getText().toString().trim();

        // fail fast pra n processar atoa
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(rg)) {
            Toast.makeText(this, "Preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // se o cara n botar alergia, joga padrao senao da nullpointer no card de triagem
        if (alergias.isEmpty()) alergias = "Nenhuma";

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction(); // trava a transacao pq sao 2 inserts casados

            // Gera o salt e o hash antes de salvar
            String saltAleatorio = PasswordHelper.generateSalt();
            String senhaHasheada = PasswordHelper.hashPassword(senha, saltAleatorio);

            ContentValues userValues = new ContentValues();
            userValues.put("email", email);
            userValues.put("senha_hash", senhaHasheada);
            userValues.put("salt", saltAleatorio);
            userValues.put("role", "PACIENTE");
            long userId = db.insert("usuarios", null, userValues);

            if (userId == -1) {
                Toast.makeText(this, "E-mail já cadastrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues pacienteValues = new ContentValues();
            pacienteValues.put("id_usuario", userId);
            pacienteValues.put("nome_completo", nome);
            pacienteValues.put("rg", rg);
            pacienteValues.put("tipo_sanguineo", sangue.toUpperCase());
            pacienteValues.put("alergias", alergias);
            long pacienteId = db.insert("pacientes", null, pacienteValues);

            if (pacienteId == -1) {
                Toast.makeText(this, "RG já cadastrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.setTransactionSuccessful(); // sucesso, comita as duas

            salvarSessaoLocal(email, nome);
            Intent intent = new Intent(this, PerfilActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            // Log.e("CADASTRO_ERRO", "falhou no insert: ", e);
            Toast.makeText(this, "Erro interno no banco.", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    private void salvarSessaoLocal(String email, String nome) {
        SharedPreferences prefs = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("email", email)
                .putString("perfil", "PACIENTE")
                .putString("nome", nome)
                .putBoolean("logado", true)
                .apply();
    }
}