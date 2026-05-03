package com.example.ambulncia_atividade;

import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Historico;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import java.util.List;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
// import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TriagemActivity extends AppCompatActivity {

    private EditText etBuscaRg;
    private Button btnBuscarRg, btnIrParaMapa;
    private LinearLayout cardFichaPaciente, wrapperHistorico;
    private TextView tvNomePaciente, tvSangue, tvAlergias, alertaRestricoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triagem);

        initViews();

        btnBuscarRg.setOnClickListener(v -> pesquisarNaBase());
        btnIrParaMapa.setOnClickListener(v -> startActivity(new Intent(this, GrafoActivity.class)));
    }

    private void initViews() {
        etBuscaRg = findViewById(R.id.etBuscaRg);
        btnBuscarRg = findViewById(R.id.btnBuscarRg);
        btnIrParaMapa = findViewById(R.id.btnIrParaMapa);
        cardFichaPaciente = findViewById(R.id.cardFichaPaciente);
        tvNomePaciente = findViewById(R.id.tvNomePacienteTriagem);
        tvSangue = findViewById(R.id.tvSangueTriagem);
        tvAlergias = findViewById(R.id.tvAlergiasTriagem);
        alertaRestricoes = findViewById(R.id.alertaAlergia);

        // gerando via codigo pra n precisar refazer o xml td agr
        wrapperHistorico = new LinearLayout(this);
        wrapperHistorico.setOrientation(LinearLayout.VERTICAL);
        wrapperHistorico.setPadding(0, 32, 0, 0);
        cardFichaPaciente.addView(wrapperHistorico);

        cardFichaPaciente.setVisibility(View.GONE);
    }

    private void pesquisarNaBase() {
        String numRg = etBuscaRg.getText().toString().trim();

        if (numRg.isEmpty()) {
            etBuscaRg.setError("Informe o RG!");
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);

        try {
            Paciente paciente = db.pacienteDao().getPacientePorRg(numRg);

            if (paciente != null) {
                populaProntuario(paciente);
                montarListaHistorico(db, numRg);
            } else {
                Toast.makeText(this, "Registro fantasma (n achou nada)", Toast.LENGTH_SHORT).show();
                cardFichaPaciente.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Crashou a query :(", Toast.LENGTH_SHORT).show();
        }
    }

    private void populaProntuario(Paciente paciente) {
        String nome = paciente.nomeCompleto;
        String sang = paciente.tipoSanguineo;
        String alerg = paciente.alergias;

        tvNomePaciente.setText("Paciente: " + (nome != null ? nome : "-"));
        tvSangue.setText("Sangue: " + (sang != null ? sang : "-"));

        if (alerg != null && !alerg.trim().isEmpty() && !alerg.equalsIgnoreCase("nenhuma")) {
            tvAlergias.setText("ALERGIAS: " + alerg);
            alertaRestricoes.setText("⚠️ ATENÇÃO: CHOQUE ANAFILÁTICO POSSÍVEL");
            alertaRestricoes.setVisibility(View.VISIBLE);
            cardFichaPaciente.setBackgroundColor(Color.parseColor("#3E1A1A"));
        } else {
            tvAlergias.setText("Ficha limpa de alergias");
            alertaRestricoes.setVisibility(View.GONE);
            cardFichaPaciente.setBackgroundColor(Color.parseColor("#1E1E1E"));
        }

        cardFichaPaciente.setVisibility(View.VISIBLE);
    }

    private void montarListaHistorico(AppDatabase db, String rgTarget) {
        wrapperHistorico.removeAllViews();

        TextView t = new TextView(this);
        t.setText("ÚLTIMOS ENCAMINHAMENTOS");
        t.setTextColor(Color.parseColor("#bfecff"));
        t.setTextSize(12f);
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        t.setPadding(0, 0, 0, 16);
        wrapperHistorico.addView(t);

        List<Historico> historicos = db.pacienteDao().getHistoricoPorRg(rgTarget);

        if (historicos != null && !historicos.isEmpty()) {
            for (Historico h : historicos) {
                TextView item = new TextView(this);
                item.setText("• " + h.dataRegistro + "  |  " + h.hospital);
                item.setTextColor(Color.WHITE);
                item.setPadding(0, 0, 0, 8);
                wrapperHistorico.addView(item);
            }
        } else {
            TextView none = new TextView(this);
            none.setText("Paciente virgem na base (0 historicos)");
            none.setTextColor(Color.GRAY);
            none.setTypeface(null, android.graphics.Typeface.ITALIC);
            wrapperHistorico.addView(none);
        }
    }
}