package com.example.ambulncia_atividade.domain;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ambulncia_atividade.domain.database.DatabaseHelper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AmbulanciaAtendimento {

    private static final int MAX_DIST = 3;
    private DatabaseHelper dbHelper;

    public AmbulanciaAtendimento(Context ctx) {
        this.dbHelper = new DatabaseHelper(ctx);
    }

    public static class Hospital {
        public String nome;
        public String bairro;
        public int total;
        public int ocupados;

        public Hospital(String nome, String bairro, int total, int ocupados) {
            this.nome = nome;
            this.bairro = bairro;
            this.total = total;
            this.ocupados = ocupados;
        }

        public int vagasLivres() {
            return total - ocupados;
        }

        public double taxa() {
            if (total == 0) return 1.0;
            return (double) ocupados / total;
        }
    }

    private List<String> getVizinhos(String origem) {
        List<String> v = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT bairro_destino FROM adjacencias_grafo WHERE bairro_origem = ?", new String[]{origem});
        if (c.moveToFirst()) {
            do {
                v.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return v;
    }

    private List<Hospital> getHospitais(String bairro) {
        List<Hospital> h = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nome, vagas_totais, vagas_ocupadas FROM hospitais WHERE nome_bairro = ?", new String[]{bairro});
        if (c.moveToFirst()) {
            do {
                h.add(new Hospital(c.getString(0), bairro, c.getInt(1), c.getInt(2)));
            } while (c.moveToNext());
        }
        c.close();
        return h;
    }

    public String buscarVaga(String origem) {
        Queue<String> q = new LinkedList<>();
        List<String> vis = new ArrayList<>();
        List<Hospital> processados = new ArrayList<>();

        q.add(origem);
        vis.add(origem);

        int dist = 0;

        // bfs limitando profundidade p n mandar viatura p mt longe
        while (!q.isEmpty() && dist <= MAX_DIST) {
            int size = q.size();
            Hospital target = null;

            for (int i = 0; i < size; i++) {
                String cur = q.poll();
                List<Hospital> hospitais = getHospitais(cur);
                processados.addAll(hospitais);

                Hospital melhor = null;
                for (Hospital h : hospitais) {
                    if (h.vagasLivres() > 0) {
                        if (melhor == null || h.vagasLivres() > melhor.vagasLivres()) {
                            melhor = h;
                        }
                    }
                }

                if (melhor != null) {
                    if (target == null || melhor.vagasLivres() > target.vagasLivres()) {
                        target = melhor;
                    }
                }

                for (String viz : getVizinhos(cur)) {
                    if (!vis.contains(viz)) {
                        vis.add(viz);
                        q.add(viz);
                    }
                }
            }

            if (target != null) {
                return target.nome;
            }
            dist++;
        }

        Hospital fallback = null;
        for (Hospital h : processados) {
            if (fallback == null || h.taxa() < fallback.taxa()) {
                fallback = h;
            }
        }

        return fallback != null ? fallback.nome : "Sem vagas";
    }
}