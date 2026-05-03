package com.example.ambulncia_atividade.domain.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bairros")
public class Bairro {
    @PrimaryKey
    @NonNull
    public String nome;

    public double lat;
    public double lng;
}