package com.example.ambulncia_atividade.domain.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.example.ambulncia_atividade.domain.database.entity.AdjacenciaGrafo;
import com.example.ambulncia_atividade.domain.database.entity.Bairro;
import com.example.ambulncia_atividade.domain.database.entity.BairroStatus;
import com.example.ambulncia_atividade.domain.database.entity.Hospital;

@Dao
public interface GrafoDao {
    @Query("SELECT * FROM bairros")
    List<Bairro> getTodosBairros();

    @Query("SELECT bairro_origem, bairro_destino FROM adjacencias_grafo")
    List<AdjacenciaGrafo> getTodasAdjacencias();

    @Query("SELECT DISTINCT bairro_origem, bairro_destino FROM adjacencias_grafo WHERE bairro_origem < bairro_destino")
    List<AdjacenciaGrafo> getAdjacenciasUnicas();

    @Query("SELECT bairro_destino FROM adjacencias_grafo WHERE bairro_origem = :origem")
    List<String> getVizinhos(String origem);

    @Query("SELECT * FROM hospitais WHERE nome_bairro = :bairro")
    List<Hospital> getHospitaisDoBairro(String bairro);

    @Query("SELECT * FROM hospitais")
    List<Hospital> getTodosHospitais();

    @Query("SELECT * FROM hospitais WHERE (vagas_totais - vagas_ocupadas) > 0 LIMIT 3")
    List<Hospital> getHospitaisComVagas();

    @Query("SELECT b.nome, b.lat, b.lng, IFNULL(SUM(h.vagas_totais), 0) as total, IFNULL(SUM(h.vagas_ocupadas), 0) as ocupadas FROM bairros b LEFT JOIN hospitais h ON b.nome = h.nome_bairro GROUP BY b.nome")
    List<BairroStatus> getStatusBairros();

    @Insert
    void inserirBairros(List<Bairro> bairros);

    @Insert
    void inserirHospitais(List<Hospital> hospitais);

    @Insert
    void inserirAdjacencias(List<AdjacenciaGrafo> adjacencias);
}