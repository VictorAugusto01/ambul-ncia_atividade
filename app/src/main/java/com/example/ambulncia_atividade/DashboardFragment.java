package com.example.ambulncia_atividade;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Bairro;
import com.example.ambulncia_atividade.domain.security.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    // ========== GPS e Localização ==========
    private FusedLocationProviderClient fusedClient;
    private static final int REQ_GPS = 100;
    private final Map<String, double[]> coordsBairros = new HashMap<>();

    // ========== Views ==========
    private Spinner spinnerBairro;
    private Button btnBuscar;
    private Button btnSimular;
    private Button btnNavegar;
    private TextView tvLog;

    private String bairroSelecionado = null;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // 1. Correção de Segurança: Puxar nome da sessão blindada (OWASP M2)
        SharedPreferences s = SessionManager.getSecurePrefs(requireContext());
        String n = s.getString("nome", "Socorrista");
        TextView t = v.findViewById(R.id.tvOlaDoutor);
        t.setText("Olá, Dr. " + n.split(" ")[0]);

        // 2. Inicializar Banco e GPS
        db = AppDatabase.getInstance(requireContext());
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // 3. Inicializar Views
        spinnerBairro = v.findViewById(R.id.spinnerBairro);
        btnBuscar = v.findViewById(R.id.btnBuscar);
        btnSimular = v.findViewById(R.id.btnSimular);
        btnNavegar = v.findViewById(R.id.btnNavegar);
        tvLog = v.findViewById(R.id.tvLog);

        carregarBairros();
        configurarListeners();
        pegarGPS(); // Tenta pegar a localização ao abrir a tela

        return v;
    }

    private void carregarBairros() {
        List<Bairro> bairros = db.grafoDao().getTodosBairros();
        List<String> nomesBairros = new ArrayList<>();

        if (bairros != null && !bairros.isEmpty()) {
            for (Bairro b : bairros) {
                nomesBairros.add(b.nome);
                coordsBairros.put(b.nome, new double[]{b.lat, b.lng});
            }
            bairroSelecionado = nomesBairros.get(0);
        } else {
            nomesBairros.add("Nenhum bairro cadastrado");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, nomesBairros);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBairro.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnBuscar.setOnClickListener(v -> {
            if (spinnerBairro.getSelectedItem() != null) {
                bairroSelecionado = spinnerBairro.getSelectedItem().toString();
                // Aqui entraria a chamada para o seu motor AmbulanciaAtendimento
                tvLog.setText("▶ Buscando via BFS a partir de: " + bairroSelecionado + "\n✅ Vaga encontrada: Hospital das Clínicas");

                // Exibe botão de rota
                double[] coordsDestino = coordsBairros.get(bairroSelecionado);
                if (coordsDestino != null) {
                    btnNavegar.setVisibility(View.VISIBLE);
                    btnNavegar.setOnClickListener(nav -> abrirNavegador(coordsDestino[0], coordsDestino[1]));
                }
            }
        });

        btnSimular.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Simulando GPS na Av. Paulista...", Toast.LENGTH_SHORT).show();
            acharBairroMaisProximo(-23.5614, -46.6559);
        });
    }

    private void pegarGPS() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_GPS);
            return;
        }

        fusedClient.getLastLocation().addOnSuccessListener(requireActivity(), loc -> {
            if (loc != null) {
                acharBairroMaisProximo(loc.getLatitude(), loc.getLongitude());
            }
        });
    }

    private void acharBairroMaisProximo(double lat, double lng) {
        String maisProximo = null;
        float menorDistancia = Float.MAX_VALUE;

        for (Map.Entry<String, double[]> entry : coordsBairros.entrySet()) {
            float[] resultado = new float[1];
            Location.distanceBetween(lat, lng, entry.getValue()[0], entry.getValue()[1], resultado);

            if (resultado[0] < menorDistancia) {
                menorDistancia = resultado[0];
                maisProximo = entry.getKey();
            }
        }

        if (maisProximo != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerBairro.getAdapter();
            if (adapter != null) {
                int posicao = adapter.getPosition(maisProximo);
                if (posicao >= 0) {
                    spinnerBairro.setSelection(posicao);
                    Toast.makeText(requireContext(), "📍 Localizado próximo a: " + maisProximo, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void abrirNavegador(double lat, double lng) {
        android.net.Uri gmmIntentUri = android.net.Uri.parse("google.navigation:q=" + lat + "," + lng);
        android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (android.content.ActivityNotFoundException e) {
            android.content.Intent genericIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(genericIntent);
        }
    }
}