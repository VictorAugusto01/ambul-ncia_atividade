package com.example.ambulncia_atividade.domain.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "hospitais",
        foreignKeys = @ForeignKey(entity = Bairro.class,
                parentColumns = "nome",
                childColumns = "nome_bairro",
                onDelete = ForeignKey.CASCADE))
public class Hospital {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nome;

    @ColumnInfo(name = "nome_bairro")
    public String nomeBairro;

    @ColumnInfo(name = "vagas_totais")
    public int vagasTotais;

    @ColumnInfo(name = "vagas_ocupadas")
    public int vagasOcupadas;
}