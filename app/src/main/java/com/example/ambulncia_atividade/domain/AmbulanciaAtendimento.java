package com.example.ambulncia_atividade.domain;

import android.content.Context;
import com.example.ambulncia_atividade.domain.database.AppDatabase;
import com.example.ambulncia_atividade.domain.database.entity.Hospital;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AmbulanciaAtendimento {

    private static final int MAX_DISTANCIA = 3;

    // Ponte de ligação para a base de dados via Room
    private final AppDatabase db;

    public AmbulanciaAtendimento(Context context) {
        this.db = AppDatabase.getInstance(context);
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

                // Busca direta via DAO
                List<Hospital> hospitaisNoBairro = db.grafoDao().getHospitaisDoBairro(bairroAtual);
                todosAnalisados.addAll(hospitaisNoBairro);

                Hospital melhorDoBairro = null;

                for (Hospital h : hospitaisNoBairro) {
                    int vagasLivres = h.vagasTotais - h.vagasOcupadas;
                    if (vagasLivres > 0) {
                        int livresMelhor = melhorDoBairro == null ? -1 : (melhorDoBairro.vagasTotais - melhorDoBairro.vagasOcupadas);
                        if (melhorDoBairro == null || vagasLivres > livresMelhor) {
                            melhorDoBairro = h;
                        }
                    }
                }

                if (melhorDoBairro != null) {
                    int livresMelhor = melhorDoBairro.vagasTotais - melhorDoBairro.vagasOcupadas;
                    int livresEscolhido = hospitalEscolhido == null ? -1 : (hospitalEscolhido.vagasTotais - hospitalEscolhido.vagasOcupadas);

                    if (hospitalEscolhido == null || livresMelhor > livresEscolhido) {
                        hospitalEscolhido = melhorDoBairro;
                    }
                }

                // Busca de arestas via DAO
                List<String> vizinhos = db.grafoDao().getVizinhos(bairroAtual);
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
            double taxa = h.vagasTotais == 0 ? 1.0 : (double) h.vagasOcupadas / h.vagasTotais;
            double taxaMenosLotado = menosLotado == null ? 2.0 :
                    (menosLotado.vagasTotais == 0 ? 1.0 : (double) menosLotado.vagasOcupadas / menosLotado.vagasTotais);

            if (menosLotado == null || taxa < taxaMenosLotado) {
                menosLotado = h;
            }
        }

        return menosLotado != null ? menosLotado.nome : "Nenhum hospital encontrado";
    }
}