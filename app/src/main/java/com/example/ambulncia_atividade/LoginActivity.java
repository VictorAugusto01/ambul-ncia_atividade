package com.example.ambulncia_atividade;

import android.content.ContentValues;
import android.content.Intent;
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
    private LinearLayout chipSocorrista, chipHospital, chipPaciente;
    private String roleAtual = "SOCORRISTA";
    private boolean modoCadastro = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        chipSocorrista = findViewById(R.id.chipSocorrista);
        chipHospital = findViewById(R.id.chipHospital);
        chipPaciente = findViewById(R.id.chipPaciente);

        modoCadastro = "cadastro".equals(getIntent().getStringExtra("modo"));

        if (modoCadastro) {
            btnEntrar.setText("CRIAR CONTA");
            setRole("PACIENTE", chipPaciente, chipSocorrista, chipHospital);
        } else {
            setRole("SOCORRISTA", chipSocorrista, chipHospital, chipPaciente);
        }

        chipSocorrista.setOnClickListener(v -> setRole("SOCORRISTA", chipSocorrista, chipHospital, chipPaciente));
        chipHospital.setOnClickListener(v -> setRole("HOSPITAL", chipHospital, chipSocorrista, chipPaciente));
        chipPaciente.setOnClickListener(v -> setRole("PACIENTE", chipPaciente, chipSocorrista, chipHospital));

        btnEntrar.setOnClickListener(v -> {
            if (modoCadastro) registrar();
            else autenticar();
        });
    }

    private void setRole(String r, LinearLayout s, LinearLayout... d) {
        roleAtual = r;
        s.setBackgroundResource(R.drawable.bg_chip_selecionado);
        for (int i = 0; i < s.getChildCount(); i++) {
            if (s.getChildAt(i) instanceof TextView) ((TextView) s.getChildAt(i)).setTextColor(Color.parseColor("#EF5350"));
        }
        for (LinearLayout c : d) {
            c.setBackgroundResource(R.drawable.bg_chip_normal);
            for (int i = 0; i < c.getChildCount(); i++) {
                if (c.getChildAt(i) instanceof TextView) ((TextView) c.getChildAt(i)).setTextColor(Color.parseColor("#666666"));
            }
        }
    }

    private void registrar() {
        String mail = etEmail.getText().toString().trim();
        String pass = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)) return;

        DatabaseHelper dbh = new DatabaseHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();

        try {
            ContentValues cv = new ContentValues();
            cv.put("email", mail);
            cv.put("senha_hash", pass);
            cv.put("salt", "s");
            cv.put("role", roleAtual);

            long id = db.insert("usuarios", null, cv);
            if (id != -1 && roleAtual.equals("PACIENTE")) {
                ContentValues cp = new ContentValues();
                cp.put("id_usuario", id);
                cp.put("nome_completo", mail.split("@")[0]);
                cp.put("rg", "ID-" + id);
                db.insert("pacientes", null, cp);
            }
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Erro DB", Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    private void autenticar() {
        String mail = etEmail.getText().toString().trim();
        String pass = etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)) return;

        DatabaseHelper dbh = new DatabaseHelper(this);
        SQLiteDatabase db = dbh.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT role FROM usuarios WHERE email = ? AND senha_hash = ?", new String[]{mail, pass});

        if (c.moveToFirst()) {
            String role = c.getString(0);

            getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE).edit()
                    .putString("email", mail)
                    .putString("perfil", role)
                    .putString("nome", mail.split("@")[0])
                    .putBoolean("logado", true).apply();

            Intent i;
            if (role.equalsIgnoreCase("PACIENTE")) i = new Intent(this, PerfilActivity.class);
            else if (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("HOSPITAL")) i = new Intent(this, PainelAdminActivity.class);
            else i = new Intent(this, MainActivity.class);

            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Inválido", Toast.LENGTH_SHORT).show();
        }
        c.close();
        db.close();
    }
}