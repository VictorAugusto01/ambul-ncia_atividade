package com.example.ambulncia_atividade;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

public class CadastroPacienteActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etSenha, etRg, etSangue, etAlergias;

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

        findViewById(R.id.btnFinalizarCadastro).setOnClickListener(v -> salvar());
    }

    private void salvar() {
        String n = etNome.getText().toString().trim();
        String e = etEmail.getText().toString().trim();
        String s = etSenha.getText().toString().trim();
        String r = etRg.getText().toString().trim();
        String b = etSangue.getText().toString().trim();
        String a = etAlergias.getText().toString().trim();

        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(e) || TextUtils.isEmpty(s) || TextUtils.isEmpty(r)) return;
        if (a.isEmpty()) a = "Nenhuma";

        DatabaseHelper dbh = new DatabaseHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();

        try {
            db.beginTransaction();
            ContentValues user = new ContentValues();
            user.put("email", e);
            user.put("senha_hash", s);
            user.put("role", "PACIENTE");
            long uid = db.insert("usuarios", null, user);

            if (uid != -1) {
                ContentValues pac = new ContentValues();
                pac.put("id_usuario", uid);
                pac.put("nome_completo", n);
                pac.put("rg", r);
                pac.put("tipo_sanguineo", b.toUpperCase());
                pac.put("alergias", a);
                db.insert("pacientes", null, pac);
                db.setTransactionSuccessful();

                getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE).edit()
                        .putString("email", e).putString("perfil", "PACIENTE").putString("nome", n).putBoolean("logado", true).apply();

                Intent i = new Intent(this, PerfilActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}