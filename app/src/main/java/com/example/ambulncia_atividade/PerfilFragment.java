package com.example.ambulncia_atividade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

public class PerfilFragment extends Fragment {

    private String mail, role, nome;
    private LinearLayout cardFicha, cardStats, listProx, listViews;
    private TextView tvRg, tvSangue, tvAlergia;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_perfil, container, false);

        SharedPreferences sf = requireActivity().getSharedPreferences("sos_leitos_prefs", Context.MODE_PRIVATE);
        nome = sf.getString("nome", "");
        mail = sf.getString("email", "");
        role = sf.getString("perfil", "");

        cardFicha = v.findViewById(R.id.containerFichaPaciente);
        cardStats = v.findViewById(R.id.containerEstatisticas);
        listProx = v.findViewById(R.id.containerHospitaisProximos);
        listViews = v.findViewById(R.id.listaHospitaisPerfil);
        tvRg = v.findViewById(R.id.tvPerfilRg);
        tvSangue = v.findViewById(R.id.tvPerfilSangue);
        tvAlergia = v.findViewById(R.id.tvPerfilAlergias);

        setupHeader(v);

        if (role.equals("PACIENTE")) {
            cardStats.setVisibility(View.GONE);
            cardFicha.setVisibility(View.VISIBLE);
            loadFicha();
        } else {
            cardStats.setVisibility(View.VISIBLE);
            cardFicha.setVisibility(View.GONE);
            ((TextView) v.findViewById(R.id.tvPerfilEmail)).setText(mail);
        }

        v.findViewById(R.id.btnVoltarPerfil).setVisibility(View.GONE);

        v.findViewById(R.id.btnLogout).setOnClickListener(btn -> {
            sf.edit().clear().apply();
            startActivity(i);
        });

        return v;
    }

    private void setupHeader(View v) {
        TextView avatar = v.findViewById(R.id.tvAvatar);
        String[] p = nome.split(" ");
        String a = p.length > 1 ? p[0].substring(0, 1) + p[1].substring(0, 1) : nome.substring(0, Math.min(2, nome.length()));
        avatar.setText(a.toUpperCase());
        ((TextView) v.findViewById(R.id.tvPerfilNome)).setText(nome);
        ((TextView) v.findViewById(R.id.tvPerfilRole)).setText(role);
    }

    private void loadFicha() {
        DatabaseHelper dbh = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT p.rg, p.tipo_sanguineo, p.alergias FROM pacientes p INNER JOIN usuarios u ON p.id_usuario = u.id WHERE u.email = ?", new String[]{mail});
        if (c.moveToFirst()) {
            tvRg.setText("RG: " + c.getString(0));
            tvSangue.setText("Sangue: " + c.getString(1));
            String al = c.getString(2);
            tvAlergia.setText("Alergias: " + al);
            if (!al.equals("Nenhuma")) tvAlergia.setTextColor(Color.parseColor("#EF5350"));
        }
        c.close();
        db.close();
    }

    private void loadVagas() {
        DatabaseHelper dbh = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nome, (vagas_totais - vagas_ocupadas) as l FROM hospitais WHERE l > 0 LIMIT 3", null);
        listViews.removeAllViews();
        if (c.moveToFirst()) {
            do {
                TextView t = new TextView(getContext());
                t.setText(c.getString(0) + "\n" + c.getInt(1) + " vagas");
                t.setTextColor(Color.parseColor("#81C784"));
                t.setPadding(0, 0, 0, 16);
                listViews.addView(t);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }
}