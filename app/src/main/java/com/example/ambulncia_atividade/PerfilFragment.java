package com.example.ambulncia_atividade;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Hospital;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import com.example.ambulncia_atividade.domain.security.SessionManager;

import java.util.List;

public class PerfilFragment extends Fragment {

    private String mail, role, nome;
    private LinearLayout cardFicha, cardStats, listProx, listViews;
    private TextView tvRg, tvSangue, tvAlergia;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_perfil, container, false);

        // Puxando dados do seu cofre seguro (OWASP M2)
        SharedPreferences sf = SessionManager.getSecurePrefs(requireContext());
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
            loadVagas(); // Adicionado para carregar as vagas se for Socorrista/Admin
        }

        v.findViewById(R.id.btnVoltarPerfil).setVisibility(View.GONE);

        // Correção do Bug e Logout Seguro
        v.findViewById(R.id.btnLogout).setOnClickListener(btn -> {
            sf.edit()
                    .remove("email")
                    .remove("perfil")
                    .remove("nome")
                    .putBoolean("logado", false)
                    .apply(); // Mantém a db_key intacta!

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

    // Substituição do Cursor pelo seu DAO (Room)
    private void loadFicha() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Paciente paciente = db.pacienteDao().getPacientePorEmail(mail);

        if (paciente != null) {
            tvRg.setText("RG: " + paciente.rg);
            tvSangue.setText("Sangue: " + paciente.tipoSanguineo);
            String al = paciente.alergias;
            tvAlergia.setText("Alergias: " + al);

            if (al != null && !al.equalsIgnoreCase("Nenhuma")) {
                tvAlergia.setTextColor(Color.parseColor("#EF5350"));
            }
        }
    }

    // Lógica de vagas movida para Orientação a Objetos
    private void loadVagas() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        List<Hospital> hospitais = db.grafoDao().getHospitaisComVagas();
        listViews.removeAllViews();

        if (hospitais != null) {
            int count = 0;
            for (Hospital h : hospitais) {
                if (count >= 3) break; // Limita a 3 hospitais na tela

                int vagasLivres = h.vagasTotais - h.vagasOcupadas;
                if (vagasLivres > 0) {
                    TextView t = new TextView(getContext());
                    t.setText(h.nome + "\n" + vagasLivres + " vagas");
                    t.setTextColor(Color.parseColor("#81C784"));
                    t.setPadding(0, 0, 0, 16);
                    listViews.addView(t);
                    count++;
                }
            }
        }
    }
}