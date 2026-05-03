package com.example.ambulncia_atividade.domain.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "pacientes",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "id",
                childColumns = "id_usuario",
                onDelete = ForeignKey.CASCADE))
public class Paciente {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    @ColumnInfo(name = "nome_completo")
    public String nomeCompleto;

    @ColumnInfo(name = "rg")
    public String rg;

    @ColumnInfo(name = "tipo_sanguineo")
    public String tipoSanguineo;

    @ColumnInfo(name = "alergias")
    public String alergias;
}