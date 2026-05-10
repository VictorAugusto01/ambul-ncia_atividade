package com.example.ambulncia_atividade;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.AdjacenciaGrafo;
import com.example.ambulncia_atividade.domain.database.entity.BairroStatus;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GrafoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Spinner spinnerBairro;
    private Button btnBuscar, btnVoltar, btnMeuLocal;
    private LinearLayout logContainer, cardResultado;
    private ScrollView logScroll;
    private TextView tvNomeResultado, tvVagasResultado, tvDistResultado;

    private FusedLocationProviderClient fusedClient;

    // cache do grafo em memoria
    private final List<String> listaBairros = new ArrayList<>();
    private final Map<String, LatLng> coords = new HashMap<>();
    private final Map<String, List<String>> adj = new HashMap<>();
    private final Map<String, Integer> vagasLivres = new HashMap<>();
    private final List<Polyline> arestasAtuais = new ArrayList<>();

    // gambiarra pra n limpar o pino da ambulancia qd o mapa recarregar a busca
    private LatLng localRealAmbulancia = null;

    private static final int REQ_GPS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafo);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        initViews();
        carregarDB();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaBairros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBairro.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapaGoogle);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void initViews() {
        spinnerBairro = findViewById(R.id.spinnerBairroGrafo);
        btnBuscar = findViewById(R.id.btnBuscarGrafo);
        btnVoltar = findViewById(R.id.btnVoltarGrafo);
        btnMeuLocal = findViewById(R.id.btnMeuLocal);
        logContainer = findViewById(R.id.logContainerGrafo);
        logScroll = findViewById(R.id.logScrollGrafo);
        cardResultado = findViewById(R.id.cardResultadoGrafo);
        tvNomeResultado = findViewById(R.id.tvNomeResultadoGrafo);
        tvVagasResultado = findViewById(R.id.tvVagasResultadoGrafo);
        tvDistResultado = findViewById(R.id.tvDistResultadoGrafo);

        btnVoltar.setOnClickListener(v -> finish());

        btnBuscar.setOnClickListener(v -> {
            localRealAmbulancia = null; // reseta o gps manual
            dispararBFS();
        });

        btnMeuLocal.setOnClickListener(v -> pegarGPS());
    }

    private void carregarDB() {
        AppDatabase db = AppDatabase.getInstance(this);

        // TODO: no futuro puxar isso de uma API usando Retrofit, banco local n escala bem
        List<BairroStatus> statusBairros = db.grafoDao().getStatusBairros();
        if (statusBairros != null) {
            for (BairroStatus bs : statusBairros) {
                listaBairros.add(bs.nome);
                coords.put(bs.nome, new LatLng(bs.lat, bs.lng));
                vagasLivres.put(bs.nome, (bs.total - bs.ocupadas));
                adj.put(bs.nome, new ArrayList<>());
            }
        }

        List<AdjacenciaGrafo> adjacencias = db.grafoDao().getTodasAdjacencias();
        if (adjacencias != null) {
            for (AdjacenciaGrafo a : adjacencias) {
                if (adj.containsKey(a.bairroOrigem)) {
                    adj.get(a.bairroOrigem).add(a.bairroDestino);
                }
            }
        }
    }

    private void pegarGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_GPS);
            return;
        }

        Toast.makeText(this, "Localizando viatura...", Toast.LENGTH_SHORT).show();
        fusedClient.getLastLocation().addOnSuccessListener(this, loc -> {
            if (loc != null) {
                // Log.d("GPS", "Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude());
                acharVertice(loc.getLatitude(), loc.getLongitude());
            } else {
                Toast.makeText(this, "GPS falhou. Ative no emulador ou pare ao ar livre.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void acharVertice(double lat, double lng) {
        String proximo = null;
        float distMinima = Float.MAX_VALUE;

        // O(V) p/ achar o mais perto. Aceitavel pro tamanho do nosso BD
        for (String b : listaBairros) {
            float[] res = new float[1];
            Location.distanceBetween(lat, lng, coords.get(b).latitude, coords.get(b).longitude, res);
            if (res[0] < distMinima) {
                distMinima = res[0];
                proximo = b;
            }
        }

        if (proximo != null) {
            localRealAmbulancia = new LatLng(lat, lng);
            spinnerBairro.setSelection(listaBairros.indexOf(proximo));
            dispararBFS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_GPS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pegarGPS();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Magia acontecendo: injetando o JSON do modo noturno
        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e("MAPA_STYLE", "O parse do estilo falhou. Verifica se o JSON tá válido.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MAPA_STYLE", "Arquivo map_style.json não encontrado na pasta raw.", e);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.5505, -46.6333), 11f));
        resetMapa();
    }

    private void resetMapa() {
        if (mMap == null) return;
        mMap.clear();

        Set<String> renderizados = new HashSet<>();
        for (String origem : adj.keySet()) {
            for (String destino : adj.get(origem)) {
                String idKey = origem.compareTo(destino) < 0 ? origem + destino : destino + origem;
                if (!renderizados.contains(idKey)) {
                    renderizados.add(idKey);
                    mMap.addPolyline(new PolylineOptions().add(coords.get(origem), coords.get(destino)).width(5f).color(Color.parseColor("#445566")));
                }
            }
        }

        for (String b : listaBairros) {
            int qtd = vagasLivres.get(b);
            float corPino = qtd > 0 ? BitmapDescriptorFactory.HUE_CYAN : BitmapDescriptorFactory.HUE_RED;
            mMap.addMarker(new MarkerOptions().position(coords.get(b)).title(b).snippet(qtd > 0 ? qtd + " vagas" : "Lotado")
                    .icon(BitmapDescriptorFactory.defaultMarker(corPino)));
        }

        // devolve a ambulancia pra tela
        if (localRealAmbulancia != null) {
            mMap.addMarker(new MarkerOptions().position(localRealAmbulancia).title("📍 Ambulância").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).zIndex(200));
        }
    }

    // TODO: extrair isso dps pra um UseCase seguindo o Clean Architecture
    private void dispararBFS() {
        if (mMap == null || listaBairros.isEmpty()) return;

        String org = (String) spinnerBairro.getSelectedItem();
        logContainer.removeAllViews();
        resetMapa();

        escreverTerminal("▶ Iniciando BFS a partir de: <b>" + org + "</b>", "#FFFFFF");
        escreverTerminal("------------------------------------------------", "#445566");

        Queue<String> fila = new LinkedList<>();
        Set<String> vistos = new HashSet<>();
        Map<String, String> trackerPai = new HashMap<>();
        Map<String, Integer> niveis = new HashMap<>();

        fila.add(org);
        vistos.add(org);
        niveis.put(org, 0);

        String target = null;

        while (!fila.isEmpty()) {
            String curr = fila.poll();
            int nivelAtual = niveis.get(curr);

            escreverTerminal("<br>[Nível " + nivelAtual + "] Analisando: <b>" + curr + "</b>", "#AAAAAA");

            int livre = vagasLivres.get(curr);
            if (livre > 0) {
                escreverTerminal("  → Vagas: <font color='#bfecff'>☑ " + livre + " livre(s)</font>");
                target = curr;
                break;
            }

            escreverTerminal("  → Situação: <font color='#EF5350'>☒ LOTADO</font>");
            escreverTerminal("  <i>Varrendo vizinhos...</i>", "#888888");

            for (String viz : adj.get(curr)) {
                if (!vistos.contains(viz)) {
                    vistos.add(viz);
                    niveis.put(viz, nivelAtual + 1);
                    trackerPai.put(viz, curr);
                    fila.add(viz);
                }
            }
        }

        escreverTerminal("<br>------------------------------------------------", "#445566");

        if (target != null) {
            escreverTerminal("<font color='#bfecff'><b>☑ DESTINO CONFIRMADO: " + target + "</b></font>");
            tvNomeResultado.setText(target);
            tvVagasResultado.setText(vagasLivres.get(target) + " leitos livres");
            tvVagasResultado.setTextColor(Color.parseColor("#bfecff"));
            tvDistResultado.setText(niveis.get(target) + " bairros de distância");
            cardResultado.setVisibility(View.VISIBLE);

            pintarRota(target, trackerPai);
        } else {
            escreverTerminal("<font color='#FFCA28'><b>⚠️ CRÍTICO: Zero vagas na região.</b></font>");
        }
    }

    private void pintarRota(String dest, Map<String, String> pais) {
        String ptr = dest;
        List<LatLng> percurso = new ArrayList<>();

        while (ptr != null) {
            percurso.add(coords.get(ptr));
            ptr = pais.get(ptr);
        }

        // engata a linha no pino fisico do GPS
        if (localRealAmbulancia != null) percurso.add(localRealAmbulancia);

        mMap.addPolyline(new PolylineOptions().addAll(percurso).width(12f).color(Color.parseColor("#42A5F5")).zIndex(100));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(percurso.get(percurso.size() - 1), 12f));
    }

    // overload maroto pra poupar repeticao de cor cinza
    private void escreverTerminal(String txt) { escreverTerminal(txt, "#CCCCCC"); }

    private void escreverTerminal(String html, String hex) {
        TextView tv = new TextView(this);
        tv.setText(Html.fromHtml("<font color='" + hex + "'>" + html + "</font>", Html.FROM_HTML_MODE_COMPACT));
        tv.setTextSize(13f);
        tv.setPadding(0, 6, 0, 6);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        logContainer.addView(tv);
        logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
    }
}