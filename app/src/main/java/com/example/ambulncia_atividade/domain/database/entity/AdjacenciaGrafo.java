package com.example.ambulncia_atividade.domain.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "adjacencias_grafo",
        primaryKeys = {"bairro_origem", "bairro_destino"},
        foreignKeys = {
                @ForeignKey(entity = Bairro.class, parentColumns = "nome", childColumns = "bairro_origem", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Bairro.class, parentColumns = "nome", childColumns = "bairro_destino", onDelete = ForeignKey.CASCADE)
        })
public class AdjacenciaGrafo {
    @NonNull
    @ColumnInfo(name = "bairro_origem")
    public String bairroOrigem;

    @NonNull
    @ColumnInfo(name = "bairro_destino")
    public String bairroDestino;
}