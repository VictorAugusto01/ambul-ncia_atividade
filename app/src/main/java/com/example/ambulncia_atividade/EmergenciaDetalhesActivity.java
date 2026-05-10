package com.example.ambulncia_atividade;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmergenciaDetalhesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencia_detalhes);

        findViewById(R.id.btnFinalizarAtendimento).setOnClickListener(v -> {
            Toast.makeText(this, "Atendimento registrado e vaga ocupada", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}