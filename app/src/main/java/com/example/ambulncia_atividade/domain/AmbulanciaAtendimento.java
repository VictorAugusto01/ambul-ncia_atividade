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

    private static final int MAX_DISTANCIA = 3;

    // Ponte de ligação para a base de dados
    private DatabaseHelper dbHelper;

    // Construtor: Agora a classe precisa do "Context" (a MainActivity) para poder abrir a base de dados
    public AmbulanciaAtendimento(Context context) {
        this.dbHelper = new DatabaseHelper(context);
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

        public double taxaOcupacao() {
            if (total == 0) return 1.0;
            return (double) ocupados / total;
        }
    }

    // Descobrir quem são os vizinhos de um bairro
    private List<String> obterVizinhos(String bairroOrigem) {
        List<String> vizinhos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT bairro_destino FROM adjacencias_grafo WHERE bairro_origem = ?", new String[]{bairroOrigem});

        if (cursor.moveToFirst()) {
            do {
                vizinhos.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vizinhos;
    }

    // Descobrir os hospitais que existem dentro de um bairro
    private List<Hospital> obterHospitaisDoBairro(String bairro) {
        List<Hospital> hospitais = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT nome, vagas_totais, vagas_ocupadas FROM hospitais WHERE nome_bairro = ?", new String[]{bairro});

        if (cursor.moveToFirst()) {
            do {
                String nome = cursor.getString(0);
                int vagasTotais = cursor.getInt(1);
                int vagasOcupadas = cursor.getInt(2);
                hospitais.add(new Hospital(nome, bairro, vagasTotais, vagasOcupadas));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hospitais;
    }

    public String encontrarHospital(String bairroOrigem) {
        Queue<String> fila = new LinkedList<>();
        List<String> visitados = new ArrayList<>();
        List<Hospital> todosAnalisados = new ArrayList<>();

        fila.add(bairroOrigem);
        visitados.add(bairroOrigem);

        int distanciaAtual = 0;

        while (!fila.isEmpty() && distanciaAtual <= MAX_DISTANCIA) {
            int tamanhoNivel = fila.size();
            Hospital hospitalEscolhido = null;

            for (int i = 0; i < tamanhoNivel; i++) {
                String bairroAtual = fila.poll();

                // Vai à Base de Dados procurar os hospitais deste bairro
                List<Hospital> hospitaisNoBairro = obterHospitaisDoBairro(bairroAtual);
                todosAnalisados.addAll(hospitaisNoBairro);

                Hospital melhorDoBairro = null;

                for (Hospital h : hospitaisNoBairro) {
                    if (h.vagasLivres() > 0) {
                        if (melhorDoBairro == null || h.vagasLivres() > melhorDoBairro.vagasLivres()) {
                            melhorDoBairro = h;
                        }
                    }
                }

                if (melhorDoBairro != null) {
                    if (hospitalEscolhido == null || melhorDoBairro.vagasLivres() > hospitalEscolhido.vagasLivres()) {
                        hospitalEscolhido = melhorDoBairro;
                    }
                }

                // Vai à Base de Dados procurar os vizinhos para continuar a expansão (Arestas do Grafo)
                List<String> vizinhos = obterVizinhos(bairroAtual);
                for (String vizinho : vizinhos) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                    }
                }
            }

            if (hospitalEscolhido != null) {
                return hospitalEscolhido.nome;
            }
            distanciaAtual++;
        }

        // Plano de Contingência (Menos Lotado)
        Hospital menosLotado = null;
        for (Hospital h : todosAnalisados) {
            if (menosLotado == null || h.taxaOcupacao() < menosLotado.taxaOcupacao()) {
                menosLotado = h;
            }
        }

        return menosLotado != null ? menosLotado.nome : "Nenhum hospital encontrado";
    }
}