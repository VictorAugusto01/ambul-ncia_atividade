package com.example.ambulncia_atividade.domain.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "historico")
public class Historico {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "rg_paciente")
    public String rgPaciente;

    public String hospital;

    @ColumnInfo(name = "data_registro")
    public String dataRegistro;
}