package com.example.ambulncia_atividade;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

import com.example.ambulncia_atividade.domain.AmbulanciaAtendimento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // ========== Backend ==========
    private AmbulanciaAtendimento sistema;

    // ========== Dados de exibição ==========
    // bairro -> lista de strings descritivas dos hospitais
    private final Map<String, List<String[]>> dadosHospitais = new LinkedHashMap<>();
    // bairro -> bairros vizinhos
    private final Map<String, List<String>> dadosGrafo = new LinkedHashMap<>();

    // ========== Views ==========
    private Spinner spinnerBairro;
    private Button btnBuscar;
    private TextView tvTotalHospitais;
    private LinearLayout containerHospitais;
    private LinearLayout cardResultado;
    private LinearLayout cardContingencia;
    private TextView tvNomeHospital;
    private TextView tvBairroHospital;
    private TextView tvVagasLivres;
    private TextView tvDistancia;
    private TextView tvNomeContingencia;
    private TextView tvLog;

    private String bairroSelecionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarViews();
        inicializarDados();
        popularInterface();
        configurarListeners();
    }

    // =========================================================
    //  INICIALIZAÇÃO
    // =========================================================

    private void inicializarViews() {
        spinnerBairro   = findViewById(R.id.spinnerBairro);
        btnBuscar       = findViewById(R.id.btnBuscar);
        tvTotalHospitais = findViewById(R.id.tvTotalHospitais);
        containerHospitais = findViewById(R.id.containerHospitais);
        cardResultado   = findViewById(R.id.cardResultado);
        cardContingencia = findViewById(R.id.cardContingencia);
        tvNomeHospital  = findViewById(R.id.tvNomeHospital);
        tvBairroHospital = findViewById(R.id.tvBairroHospital);
        tvVagasLivres   = findViewById(R.id.tvVagasLivres);
        tvDistancia     = findViewById(R.id.tvDistancia);
        tvNomeContingencia = findViewById(R.id.tvNomeContingencia);
        tvLog           = findViewById(R.id.tvLog);
    }

    /**
     * Monta o grafo de São Paulo - Zona Oeste e adjacências.
     * Dados baseados em bairros reais. Hospitais com capacidades fictícias para demo.
     *
     * String[] = { nome, total, ocupados }
     */
    private void inicializarDados() {
        // 1. Instancia o sistema passando o contexto da Activity (necessário para o SQLite)
        sistema = new AmbulanciaAtendimento(this);

        // 2. Abre a ligação com a Base de Dados em modo de leitura
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 3. Limpa os mapas para evitar duplicidade caso o método seja chamado novamente
        dadosGrafo.clear();
        dadosHospitais.clear();

        // 4. Carrega os Bairros para o Spinner e para a estrutura da UI
        Cursor cBairros = db.rawQuery("SELECT nome FROM bairros", null);
        if (cBairros.moveToFirst()) {
            do {
                String b = cBairros.getString(0);
                dadosGrafo.put(b, new ArrayList<>());
                dadosHospitais.put(b, new ArrayList<>());
            } while (cBairros.moveToNext());
        }
        cBairros.close();

        // 5. Carrega as Conexões (Adjacências) do grafo para a interface
        Cursor cAdj = db.rawQuery("SELECT bairro_origem, bairro_destino FROM adjacencias_grafo", null);
        if (cAdj.moveToFirst()) {
            do {
                String origem = cAdj.getString(0);
                String destino = cAdj.getString(1);
                if (dadosGrafo.containsKey(origem)) {
                    dadosGrafo.get(origem).add(destino);
                }
            } while (cAdj.moveToNext());
        }
        cAdj.close();

        // 6. Carrega os Hospitais para os cards da interface (Nome, Bairro, Total e Ocupados)
        Cursor cHosp = db.rawQuery("SELECT nome, nome_bairro, vagas_totais, vagas_ocupadas FROM hospitais", null);
        if (cHosp.moveToFirst()) {
            do {
                String nome = cHosp.getString(0);
                String bairro = cHosp.getString(1);
                String total = String.valueOf(cHosp.getInt(2));
                String ocupados = String.valueOf(cHosp.getInt(3));

                if (dadosHospitais.containsKey(bairro)) {
                    dadosHospitais.get(bairro).add(new String[]{nome, total, ocupados});
                }
            } while (cHosp.moveToNext());
        }
        cHosp.close();
        db.close(); // Fecha a conexão para liberar memória
    }

    // =========================================================
    //  INTERFACE
    // =========================================================

    private void popularInterface() {
        // Spinner com bairros
        List<String> bairros = new ArrayList<>(dadosGrafo.keySet());

        if (!bairros.isEmpty()) {
            bairroSelecionado = bairros.get(0);
        } else {
            bairros.add("Nenhum bairro cadastrado");
            bairroSelecionado = "Nenhum bairro cadastrado";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bairros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBairro.setAdapter(adapter);
        bairroSelecionado = bairros.get(0);

        // Contador total de hospitais
        int total = 0;
        for (List<String[]> lista : dadosHospitais.values()) total += lista.size();
        tvTotalHospitais.setText(total + " unidades");

        // Cards dos bairros
        renderizarCardsBairros();
    }

    private void renderizarCardsBairros() {
        containerHospitais.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Map.Entry<String, List<String[]>> entry : dadosHospitais.entrySet()) {
            String bairro = entry.getKey();
            List<String[]> hospitais = entry.getValue();

            View cardView = inflater.inflate(R.layout.item_bairro, containerHospitais, false);
            TextView tvBairroCard = cardView.findViewById(R.id.tvBairroCard);
            TextView tvConexoes = cardView.findViewById(R.id.tvConexoes);
            LinearLayout containerH = cardView.findViewById(R.id.containerHospitaisBairro);

            tvBairroCard.setText(bairro);
            List<String> vizinhos = dadosGrafo.get(bairro);
            int nVizinhos = vizinhos != null ? vizinhos.size() : 0;
            tvConexoes.setText(nVizinhos + " conexões");

            // Hospitalzinhos dentro do card
            for (String[] h : hospitais) {
                String nome = h[0];
                int totalLeitos = Integer.parseInt(h[1]);
                int ocupados = Integer.parseInt(h[2]);
                int livres = totalLeitos - ocupados;

                View itemView = inflater.inflate(R.layout.item_hospital, containerH, false);
                TextView tvNomeH = itemView.findViewById(R.id.tvNomeHospitalItem);
                TextView tvOcupados = itemView.findViewById(R.id.tvOcupados);
                TextView tvTotal = itemView.findViewById(R.id.tvTotalItem);
                TextView tvVagas = itemView.findViewById(R.id.tvVagasBadge);
                View indicador = itemView.findViewById(R.id.indicadorOcupacao);

                tvNomeH.setText(nome);
                tvOcupados.setText(String.valueOf(ocupados));
                tvTotal.setText(String.valueOf(totalLeitos));
                tvVagas.setText(String.valueOf(livres));

                // Cor do indicador: verde=vaga, amarelo=quase, vermelho=lotado
                double taxa = totalLeitos > 0 ? (double) ocupados / totalLeitos : 1.0;
                if (taxa >= 1.0) {
                    indicador.setBackgroundColor(Color.parseColor("#FFD32F2F"));
                    tvVagas.setTextColor(Color.parseColor("#FFFF5555"));
                } else if (taxa >= 0.85) {
                    indicador.setBackgroundColor(Color.parseColor("#FFFFCA28"));
                    tvVagas.setTextColor(Color.parseColor("#FFFFCA28"));
                } else {
                    indicador.setBackgroundColor(Color.parseColor("#FF4CAF50"));
                    tvVagas.setTextColor(Color.parseColor("#FF4CAF50"));
                }

                containerH.addView(itemView);
            }

            containerHospitais.addView(cardView);
        }
    }

    // =========================================================
    //  LISTENERS
    // =========================================================

    private void configurarListeners() {
        spinnerBairro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bairroSelecionado = (String) parent.getItemAtPosition(position);
                // Esconde resultado anterior ao mudar de bairro
                cardResultado.setVisibility(View.GONE);
                cardContingencia.setVisibility(View.GONE);
                tvLog.setText("Aguardando busca...");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnBuscar.setOnClickListener(v -> executarBusca());
    }

    // =========================================================
    //  BUSCA BFS COM LOG
    // =========================================================

    private void executarBusca() {
        if (bairroSelecionado == null) return;

        cardResultado.setVisibility(View.GONE);
        cardContingencia.setVisibility(View.GONE);

        StringBuilder log = new StringBuilder();
        log.append("▶ Iniciando BFS a partir de: ").append(bairroSelecionado).append("\n");
        log.append("─────────────────────────────\n");

        // Executa BFS manualmente para gerar log detalhado
        ResultadoBusca resultado = executarBfsComLog(bairroSelecionado, log);

        tvLog.setText(log.toString());

        if (resultado != null) {
            if (resultado.isContingencia) {
                // Plano de contingência
                tvNomeContingencia.setText(resultado.nomeHospital);
                cardContingencia.setVisibility(View.VISIBLE);
                log.append("\n⚠️ PLANO DE CONTINGÊNCIA ATIVADO\n");
                log.append("→ Menos sobrecarregado: ").append(resultado.nomeHospital);
            } else {
                // Resultado normal
                tvNomeHospital.setText(resultado.nomeHospital);
                tvBairroHospital.setText("📍 " + resultado.bairro);
                tvVagasLivres.setText(String.valueOf(resultado.vagasLivres));
                tvDistancia.setText(String.valueOf(resultado.distancia));
                cardResultado.setVisibility(View.VISIBLE);
                log.append("\n✅ DESTINO DEFINIDO: ").append(resultado.nomeHospital);
            }
        } else {
            log.append("\n❌ Nenhum hospital encontrado.");
        }

        tvLog.setText(log.toString());
    }

    /**
     * Reimplementa a BFS com log detalhado para exibição no frontend.
     * Espelha a lógica de AmbulanciaAtendimento.encontrarHospital().
     */
    private ResultadoBusca executarBfsComLog(String bairroOrigem, StringBuilder log) {
        int MAX_DISTANCIA = 3;

        java.util.Queue<String> fila = new java.util.LinkedList<>();
        java.util.Set<String> visitados = new java.util.HashSet<>();
        List<HospitalSimples> todosAnalisados = new ArrayList<>();

        fila.add(bairroOrigem);
        visitados.add(bairroOrigem);

        for (int nivel = 0; nivel <= MAX_DISTANCIA; nivel++) {
            int tamanhoFila = fila.size();
            HospitalSimples melhorNivel = null;

            log.append("\n[Nível ").append(nivel).append("] Analisando ").append(tamanhoFila).append(" bairro(s):\n");

            for (int i = 0; i < tamanhoFila; i++) {
                String atual = fila.poll();
                log.append("  → ").append(atual).append("\n");

                List<String[]> hospitaisDoBairro = dadosHospitais.get(atual);
                if (hospitaisDoBairro != null) {
                    for (String[] h : hospitaisDoBairro) {
                        int total = Integer.parseInt(h[1]);
                        int ocupados = Integer.parseInt(h[2]);
                        int livres = total - ocupados;
                        double taxa = total > 0 ? (double) ocupados / total : 1.0;

                        HospitalSimples hs = new HospitalSimples(h[0], atual, total, ocupados, nivel);
                        todosAnalisados.add(hs);

                        String status = livres > 0 ? "✅ " + livres + " livre(s)" : "❌ LOTADO";
                        log.append("     ").append(h[0]).append(": ").append(status).append("\n");

                        if (livres > 0) {
                            if (melhorNivel == null || livres > (melhorNivel.total - melhorNivel.ocupados)) {
                                melhorNivel = hs;
                            }
                        }
                    }
                }

                // Adiciona vizinhos
                List<String> vizinhos = dadosGrafo.get(atual);
                if (vizinhos != null) {
                    for (String v : vizinhos) {
                        if (!visitados.contains(v)) {
                            visitados.add(v);
                            fila.add(v);
                        }
                    }
                }
            }

            if (melhorNivel != null) {
                log.append("\n✅ Vaga encontrada no Nível ").append(nivel).append("!\n");
                ResultadoBusca r = new ResultadoBusca();
                r.nomeHospital = melhorNivel.nome;
                r.bairro = melhorNivel.bairro;
                r.vagasLivres = melhorNivel.total - melhorNivel.ocupados;
                r.distancia = nivel;
                r.isContingencia = false;
                return r;
            } else {
                log.append("  ✖ Sem vagas neste nível. Expandindo...\n");
            }
        }

        // Contingência: menor taxa de ocupação
        log.append("\n⚠️ Limite de ").append(MAX_DISTANCIA).append(" bairros atingido.\n");
        log.append("Selecionando hospital menos sobrecarregado...\n");

        HospitalSimples menosLotado = null;
        for (HospitalSimples h : todosAnalisados) {
            double taxa = h.total > 0 ? (double) h.ocupados / h.total : 1.0;
            if (menosLotado == null) {
                menosLotado = h;
            } else {
                double taxaMenos = menosLotado.total > 0 ? (double) menosLotado.ocupados / menosLotado.total : 1.0;
                if (taxa < taxaMenos) menosLotado = h;
            }
        }

        if (menosLotado != null) {
            double taxa = menosLotado.total > 0 ? (double) menosLotado.ocupados / menosLotado.total * 100 : 100;
            log.append("→ ").append(menosLotado.nome)
               .append(" (").append(String.format("%.0f", taxa)).append("% ocupado)\n");

            ResultadoBusca r = new ResultadoBusca();
            r.nomeHospital = menosLotado.nome;
            r.bairro = menosLotado.bairro;
            r.isContingencia = true;
            return r;
        }

        return null;
    }

    // =========================================================
    //  CLASSES DE APOIO INTERNAS
    // =========================================================

    static class HospitalSimples {
        String nome, bairro;
        int total, ocupados, nivel;

        HospitalSimples(String nome, String bairro, int total, int ocupados, int nivel) {
            this.nome = nome;
            this.bairro = bairro;
            this.total = total;
            this.ocupados = ocupados;
            this.nivel = nivel;
        }
    }

    static class ResultadoBusca {
        String nomeHospital;
        String bairro;
        int vagasLivres;
        int distancia;
        boolean isContingencia;
    }
}
