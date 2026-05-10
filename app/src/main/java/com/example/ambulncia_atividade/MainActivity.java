package com.example.ambulncia_atividade;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        nav.setOnItemSelectedListener(item -> {
            Fragment f = null;
            int id = item.getItemId();

            if (id == R.id.nav_inicio) f = new DashboardFragment();
            else if (id == R.id.nav_pacientes) f = new TriagemFragment();
            else if (id == R.id.nav_mapa) f = new MapaFragment();
            else if (id == R.id.nav_perfil) f = new PerfilFragment();

            if (f != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, f).commit();
            }
            return true;
        });

        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_inicio);
        }
    }

    public void pularParaMapa() {
        BottomNavigationView n = findViewById(R.id.bottom_navigation);
        n.setSelectedItemId(R.id.nav_mapa);
    }
}