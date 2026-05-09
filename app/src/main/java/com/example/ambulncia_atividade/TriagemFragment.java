package com.example.ambulncia_atividade;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

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

        DatabaseHelper dbh = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbh.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nome_completo, tipo_sanguineo, alergias FROM pacientes WHERE rg = ?", new String[]{rg});

        if (c.moveToFirst()) {
            tvNome.setText(c.getString(0));
            tvSangue.setText(c.getString(1));
            String al = c.getString(2);

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
        c.close();
        db.close();
    }

    private void loadHist(SQLiteDatabase db, String rg) {
        wrapHist.removeAllViews();
        Cursor h = db.rawQuery("SELECT hospital, data_registro FROM historico WHERE rg_paciente = ? ORDER BY id DESC", new String[]{rg});

        if (h.moveToFirst()) {
            do {
                TextView t = new TextView(getContext());
                t.setText(h.getString(1) + " | " + h.getString(0));
                t.setTextColor(Color.WHITE);
                wrapHist.addView(t);
            } while (h.moveToNext());
        }
        h.close();
    }
}