package com.example.ambulncia_atividade;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class GrafoActivity extends AppCompatActivity {

    private GrafoView grafoView;
    private Spinner spinnerBairro;
    private Button btnBuscar, btnReset, btnVoltar;
    private LinearLayout logContainer;
    private ScrollView logScroll;
    private LinearLayout cardResultado, cardContingencia;
    private TextView tvNomeResultado, tvVagasResultado, tvDistResultado;
    private TextView tvNomeContingencia;

    private final List<String> bairros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafo);

        grafoView       = findViewById(R.id.grafoView);
        spinnerBairro   = findViewById(R.id.spinnerBairroGrafo);
        btnBuscar       = findViewById(R.id.btnBuscarGrafo);
        btnReset        = findViewById(R.id.btnResetGrafo);
        btnVoltar       = findViewById(R.id.btnVoltarGrafo);
        logContainer    = findViewById(R.id.logContainerGrafo);
        logScroll       = findViewById(R.id.logScrollGrafo);
        cardResultado   = findViewById(R.id.cardResultadoGrafo);
        cardContingencia = findViewById(R.id.cardContingenciaGrafo);
        tvNomeResultado  = findViewById(R.id.tvNomeResultadoGrafo);
        tvVagasResultado = findViewById(R.id.tvVagasResultadoGrafo);
        tvDistResultado  = findViewById(R.id.tvDistResultadoGrafo);
        tvNomeContingencia = findViewById(R.id.tvNomeContingenciaGrafo);

        construirGrafo();
        configurarSpinner();
        configurarListeners();
    }

    private void construirGrafo() {
        // Nós: id, xPct (0-1), yPct (0-1), totalLeitos, leitosOcupados
        grafoView.adicionarNo("Lapa",              0.18f, 0.12f, 160, 135);
        grafoView.adicionarNo("Perdizes",          0.45f, 0.06f, 250, 205);
        grafoView.adicionarNo("Vila Madalena",     0.72f, 0.15f, 120,  75);
        grafoView.adicionarNo("Pinheiros",         0.70f, 0.38f, 360, 358);
        grafoView.adicionarNo("Alto de Pinheiros", 0.36f, 0.30f, 215, 215);
        grafoView.adicionarNo("Itaim Bibi",        0.80f, 0.58f, 295, 294);
        grafoView.adicionarNo("Butantã",           0.18f, 0.45f, 250, 190);
        grafoView.adicionarNo("Morumbi",           0.48f, 0.55f, 140, 138);
        grafoView.adicionarNo("Santo Amaro",       0.75f, 0.76f, 155, 115);
        grafoView.adicionarNo("Campo Limpo",       0.40f, 0.76f, 140,  95);
        grafoView.adicionarNo("Capão Redondo",     0.18f, 0.76f, 125, 115);
        grafoView.adicionarNo("Interlagos",        0.52f, 0.90f, 130, 120);

        grafoView.conectar("Lapa", "Perdizes");
        grafoView.conectar("Lapa", "Alto de Pinheiros");
        grafoView.conectar("Lapa", "Butantã");
        grafoView.conectar("Perdizes", "Vila Madalena");
        grafoView.conectar("Perdizes", "Alto de Pinheiros");
        grafoView.conectar("Vila Madalena", "Pinheiros");
        grafoView.conectar("Pinheiros", "Alto de Pinheiros");
        grafoView.conectar("Pinheiros", "Itaim Bibi");
        grafoView.conectar("Alto de Pinheiros", "Butantã");
        grafoView.conectar("Itaim Bibi", "Morumbi");
        grafoView.conectar("Itaim Bibi", "Santo Amaro");
        grafoView.conectar("Butantã", "Morumbi");
        grafoView.conectar("Morumbi", "Campo Limpo");
        grafoView.conectar("Campo Limpo", "Capão Redondo");
        grafoView.conectar("Campo Limpo", "Santo Amaro");
        grafoView.conectar("Campo Limpo", "Interlagos");
        grafoView.conectar("Santo Amaro", "Interlagos");
        grafoView.conectar("Capão Redondo", "Interlagos");

        bairros.add("Lapa");
        bairros.add("Perdizes");
        bairros.add("Vila Madalena");
        bairros.add("Pinheiros");
        bairros.add("Alto de Pinheiros");
        bairros.add("Itaim Bibi");
        bairros.add("Butantã");
        bairros.add("Morumbi");
        bairros.add("Santo Amaro");
        bairros.add("Campo Limpo");
        bairros.add("Capão Redondo");
        bairros.add("Interlagos");

        grafoView.setBfsListener(new GrafoView.BfsListener() {
            @Override
            public void onLogLine(String line, int tipo) {
                runOnUiThread(() -> adicionarLog(line, tipo));
            }

            @Override
            public void onResultado(String nome, String bairro, int vagas, int dist, boolean contingencia) {
                runOnUiThread(() => mostrarResultado(nome, vagas, dist, contingencia));
            }
        });
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bairros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBairro.setAdapter(adapter);

        spinnerBairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                String b = bairros.get(pos);
                grafoView.setBairroSelecionado(b);
                limparLog();
                esconderResultados();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        grafoView.setBairroSelecionado(bairros.get(0));
    }

    private void configurarListeners() {
        btnBuscar.setOnClickListener(v -> {
            limparLog();
            esconderResultados();
            grafoView.iniciarBfs();
        });

        btnReset.setOnClickListener(v -> {
            grafoView.resetar();
            grafoView.setBairroSelecionado(bairros.get(spinnerBairro.getSelectedItemPosition()));
            limparLog();
            esconderResultados();
        });

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void adicionarLog(String texto, int tipo) {
        if (texto.isEmpty()) return;
        TextView tv = new TextView(this);
        tv.setTextSize(11f);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setLineSpacing(4f, 1f);
        tv.setPadding(0, 1, 0, 1);
        tv.setText(texto);

        switch (tipo) {
            case 1: tv.setTextColor(Color.parseColor("#66CC66")); break;
            case 2: tv.setTextColor(Color.parseColor("#FFCA28")); break;
            case 3: tv.setTextColor(Color.parseColor("#FF5555")); break;
            default: tv.setTextColor(Color.parseColor("#447744")); break;
        }

        logContainer.addView(tv);
        logScroll.post(() -> logScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void mostrarResultado(String nome, int vagas, int dist, boolean contingencia) {
        if (contingencia) {
            cardResultado.setVisibility(View.GONE);
            tvNomeContingencia.setText(nome);
            cardContingencia.setVisibility(View.VISIBLE);
        } else {
            cardContingencia.setVisibility(View.GONE);
            tvNomeResultado.setText(nome);
            tvVagasResultado.setText(vagas + " leitos livres");
            tvDistResultado.setText(dist + " bairro(s) de distância");
            cardResultado.setVisibility(View.VISIBLE);
        }
    }

    private void limparLog() {
        logContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setTextSize(11f);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setText("Aguardando busca...");
        tv.setTextColor(Color.parseColor("#447744"));
        logContainer.addView(tv);
    }

    private void esconderResultados() {
        cardResultado.setVisibility(View.GONE);
        cardContingencia.setVisibility(View.GONE);
    }
}
