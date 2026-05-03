package com.example.ambulncia_atividade;

import com.example.ambulncia_atividade.domain.security.SessionManager;
import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Hospital;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import java.util.List;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    private String mailUser, perfUser, nameUser;
    private LinearLayout cardFicha, cardStats, listHospProximos, listHospViews;
    private TextView txtRg, txtSangue, txtAlergia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SharedPreferences sf = SessionManager.getSecurePrefs(this);
        nameUser = sf.getString("nome", "User");
        mailUser = sf.getString("email", "-");
        perfUser = sf.getString("perfil", "Socorrista");

        bindViews();
        setupHead();

        // chaveia a interface de acordo com a role do cara
        if (perfUser.equalsIgnoreCase("PACIENTE")) {
            cardStats.setVisibility(View.GONE);
            cardFicha.setVisibility(View.VISIBLE);
            listHospProximos.setVisibility(View.VISIBLE);
            puxarFichaDB();
            puxarVagasLivresDB();
        } else {
            // view de socorrista/admin
            cardStats.setVisibility(View.VISIBLE);
            cardFicha.setVisibility(View.GONE);
            listHospProximos.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tvPerfilEmail)).setText(mailUser);
        }

        findViewById(R.id.btnVoltarPerfil).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> fazerLogout(sf));
    }

    private void bindViews() {
        cardFicha = findViewById(R.id.containerFichaPaciente);
        cardStats = findViewById(R.id.containerEstatisticas);
        listHospProximos = findViewById(R.id.containerHospitaisProximos);
        listHospViews = findViewById(R.id.listaHospitaisPerfil);
        txtRg = findViewById(R.id.tvPerfilRg);
        txtSangue = findViewById(R.id.tvPerfilSangue);
        txtAlergia = findViewById(R.id.tvPerfilAlergias);
    }

    private void setupHead() {
        TextView imgLetras = findViewById(R.id.tvAvatar);
        String[] chunks = nameUser.split(" ");
        String abr = chunks.length >= 2 ? String.valueOf(chunks[0].charAt(0)) + chunks[1].charAt(0) : nameUser.substring(0, Math.min(2, nameUser.length()));
        imgLetras.setText(abr.toUpperCase());

        ((TextView) findViewById(R.id.tvPerfilNome)).setText(nameUser);
        String emot = perfUser.equalsIgnoreCase("PACIENTE") ? "👤 " : "🚑 ";
        ((TextView) findViewById(R.id.tvPerfilRole)).setText(emot + perfUser.toUpperCase() + " · SAMU SP");
    }

    private void puxarFichaDB() {
        AppDatabase db = AppDatabase.getInstance(this);
        Paciente paciente = db.pacienteDao().getPacientePorEmail(mailUser);

        if (paciente != null) {
            txtRg.setText("RG: " + paciente.rg);
            txtSangue.setText("Tipagem: " + paciente.tipoSanguineo);
            String al = paciente.alergias;
            txtAlergia.setText("Alergias: " + al);

            if (al != null && !al.equalsIgnoreCase("Nenhuma")) {
                txtAlergia.setTextColor(Color.parseColor("#EF5350"));
            }
        }
    }

    private void puxarVagasLivresDB() {
        AppDatabase db = AppDatabase.getInstance(this);
        List<Hospital> hospitais = db.grafoDao().getHospitaisComVagas();

        listHospViews.removeAllViews();

        if (hospitais != null && !hospitais.isEmpty()) {
            for (Hospital h : hospitais) {
                TextView item = new TextView(this);
                int vagasLivres = h.vagasTotais - h.vagasOcupadas;
                item.setText(h.nome + " (" + h.nomeBairro + ")\n" + vagasLivres + " vagas abertas");
                item.setTextColor(Color.parseColor("#81C784"));
                item.setBackgroundResource(R.drawable.bg_chip_normal);
                item.setPadding(32, 24, 32, 24);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 16);
                item.setLayoutParams(lp);
                listHospViews.addView(item);
            }
        } else {
            TextView v = new TextView(this);
            v.setText("Sistema em alerta vermelho. 0 vagas.");
            v.setTextColor(Color.parseColor("#AAAAAA"));
            listHospViews.addView(v);
        }
    }

    private void fazerLogout(SharedPreferences prefs) {
        prefs.edit().clear().apply();
        Intent i = new Intent(this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}