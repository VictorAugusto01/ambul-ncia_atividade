package com.example.ambulncia_atividade;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        SharedPreferences s = requireActivity().getSharedPreferences("sos_leitos_prefs", Context.MODE_PRIVATE);
        String n = s.getString("nome", "Socorrista");

        TextView t = v.findViewById(R.id.tvOlaDoutor);
        t.setText("Olá, Dr. " + n.split(" ")[0]);

        v.findViewById(R.id.btnVerMapaCompleto).setOnClickListener(btn -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).pularParaMapa();
            }
        });

        return v;
    }
}