package com.example.ambulncia_atividade.domain.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.ambulncia_atividade.domain.database.entity.Usuario;

@Dao
public interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario getUsuarioPorEmail(String email);

    @Insert
    long inserir(Usuario usuario);
}