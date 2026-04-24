package com.example.ambulncia_atividade.domain;

import java.util.*;

public class AmbulanciaAtendimento {

    private static final int MAX_DISTANCIA = 3;

    private Map<String, List<String>> grafo = new HashMap<>();
    private Map<String, List<Hospital>> hospitais = new HashMap<>();

    // Classe simples para segurar os dados
    class Hospital {
        String nome;
        String bairro;
        int total;
        int ocupados;

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

    public void adicionarBairro(String bairro) {
        grafo.putIfAbsent(bairro, new ArrayList<>());
        hospitais.putIfAbsent(bairro, new ArrayList<>());
    }

    public void conectarBairros(String b1, String b2) {
        grafo.get(b1).add(b2);
        grafo.get(b2).add(b1); // ida e volta
    }

    public void adicionarHospital(String nome, String bairro, int total, int ocupados) {
        hospitais.get(bairro).add(new Hospital(nome, bairro, total, ocupados));
    }

    public String encontrarHospital(String bairroOrigem) {
        if (!grafo.containsKey(bairroOrigem)) return "Bairro não encontrado";

        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        List<Hospital> todosAnalisados = new ArrayList<>();

        fila.add(bairroOrigem);
        visitados.add(bairroOrigem);

        // Busca em largura limitando a distância máxima (3 bairros)
        for (int nivel = 0; nivel <= MAX_DISTANCIA; nivel++) {
            int tamanhoFila = fila.size();
            Hospital hospitalEscolhido = null;

            for (int i = 0; i < tamanhoFila; i++) {
                String atual = fila.poll();

                // Salva pra caso ninguém tenha vaga no final
                if (hospitais.containsKey(atual)) {
                    todosAnalisados.addAll(hospitais.get(atual));
                }

                // Procura quem tem mais vaga nesse bairro
                Hospital melhorDoBairro = null;
                if (hospitais.containsKey(atual)) {
                    for (Hospital h : hospitais.get(atual)) {
                        if (h.vagasLivres() > 0) {
                            if (melhorDoBairro == null || h.vagasLivres() > melhorDoBairro.vagasLivres()) {
                                melhorDoBairro = h;
                            }
                        }
                    }
                }

                // Compara o melhor do bairro com o melhor geral desse nível de distância
                if (melhorDoBairro != null) {
                    if (hospitalEscolhido == null || melhorDoBairro.vagasLivres() > hospitalEscolhido.vagasLivres()) {
                        hospitalEscolhido = melhorDoBairro;
                    }
                }

                // Joga os vizinhos na fila pra próxima iteração
                for (String vizinho : grafo.getOrDefault(atual, new ArrayList<>())) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                    }
                }
            }

            // Achou vaga nesse nível, pode parar e retornar
            if (hospitalEscolhido != null) {
                return hospitalEscolhido.nome;
            }
        }

        // Se rodou tudo e não achou ninguém com vaga livre, pega o menos pior
        Hospital menosLotado = null;
        for (Hospital h : todosAnalisados) {
            if (menosLotado == null || h.taxaOcupacao() < menosLotado.taxaOcupacao()) {
                menosLotado = h;
            }
        }

        return menosLotado != null ? menosLotado.nome : "Nenhum hospital encontrado";
    }
}