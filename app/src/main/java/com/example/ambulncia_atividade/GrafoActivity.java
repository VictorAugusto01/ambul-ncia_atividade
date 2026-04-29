package com.example.ambulncia_atividade;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

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
        // Abre conexão com seu banco de dados
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Puxa os Bairros, o X e Y deles, e soma as vagas dos hospitais daquele bairro
        String queryBairros = "SELECT b.nome, b.x_pct, b.y_pct, " +
                "IFNULL(SUM(h.vagas_totais), 0), IFNULL(SUM(h.vagas_ocupadas), 0) " +
                "FROM bairros b LEFT JOIN hospitais h ON b.nome = h.nome_bairro " +
                "GROUP BY b.nome, b.x_pct, b.y_pct";

        Cursor cBairros = db.rawQuery(queryBairros, null);
        if (cBairros.moveToFirst()) {
            do {
                String nome = cBairros.getString(0);
                float x = cBairros.getFloat(1);
                float y = cBairros.getFloat(2);
                int total = cBairros.getInt(3);
                int ocup = cBairros.getInt(4);

                // Envia pro motor visual e pra lista do Spinner
                grafoView.adicionarNo(nome, x, y, total, ocup);
                bairros.add(nome);
            } while (cBairros.moveToNext());
        }
        cBairros.close();

        // Puxa as conexões (Arestas)
        Cursor cAdj = db.rawQuery("SELECT DISTINCT bairro_origem, bairro_destino FROM adjacencias_grafo WHERE bairro_origem < bairro_destino", null);
        if (cAdj.moveToFirst()) {
            do {
                grafoView.conectar(cAdj.getString(0), cAdj.getString(1));
            } while (cAdj.moveToNext());
        }
        cAdj.close();
        db.close();

        grafoView.setBfsListener(new GrafoView.BfsListener() {
            @Override
            public void onLogLine(String line, int tipo) {
            }

            @Override
            public void onResultado(String nome, String bairro, int vagas, int dist, boolean contingencia) {
                runOnUiThread(() -> mostrarResultado(nome, vagas, dist, contingencia));
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
