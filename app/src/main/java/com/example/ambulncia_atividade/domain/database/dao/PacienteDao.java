package com.example.ambulncia_atividade.domain.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import com.example.ambulncia_atividade.domain.database.entity.Historico;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;

@Dao
public interface PacienteDao {
    @Insert
    long inserir(Paciente paciente);

    @Query("SELECT * FROM pacientes WHERE rg = :rg LIMIT 1")
    Paciente getPacientePorRg(String rg);

    // Faz o JOIN automático e devolve o objeto Paciente montado
    @Query("SELECT p.* FROM pacientes p INNER JOIN usuarios u ON p.id_usuario = u.id WHERE u.email = :email LIMIT 1")
    Paciente getPacientePorEmail(String email);

    @Query("SELECT * FROM historico WHERE rg_paciente = :rg ORDER BY id DESC")
    List<Historico> getHistoricoPorRg(String rg);
}