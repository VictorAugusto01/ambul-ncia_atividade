package com.example.ambulncia_atividade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ambulncia_atividade.domain.database.DatabaseHelper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LinearLayout cardResultadoFlutuante;
    private TextView tvHospitalRecomendado, tvTempoEstimado, tvDistanciaEstimada, tvStatusUTI;
    private Button btnIniciarRotaGoogle;
    private ImageView btnFecharCard;

    private FusedLocationProviderClient fusedClient;
    private static final int REQ_GPS = 100;

    private final List<String> listaBairros = new ArrayList<>();
    private final Map<String, LatLng> coords = new HashMap<>();
    private final Map<String, List<String>> adj = new HashMap<>();
    private final Map<String, Integer> vagasLivres = new HashMap<>();

    private LatLng destinoCalculado = null;
    private LatLng localRealAmbulancia = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        cardResultadoFlutuante = view.findViewById(R.id.cardResultadoFlutuante);
        tvHospitalRecomendado = view.findViewById(R.id.tvHospitalRecomendado);
        tvTempoEstimado = view.findViewById(R.id.tvTempoEstimado);
        tvDistanciaEstimada = view.findViewById(R.id.tvDistanciaEstimada);
        tvStatusUTI = view.findViewById(R.id.tvStatusUTI);
        btnIniciarRotaGoogle = view.findViewById(R.id.btnIniciarRotaGoogle);
        btnFecharCard = view.findViewById(R.id.btnFecharCard);

        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        carregarDB();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapaGoogleFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnFecharCard.setOnClickListener(v -> cardResultadoFlutuante.setVisibility(View.GONE));

        btnIniciarRotaGoogle.setOnClickListener(v -> {
            if (destinoCalculado != null) {
                startActivity(new Intent(getActivity(), EmergenciaDetalhesActivity.class));

                String uri = "https://www.google.com/maps/dir/?api=1" +
                        "&destination=" + destinoCalculado.latitude + "," + destinoCalculado.longitude +
                        "&travelmode=driving";

                Intent intentMaps = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intentMaps.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(intentMaps);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                }

            } else {
                Toast.makeText(getContext(), "Calcule uma rota primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void carregarDB() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT b.nome, b.lat, b.lng, IFNULL(SUM(h.vagas_totais), 0), IFNULL(SUM(h.vagas_ocupadas), 0) FROM bairros b LEFT JOIN hospitais h ON b.nome = h.nome_bairro GROUP BY b.nome", null);
        if (c1.moveToFirst()) {
            do {
                String nome = c1.getString(0);
                listaBairros.add(nome);
                coords.put(nome, new LatLng(c1.getDouble(1), c1.getDouble(2)));
                vagasLivres.put(nome, (c1.getInt(3) - c1.getInt(4)));
                adj.put(nome, new ArrayList<>());
            } while (c1.moveToNext());
        }
        c1.close();

        Cursor c2 = db.rawQuery("SELECT bairro_origem, bairro_destino FROM adjacencias_grafo", null);
        if (c2.moveToFirst()) {
            do {
                String origem = c2.getString(0);
                if (adj.containsKey(origem)) adj.get(origem).add(c2.getString(1));
            } while (c2.moveToNext());
        }
        c2.close();
        db.close();
    }

    private void pegarGPS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_GPS);
            return;
        }

        fusedClient.getLastLocation().addOnSuccessListener(requireActivity(), loc -> {
            if (loc != null) {
                acharVertice(loc.getLatitude(), loc.getLongitude());
            } else {
                acharVertice(-23.4628, -46.5333);
            }
        });
    }

    private void acharVertice(double lat, double lng) {
        String proximo = null;
        float distMinima = Float.MAX_VALUE;

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
            dispararBFS(proximo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_GPS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pegarGPS();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            Log.e("MAPA", "Faltou o map_style.json na pasta raw", e);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.5505, -46.6333), 11f));

        mMap.setOnMarkerClickListener(marker -> {
            String nomeBairro = marker.getTitle();
            if (nomeBairro != null && listaBairros.contains(nomeBairro)) {
                localRealAmbulancia = null;
                dispararBFS(nomeBairro);
            }
            return false;
        });

        resetMapa();
        pegarGPS();
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
                }
            }
        }

        for (String b : listaBairros) {
            int qtd = vagasLivres.get(b);
            float corPino = qtd > 0 ? BitmapDescriptorFactory.HUE_CYAN : BitmapDescriptorFactory.HUE_RED;
            mMap.addMarker(new MarkerOptions().position(coords.get(b)).title(b).snippet(qtd > 0 ? qtd + " vagas" : "Lotado")
                    .icon(BitmapDescriptorFactory.defaultMarker(corPino)));
        }

        if (localRealAmbulancia != null) {
            mMap.addMarker(new MarkerOptions().position(localRealAmbulancia).title("📍 Ambulância")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).zIndex(200));
        }
    }

    private void dispararBFS(String origem) {
        if (mMap == null || listaBairros.isEmpty()) return;

        resetMapa();

        Queue<String> fila = new LinkedList<>();
        Set<String> vistos = new HashSet<>();
        Map<String, String> trackerPai = new HashMap<>();
        Map<String, Integer> niveis = new HashMap<>();

        fila.add(origem);
        vistos.add(origem);
        niveis.put(origem, 0);

        String target = null;

        while (!fila.isEmpty()) {
            String curr = fila.poll();
            int nivelAtual = niveis.get(curr);

            if (vagasLivres.get(curr) > 0) {
                target = curr;
                break;
            }

            for (String viz : adj.get(curr)) {
                if (!vistos.contains(viz)) {
                    vistos.add(viz);
                    niveis.put(viz, nivelAtual + 1);
                    trackerPai.put(viz, curr);
                    fila.add(viz);
                }
            }
        }

        if (target != null) {
            destinoCalculado = coords.get(target);

            int distBairros = niveis.get(target);
            int minEstimado = (distBairros * 12) + 5;
            double kmEstimado = (distBairros * 4.2) + 1.5;

            tvHospitalRecomendado.setText("Hospital " + target);
            tvTempoEstimado.setText(minEstimado + " min");
            tvDistanciaEstimada.setText(String.format("• %.1f km", kmEstimado));

            int vagasL = vagasLivres.get(target);
            tvStatusUTI.setText(vagasL + " Leitos Livres");
            tvStatusUTI.setTextColor(Color.parseColor("#4CAF50"));

            cardResultadoFlutuante.setVisibility(View.VISIBLE);
            pintarRota(target, trackerPai);

        } else {
            Toast.makeText(getContext(), "CRÍTICO: Zero vagas na região", Toast.LENGTH_LONG).show();
            cardResultadoFlutuante.setVisibility(View.GONE);
        }
    }

    private void pintarRota(String dest, Map<String, String> pais) {
        String ptr = dest;
        List<LatLng> percurso = new ArrayList<>();

        while (ptr != null) {
            percurso.add(coords.get(ptr));
            ptr = pais.get(ptr); // volta no caminho ate a origem
        }

        if (localRealAmbulancia != null) percurso.add(localRealAmbulancia);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(percurso.get(percurso.size() - 1), 12f));
    }
}