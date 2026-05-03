package com.example.ambulncia_atividade;

import com.example.ambulncia_atividade.domain.security.SessionManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

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
        DatabaseHelper dh = new DatabaseHelper(this);
        SQLiteDatabase db = dh.getReadableDatabase();

        // inner join brabo pra nao precisar salvar o RG na shared prefs
        String sql = "SELECT p.rg, p.tipo_sanguineo, p.alergias FROM pacientes p INNER JOIN usuarios u ON p.id_usuario = u.id WHERE u.email = ?";
        Cursor c = db.rawQuery(sql, new String[]{mailUser});

        if (c.moveToFirst()) {
            txtRg.setText("RG: " + c.getString(0));
            txtSangue.setText("Tipagem: " + c.getString(1));
            String al = c.getString(2);
            txtAlergia.setText("Alergias: " + al);
            if (!al.equalsIgnoreCase("Nenhuma")) txtAlergia.setTextColor(Color.parseColor("#EF5350"));
        }
        c.close();
        db.close();
    }

    private void puxarVagasLivresDB() {
        DatabaseHelper dh = new DatabaseHelper(this);
        SQLiteDatabase db = dh.getReadableDatabase();

        // limit 3 senao explode a tela do celular do cara
        Cursor c = db.rawQuery("SELECT nome, nome_bairro, (vagas_totais - vagas_ocupadas) as l FROM hospitais WHERE l > 0 LIMIT 3", null);
        listHospViews.removeAllViews();

        if (c.moveToFirst()) {
            do {
                TextView item = new TextView(this);
                item.setText(c.getString(0) + " (" + c.getString(1) + ")\n" + c.getInt(2) + " vagas abertas");
                item.setTextColor(Color.parseColor("#81C784"));
                item.setBackgroundResource(R.drawable.bg_chip_normal);
                item.setPadding(32, 24, 32, 24);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 16);
                item.setLayoutParams(lp);
                listHospViews.addView(item);
            } while (c.moveToNext());
        } else {
            TextView v = new TextView(this);
            v.setText("Sistema em alerta vermelho. 0 vagas.");
            v.setTextColor(Color.parseColor("#AAAAAA"));
            listHospViews.addView(v);
        }
        c.close();
        db.close();
    }

    private void fazerLogout(SharedPreferences prefs) {
        prefs.edit().clear().apply();
        Intent i = new Intent(this, SplashActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}