package com.example.ambulncia_atividade;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Historico;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;

import java.util.List;

public class TriagemFragment extends Fragment {

    private EditText etRg;
    private LinearLayout cardFicha, wrapHist;
    private TextView tvNome, tvSangue, tvAlergias, tvAlerta;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_triagem, container, false);

        etRg = v.findViewById(R.id.etBuscaRg);
        cardFicha = v.findViewById(R.id.cardFichaPaciente);
        tvNome = v.findViewById(R.id.tvNomePacienteTriagem);
        tvSangue = v.findViewById(R.id.tvSangueTriagem);
        tvAlergias = v.findViewById(R.id.tvAlergiasTriagem);
        tvAlerta = v.findViewById(R.id.alertaAlergia);

        wrapHist = new LinearLayout(getContext());
        wrapHist.setOrientation(LinearLayout.VERTICAL);
        wrapHist.setPadding(0, 32, 0, 0);
        cardFicha.addView(wrapHist);
        cardFicha.setVisibility(View.GONE);

        v.findViewById(R.id.btnBuscarRg).setOnClickListener(btn -> buscar());

        v.findViewById(R.id.btnIrParaMapa).setOnClickListener(btn -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).pularParaMapa();
            }
        });

        return v;
    }

    private void buscar() {
        String rg = etRg.getText().toString().trim();
        if (rg.isEmpty()) return;

        // Conecta ao banco de dados Room
        AppDatabase db = AppDatabase.getInstance(requireContext());
        Paciente paciente = db.pacienteDao().getPacientePorRg(rg);

        if (paciente != null) {
            tvNome.setText(paciente.nomeCompleto);
            tvSangue.setText(paciente.tipoSanguineo);
            String al = paciente.alergias;

            if (al != null && !al.trim().isEmpty() && !al.equalsIgnoreCase("nenhuma")) {
                tvAlergias.setText(al);
                tvAlerta.setVisibility(View.VISIBLE);
                cardFicha.setBackgroundColor(Color.parseColor("#3E1A1A"));
            } else {
                tvAlergias.setText("Nenhuma");
                tvAlerta.setVisibility(View.GONE);
                cardFicha.setBackgroundColor(Color.parseColor("#1E1E1E"));
            }

            cardFicha.setVisibility(View.VISIBLE);
            loadHist(db, rg);
        } else {
            cardFicha.setVisibility(View.GONE);
        }
    }

    private void loadHist(AppDatabase db, String rg) {
        wrapHist.removeAllViews();

        // Puxa o histórico do banco de dados usando a entidade
        List<Historico> historicos = db.pacienteDao().getHistoricoPorRg(rg);

        if (historicos != null && !historicos.isEmpty()) {
            for (Historico h : historicos) {
                TextView t = new TextView(getContext());
                // Formata o texto
                t.setText(h.dataRegistro + " | " + h.hospital);
                t.setTextColor(Color.WHITE);
                wrapHist.addView(t);
            }
        }
    }
}