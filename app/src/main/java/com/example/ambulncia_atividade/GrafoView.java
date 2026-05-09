package com.example.ambulncia_atividade;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ambulncia_atividade.domain.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GrafoView extends View {

    // ── Dados do grafo ──────────────────────────────────────────────
    public static class No {
        String id;
        float xPct, yPct;   // posição em % do canvas (0–1)
        int totalLeitos, leitosOcupados;
        float x, y;          // posição calculada em px

        No(String id, float xPct, float yPct, int total, int ocupados) {
            this.id = id; this.xPct = xPct; this.yPct = yPct;
            this.totalLeitos = total; this.leitosOcupados = ocupados;
        }

        int vagasLivres() { return Math.max(0, totalLeitos - leitosOcupados); }
        float taxaOcupacao() { return totalLeitos > 0 ? (float) leitosOcupados / totalLeitos : 1f; }

        int cor() {
            float t = taxaOcupacao();
            if (t >= 1f)    return Color.parseColor("#D32F2F");
            if (t >= 0.85f) return Color.parseColor("#FFCA28");
            return Color.parseColor("#4CAF50");
        }
    }

    public interface BfsListener {
        void onLogLine(String line, int tipo); // 0=dim, 1=ok, 2=warn, 3=err
        void onResultado(String nomeHospital, String bairro, int vagas, int dist, boolean contingencia);
    }

    // ── Estado ───────────────────────────────────────────────────────
    private final List<No> nos = new ArrayList<>();
    private final List<int[]> arestas = new ArrayList<>();  // índices
    private final Map<String, Integer> idxMap = new HashMap<>();
    private final Map<String, List<String>> adj = new HashMap<>();

    private final Set<Integer> visitadosNos = new HashSet<>();
    private final Set<String> visitadosArestas = new HashSet<>();
    private final Set<String> caminhoArestas = new HashSet<>();
    private int origemIdx = -1;
    private int destinoIdx = -1;

    private BfsListener listener;
    private String bairroSelecionado = null;

    // ── Paints ───────────────────────────────────────────────────────
    private final Paint pArestaInativa  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pArestaVisitada = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pArestaCaminho  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pNoFundo        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pNoBorda        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTextoNo        = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTextoLabel     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTextoBadge     = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float noRaio = 22f;

    // ── Animação ──────────────────────────────────────────────────────
    private final List<Runnable> passos = new ArrayList<>();
    private int passoAtual = 0;
    private final android.os.Handler handler = new android.os.Handler();

    public GrafoView(Context ctx) { super(ctx); init(); }
    public GrafoView(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public GrafoView(Context ctx, AttributeSet a, int s) { super(ctx, a, s); init(); }

    private void init() {
        pArestaInativa.setColor(Color.parseColor("#1e2a3a"));
        pArestaInativa.setStrokeWidth(2f);
        pArestaInativa.setStyle(Paint.Style.STROKE);

        pArestaVisitada.setColor(Color.parseColor("#42A5F5"));
        pArestaVisitada.setStrokeWidth(2.5f);
        pArestaVisitada.setStyle(Paint.Style.STROKE);

        pArestaCaminho.setColor(Color.WHITE);
        pArestaCaminho.setStrokeWidth(4f);
        pArestaCaminho.setStyle(Paint.Style.STROKE);

        pNoFundo.setStyle(Paint.Style.FILL);
        pNoBorda.setStyle(Paint.Style.STROKE);
        pNoBorda.setStrokeWidth(2.5f);

        pTextoNo.setColor(Color.WHITE);
        pTextoNo.setTextAlign(Paint.Align.CENTER);
        pTextoNo.setFakeBoldText(true);

        pTextoLabel.setColor(Color.parseColor("#AAAAAA"));
        pTextoLabel.setTextAlign(Paint.Align.CENTER);

        pTextoBadge.setColor(Color.parseColor("#111111"));
        pTextoBadge.setTextAlign(Paint.Align.CENTER);
        pTextoBadge.setFakeBoldText(true);
    }

    // ── API pública ──────────────────────────────────────────────────

    public void setBfsListener(BfsListener l) { this.listener = l; }

    public void adicionarNo(String id, float xPct, float yPct, int total, int ocup) {
        int idx = nos.size();
        nos.add(new No(id, xPct, yPct, total, ocup));
        idxMap.put(id, idx);
        adj.put(id, new ArrayList<>());
    }

    public void conectar(String a, String b) {
        int ia = idxMap.get(a), ib = idxMap.get(b);
        arestas.add(new int[]{ia, ib});
        adj.get(a).add(b);
        adj.get(b).add(a);
    }

    public void setBairroSelecionado(String id) {
        bairroSelecionado = id;
        resetar();
        origemIdx = idxMap.containsKey(id) ? idxMap.get(id) : -1;
        invalidate();
    }

    public void resetar() {
        handler.removeCallbacksAndMessages(null);
        visitadosNos.clear();
        visitadosArestas.clear();
        caminhoArestas.clear();
        origemIdx = -1;
        destinoIdx = -1;
        passos.clear();
        passoAtual = 0;
        invalidate();
    }

    public void iniciarBfs() {
        if (bairroSelecionado == null || !idxMap.containsKey(bairroSelecionado)) return;
        resetar();
        origemIdx = idxMap.get(bairroSelecionado);
        construirPassosBfs(bairroSelecionado);
        executarProximoPasso();
    }

    // ── BFS com passos ───────────────────────────────────────────────

    private void construirPassosBfs(String inicio) {
        int MAX_DIST = 3;
        Queue<String> fila = new LinkedList<>();
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> pai = new HashMap<>();
        Set<String> vis = new HashSet<>();

        fila.add(inicio);
        vis.add(inicio);
        dist.put(inicio, 0);

        emit("▶ BFS iniciado em: " + inicio, 1);

        List<No> todosAnalisados = new ArrayList<>();
        final String[] encontrado = {null};

        while (!fila.isEmpty()) {
            String cur = fila.poll();
            int d = dist.get(cur);
            if (d > MAX_DIST) continue;

            No n = nos.get(idxMap.get(cur));
            todosAnalisados.add(n);
            final String curF = cur;
            final int dF = d;
            final int vagasF = n.vagasLivres();

            passos.add(() -> {
                visitadosNos.add(idxMap.get(curF));
                String status = vagasF > 0 ? vagasF + " vaga(s)" : "LOTADO";
                int tipo = vagasF > 0 ? 1 : 3;
                emit("  [d=" + dF + "] " + curF + ": " + status, tipo);
                invalidate();
            });

            if (vagasF > 0 && encontrado[0] == null) {
                encontrado[0] = cur;
            }

            if (d < MAX_DIST) {
                for (String viz : adj.get(cur)) {
                    if (!vis.contains(viz)) {
                        vis.add(viz);
                        dist.put(viz, d + 1);
                        pai.put(viz, cur);
                        fila.add(viz);
                        final String vizF = viz, curF2 = cur;
                        passos.add(() -> {
                            visitadosArestas.add(arestaKey(curF2, vizF));
                            invalidate();
                        });
                    }
                }
            }
        }

        if (encontrado[0] != null) {
            final String destF = encontrado[0];
            final int distF = dist.get(destF);
            List<String> caminho = new ArrayList<>();
            String c = destF;
            while (c != null) { caminho.add(0, c); c = pai.get(c); }

            passos.add(() -> {
                destinoIdx = idxMap.get(destF);
                for (int i = 0; i < caminho.size() - 1; i++)
                    caminhoArestas.add(arestaKey(caminho.get(i), caminho.get(i + 1)));
                No nd = nos.get(destinoIdx);
                emit("", 0);
                emit("✅ Destino: " + destF + " (" + nd.vagasLivres() + " vagas, dist=" + distF + ")", 1);
                if (listener != null)
                    listener.onResultado(destF, destF, nd.vagasLivres(), distF, false);
                invalidate();
            });
        } else {
            No melhor = todosAnalisados.isEmpty() ? null :
                todosAnalisados.stream().min((a, b) ->
                    Float.compare(a.taxaOcupacao(), b.taxaOcupacao())).orElse(null);
            if (melhor != null) {
                final No mF = melhor;
                passos.add(() -> {
                    destinoIdx = idxMap.get(mF.id);
                    int taxa = Math.round(mF.taxaOcupacao() * 100);
                    emit("", 0);
                    emit("⚠️ Contingência: " + mF.id + " (" + taxa + "% ocupado)", 2);
                    if (listener != null)
                        listener.onResultado(mF.id, mF.id, mF.vagasLivres(), -1, true);
                    invalidate();
                });
            }
        }
    }

    private void executarProximoPasso() {
        if (passoAtual >= passos.size()) return;
        passos.get(passoAtual++).run();
        long delay = passoAtual > 0 && passoAtual <= passos.size() ? 350 : 100;
        handler.postDelayed(this::executarProximoPasso, delay);
    }

    private void emit(String msg, int tipo) {
        if (listener != null) listener.onLogLine(msg, tipo);
    }

    private String arestaKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    // ── Touch ────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        float tx = e.getX(), ty = e.getY();
        for (No n : nos) {
            float dx = tx - n.x, dy = ty - n.y;
            if (dx * dx + dy * dy <= (noRaio + 10) * (noRaio + 10)) {
                setBairroSelecionado(n.id);
                if (listener != null) listener.onLogLine("Bairro selecionado: " + n.id, 0);
                return true;
            }
        }
        return true;
    }

    // ── Draw ─────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        float pad = noRaio + 4;
        for (No n : nos) {
            n.x = pad + n.xPct * (w - 2 * pad);
            n.y = pad + n.yPct * (h - 2 * pad);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#0d1117"));

        float ts = getWidth() / 400f;
        noRaio = 20f * ts;
        pTextoNo.setTextSize(11f * ts);
        pTextoLabel.setTextSize(9f * ts);
        pTextoBadge.setTextSize(10f * ts);
        pArestaInativa.setStrokeWidth(1.5f * ts);
        pArestaVisitada.setStrokeWidth(2.5f * ts);
        pArestaCaminho.setStrokeWidth(4f * ts);
        pNoBorda.setStrokeWidth(2f * ts);

        // Arestas
        for (int[] e : arestas) {
            No na = nos.get(e[0]), nb = nos.get(e[1]);
            String key = arestaKey(na.id, nb.id);
            Paint p = caminhoArestas.contains(key) ? pArestaCaminho :
                      visitadosArestas.contains(key) ? pArestaVisitada : pArestaInativa;
            canvas.drawLine(na.x, na.y, nb.x, nb.y, p);
        }

        // Nós
        for (int i = 0; i < nos.size(); i++) {
            No n = nos.get(i);
            boolean isOrigem  = (i == origemIdx);
            boolean isDestino = (i == destinoIdx);
            boolean isVis     = visitadosNos.contains(i);

            float r = (isOrigem || isDestino) ? noRaio * 1.25f : noRaio;

            // Fundo
            if (isOrigem) {
                pNoFundo.setColor(Color.WHITE);
            } else if (isDestino) {
                pNoFundo.setColor(Color.parseColor("#2E7D32"));
            } else if (isVis) {
                pNoFundo.setColor(n.cor());
            } else {
                pNoFundo.setColor(Color.parseColor("#1e2a3a"));
            }
            canvas.drawCircle(n.x, n.y, r, pNoFundo);

            // Borda
            pNoBorda.setColor(isOrigem ? Color.parseColor("#AAAAAA") :
                              isDestino ? Color.parseColor("#81C784") :
                              isVis ? n.cor() : Color.parseColor("#2a3a4a"));
            canvas.drawCircle(n.x, n.y, r, pNoBorda);

            // Texto dentro do nó
            if (isVis || isOrigem || isDestino) {
                pTextoNo.setColor(isOrigem ? Color.BLACK : Color.WHITE);
                String txt = isOrigem ? "AQUI" : (n.vagasLivres() + "v");
                canvas.drawText(txt, n.x, n.y + pTextoNo.getTextSize() * 0.4f, pTextoNo);
            } else {
                pTextoNo.setColor(Color.parseColor("#445566"));
                String short_ = n.id.length() > 4 ? n.id.substring(0, 3) : n.id;
                canvas.drawText(short_, n.x, n.y + pTextoNo.getTextSize() * 0.4f, pTextoNo);
            }

            // Label abaixo
            String[] palavras = n.id.split(" ");
            float ly = n.y + r + pTextoLabel.getTextSize() + 2;
            pTextoLabel.setColor((isVis || isOrigem || isDestino) ?
                Color.parseColor("#CCCCCC") : Color.parseColor("#445566"));
            if (palavras.length <= 2) {
                canvas.drawText(n.id, n.x, ly, pTextoLabel);
            } else {
                canvas.drawText(palavras[0] + " " + palavras[1], n.x, ly, pTextoLabel);
                canvas.drawText(palavras[2], n.x, ly + pTextoLabel.getTextSize() + 1, pTextoLabel);
            }
        }
    }

    public static class CadastroPacienteActivity extends AppCompatActivity {

        private EditText etNome, etEmail, etSenha, etRg, etSangue, etAlergias;
        private Button btnCadastrar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_cadastro_paciente);

            etNome = findViewById(R.id.etCadNome);
            etEmail = findViewById(R.id.etCadEmail);
            etSenha = findViewById(R.id.etCadSenha);
            etRg = findViewById(R.id.etCadRg);
            etSangue = findViewById(R.id.etCadSangue);
            etAlergias = findViewById(R.id.etCadAlergias);
            btnCadastrar = findViewById(R.id.btnFinalizarCadastro);

            btnCadastrar.setOnClickListener(v -> processarCadastro());
        }

        private void processarCadastro() {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();
            String rg = etRg.getText().toString().trim();
            String sangue = etSangue.getText().toString().trim();
            String alergias = etAlergias.getText().toString().trim();

            if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) || TextUtils.isEmpty(rg)) {
                Toast.makeText(this, "Preencha os campos obrigatórios (Nome, Email, Senha e RG).", Toast.LENGTH_SHORT).show();
                return;
            }

            if (alergias.isEmpty()) alergias = "Nenhuma";

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            try {
                db.beginTransaction(); // Inicia transação segura para as duas tabelas

                // 1. Cria credencial de Login
                ContentValues userValues = new ContentValues();
                userValues.put("email", email);
                userValues.put("senha_hash", senha);
                userValues.put("role", "PACIENTE");
                long userId = db.insert("usuarios", null, userValues);

                if (userId == -1) {
                    Toast.makeText(this, "E-mail já cadastrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Cria a Ficha Médica amarrada ao Login
                ContentValues pacienteValues = new ContentValues();
                pacienteValues.put("id_usuario", userId);
                pacienteValues.put("nome_completo", nome);
                pacienteValues.put("rg", rg);
                pacienteValues.put("tipo_sanguineo", sangue.toUpperCase());
                pacienteValues.put("alergias", alergias);
                long pacienteId = db.insert("pacientes", null, pacienteValues);

                if (pacienteId == -1) {
                    Toast.makeText(this, "RG já cadastrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.setTransactionSuccessful(); // Tudo certo! Confirma a gravação

                // Salva a sessão e joga direto pro Perfil
                salvarSessao(email, nome);
                Intent intent = new Intent(this, PerfilActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } catch (Exception e) {
                Toast.makeText(this, "Erro no banco de dados.", Toast.LENGTH_SHORT).show();
            } finally {
                db.endTransaction();
                db.close();
            }
        }

        private void salvarSessao(String email, String nome) {
            SharedPreferences prefs = getSharedPreferences("sos_leitos_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("email", email)
                    .putString("perfil", "PACIENTE")
                    .putString("nome", nome)
                    .putBoolean("logado", true)
                    .apply();
        }
    }

    public static class TriagemFragment extends Fragment {

        private EditText etBuscaRg;
        private Button btnBuscarRg, btnIrParaMapa;
        private LinearLayout cardFichaPaciente, wrapperHistorico;
        private TextView tvNomePaciente, tvSangue, tvAlergias, alertaRestricoes;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // reaproveitando o layout velho pra n ter q codar XML de novo
            View view = inflater.inflate(R.layout.activity_triagem, container, false);

            initViews(view);

            btnBuscarRg.setOnClickListener(v -> pesquisarNaBase());

            // Ao inves de abrir tela solta, avisa a MainActivity pra trocar a aba la embaixo
            btnIrParaMapa.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).pularParaMapa();
                }
            });

            return view;
        }

        private void initViews(View view) {
            etBuscaRg = view.findViewById(R.id.etBuscaRg);
            btnBuscarRg = view.findViewById(R.id.btnBuscarRg);
            btnIrParaMapa = view.findViewById(R.id.btnIrParaMapa);
            cardFichaPaciente = view.findViewById(R.id.cardFichaPaciente);
            tvNomePaciente = view.findViewById(R.id.tvNomePacienteTriagem);
            tvSangue = view.findViewById(R.id.tvSangueTriagem);
            tvAlergias = view.findViewById(R.id.tvAlergiasTriagem);
            alertaRestricoes = view.findViewById(R.id.alertaAlergia);

            wrapperHistorico = new LinearLayout(getContext());
            wrapperHistorico.setOrientation(LinearLayout.VERTICAL);
            wrapperHistorico.setPadding(0, 32, 0, 0);
            cardFichaPaciente.addView(wrapperHistorico);

            cardFichaPaciente.setVisibility(GONE);
        }

        private void pesquisarNaBase() {
            String numRg = etBuscaRg.getText().toString().trim();

            if (numRg.isEmpty()) {
                etBuscaRg.setError("Mano, digita o RG!");
                return;
            }

            DatabaseHelper db = new DatabaseHelper(getContext());
            SQLiteDatabase sql = db.getReadableDatabase();
            Cursor c = null;

            try {
                c = sql.rawQuery("SELECT nome_completo, tipo_sanguineo, alergias FROM pacientes WHERE rg = ?", new String[]{numRg});

                if (c.moveToFirst()) {
                    populaProntuario(c);
                    montarListaHistorico(sql, numRg);
                } else {
                    Toast.makeText(getContext(), "Registro fantasma (não achou ninguem)", Toast.LENGTH_SHORT).show();
                    cardFichaPaciente.setVisibility(GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Deu ruim na query do SQLite :(", Toast.LENGTH_SHORT).show();
            } finally {
                if (c != null) c.close();
                sql.close();
            }
        }

        private void populaProntuario(Cursor c) {
            String nome = c.getString(0);
            String sang = c.getString(1);
            String alerg = c.getString(2);

            tvNomePaciente.setText("Paciente: " + (nome != null ? nome : "-"));
            tvSangue.setText("Sangue: " + (sang != null ? sang : "-"));

            // se o cara tiver alergia berrante a gente tem q trocar a cor do card pra avisar o socorrista
            if (alerg != null && !alerg.trim().isEmpty() && !alerg.equalsIgnoreCase("nenhuma")) {
                tvAlergias.setText("ALERGIAS: " + alerg);
                alertaRestricoes.setText("⚠️ ALERTA VERMELHO: CHOQUE ANAFILÁTICO POSSÍVEL");
                alertaRestricoes.setVisibility(VISIBLE);
                cardFichaPaciente.setBackgroundColor(Color.parseColor("#3E1A1A"));
            } else {
                tvAlergias.setText("Ficha limpa de alergias");
                alertaRestricoes.setVisibility(GONE);
                cardFichaPaciente.setBackgroundColor(Color.parseColor("#1E1E1E"));
            }

            cardFichaPaciente.setVisibility(VISIBLE);
        }

        private void montarListaHistorico(SQLiteDatabase db, String rgTarget) {
            wrapperHistorico.removeAllViews();

            TextView t = new TextView(getContext());
            t.setText("ÚLTIMOS ENCAMINHAMENTOS");
            t.setTextColor(Color.parseColor("#bfecff"));
            t.setTextSize(12f);
            t.setTypeface(null, android.graphics.Typeface.BOLD);
            t.setPadding(0, 0, 0, 16);
            wrapperHistorico.addView(t);

            Cursor h = db.rawQuery("SELECT hospital, data_registro FROM historico WHERE rg_paciente = ? ORDER BY id DESC", new String[]{rgTarget});

            if (h.moveToFirst()) {
                do {
                    TextView item = new TextView(getContext());
                    item.setText("• " + h.getString(1) + "  |  " + h.getString(0));
                    item.setTextColor(Color.WHITE);
                    item.setPadding(0, 0, 0, 8);
                    wrapperHistorico.addView(item);
                } while (h.moveToNext());
            } else {
                TextView none = new TextView(getContext());
                none.setText("Paciente zerado na base (sem historicos passados)");
                none.setTextColor(Color.GRAY);
                none.setTypeface(null, android.graphics.Typeface.ITALIC);
                wrapperHistorico.addView(none);
            }
            h.close();
        }
    }
}
