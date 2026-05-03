package com.example.ambulncia_atividade.domain.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "senha_hash")
    public String senhaHash;

    @ColumnInfo(name = "salt")
    public String salt;

    @ColumnInfo(name = "role")
    public String role;
}