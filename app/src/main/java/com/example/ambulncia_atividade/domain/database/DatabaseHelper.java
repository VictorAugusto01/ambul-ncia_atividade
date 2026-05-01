package com.example.ambulncia_atividade.domain.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sos_leitos.db";
    // TODO: migrar para Room Database depois, manter SQL na mão ta dando mt trabalho
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // auth e perfis
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, senha_hash TEXT, salt TEXT, role TEXT)");
        db.execSQL("CREATE TABLE pacientes (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER, nome_completo TEXT, rg TEXT UNIQUE, tipo_sanguineo TEXT, alergias TEXT, FOREIGN KEY(id_usuario) REFERENCES usuarios(id))");

        // dados do grafo (mapa)
        db.execSQL("CREATE TABLE bairros (nome TEXT PRIMARY KEY, lat REAL, lng REAL)");
        db.execSQL("CREATE TABLE hospitais (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, nome_bairro TEXT, vagas_totais INTEGER, vagas_ocupadas INTEGER, FOREIGN KEY(nome_bairro) REFERENCES bairros(nome))");
        db.execSQL("CREATE TABLE adjacencias_grafo (bairro_origem TEXT, bairro_destino TEXT, PRIMARY KEY(bairro_origem, bairro_destino), FOREIGN KEY(bairro_origem) REFERENCES bairros(nome), FOREIGN KEY(bairro_destino) REFERENCES bairros(nome))");

        // historico pra tela de triagem
        db.execSQL("CREATE TABLE historico (id INTEGER PRIMARY KEY AUTOINCREMENT, rg_paciente TEXT, hospital TEXT, data_registro TEXT, FOREIGN KEY(rg_paciente) REFERENCES pacientes(rg))");

        popularBancoInicial(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // gambiarra provisória: se mudar a versao do DB, dropa tudo e recria pra n quebrar as colunas
        db.execSQL("DROP TABLE IF EXISTS historico");
        db.execSQL("DROP TABLE IF EXISTS adjacencias_grafo");
        db.execSQL("DROP TABLE IF EXISTS hospitais");
        db.execSQL("DROP TABLE IF EXISTS bairros");
        db.execSQL("DROP TABLE IF EXISTS pacientes");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    private void popularBancoInicial(SQLiteDatabase db) {
        // mock de usuarios
        db.execSQL("INSERT INTO usuarios (email, senha_hash, role) VALUES ('medico@gmail.com', '1234', 'SOCORRISTA')");
        db.execSQL("INSERT INTO usuarios (email, senha_hash, role) VALUES ('gabriel@gmail.com', '1234', 'PACIENTE')");

        // ficha fake pra testar a triagem dps
        db.execSQL("INSERT INTO pacientes (id_usuario, nome_completo, rg, tipo_sanguineo, alergias) VALUES (2, 'Gabriel Alex', '60333555-1', 'O+', 'Dipirona, Frutos do Mar')");

        db.execSQL("INSERT INTO historico (rg_paciente, hospital, data_registro) VALUES ('60333555-1', 'UPA Butantã', '10/04/2026')");
        db.execSQL("INSERT INTO historico (rg_paciente, hospital, data_registro) VALUES ('60333555-1', 'Hospital das Clínicas', '15/04/2026')");

        // enfiando as coordenadas na mão pq não tem API consumindo isso (ainda)
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Lapa', -23.520, -46.702)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Vila Madalena', -23.551, -46.697)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Alto de Pinheiros', -23.548, -46.705)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Pinheiros', -23.561, -46.695)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Butantã', -23.572, -46.708)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Itaim Bibi', -23.585, -46.677)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Morumbi', -23.600, -46.720)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Santo Amaro', -23.650, -46.705)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Campo Limpo', -23.635, -46.764)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Capão Redondo', -23.670, -46.776)");
        db.execSQL("INSERT INTO bairros (nome, lat, lng) VALUES ('Interlagos', -23.700, -46.680)");

        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Metropolitano', 'Lapa', 100, 75)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('PS Alto de Pinheiros', 'Alto de Pinheiros', 215, 215)"); // teste: deixei lotado de proposito
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('UPA Butantã', 'Butantã', 250, 190)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital das Clínicas', 'Pinheiros', 500, 498)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Sancta Maggiore', 'Itaim Bibi', 80, 79)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Campo Limpo', 'Campo Limpo', 140, 95)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Albert Einstein', 'Morumbi', 300, 298)");

        // ligando os nós do grafo
        conectar(db, "Lapa", "Alto de Pinheiros");
        conectar(db, "Lapa", "Vila Madalena");
        conectar(db, "Alto de Pinheiros", "Vila Madalena");
        conectar(db, "Alto de Pinheiros", "Pinheiros");
        conectar(db, "Alto de Pinheiros", "Butantã");
        conectar(db, "Vila Madalena", "Pinheiros");
        conectar(db, "Pinheiros", "Itaim Bibi");
        conectar(db, "Pinheiros", "Morumbi");
        conectar(db, "Butantã", "Morumbi");
        conectar(db, "Itaim Bibi", "Santo Amaro");
        conectar(db, "Morumbi", "Santo Amaro");
        conectar(db, "Morumbi", "Campo Limpo");
        conectar(db, "Santo Amaro", "Interlagos");
        conectar(db, "Campo Limpo", "Santo Amaro");
        conectar(db, "Campo Limpo", "Capão Redondo");
        conectar(db, "Capão Redondo", "Interlagos");
    }

    // helper pra n ter que ficar duplicando insert ja q o grafo nao eh direcionado
    private void conectar(SQLiteDatabase db, String b1, String b2) {
        db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + b1 + "', '" + b2 + "')");
        db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + b2 + "', '" + b1 + "')");
    }
}