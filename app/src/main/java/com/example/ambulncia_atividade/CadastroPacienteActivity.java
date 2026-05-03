package com.example.ambulncia_atividade;

import com.example.ambulncia_atividade.domain.security.SessionManager;
import com.example.ambulncia_atividade.domain.security.PasswordHelper;
import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import com.example.ambulncia_atividade.domain.database.entity.Usuario;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


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

        AppDatabase db = AppDatabase.getInstance(this);

        try {
            db.beginTransaction(); // O Room suporta a mesma estrutura de transação

            String saltAleatorio = PasswordHelper.generateSalt();
            String senhaHasheada = PasswordHelper.hashPassword(senha, saltAleatorio);

            Usuario novoUser = new Usuario();
            novoUser.email = email;
            novoUser.senhaHash = senhaHasheada;
            novoUser.salt = saltAleatorio;
            novoUser.role = "PACIENTE";

            long userId = db.usuarioDao().inserir(novoUser);

            if (userId == -1) {
                Toast.makeText(this, "E-mail já cadastrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            Paciente novoPac = new Paciente();
            novoPac.idUsuario = (int) userId;
            novoPac.nomeCompleto = nome;
            novoPac.rg = rg;
            novoPac.tipoSanguineo = sangue.toUpperCase();
            novoPac.alergias = alergias;

            long pacienteId = db.pacienteDao().inserir(novoPac);

            if (pacienteId == -1) {
                Toast.makeText(this, "RG já cadastrado.", Toast.LENGTH_SHORT).show();
                return;
            }

            db.setTransactionSuccessful();

            salvarSessaoLocal(email, nome);
            Intent intent = new Intent(this, PerfilActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Toast.makeText(this, "Erro interno no banco.", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
        }
    }

    private void salvarSessaoLocal(String email, String nome) {
        SharedPreferences prefs = SessionManager.getSecurePrefs(this);
        prefs.edit()
                .putString("email", email)
                .putString("perfil", "PACIENTE")
                .putString("nome", nome)
                .putBoolean("logado", true)
                .apply();
    }
}