package com.example.ambulncia_atividade.domain.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ambulncia_atividade.domain.security.PasswordHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sos_leitos.db";
    private static final int DATABASE_VERSION = 4;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT UNIQUE, senha_hash TEXT, salt TEXT, role TEXT)");

        db.execSQL("CREATE TABLE pacientes (id INTEGER PRIMARY KEY AUTOINCREMENT, id_usuario INTEGER, nome_completo TEXT, rg TEXT UNIQUE, tipo_sanguineo TEXT, alergias TEXT, FOREIGN KEY(id_usuario) REFERENCES usuarios(id))");

        db.execSQL("CREATE TABLE bairros (nome TEXT PRIMARY KEY, lat REAL, lng REAL)");

        db.execSQL("CREATE TABLE hospitais (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, nome_bairro TEXT, vagas_totais INTEGER, vagas_ocupadas INTEGER, FOREIGN KEY(nome_bairro) REFERENCES bairros(nome))");

        db.execSQL("CREATE TABLE adjacencias_grafo (bairro_origem TEXT, bairro_destino TEXT, PRIMARY KEY(bairro_origem, bairro_destino), FOREIGN KEY(bairro_origem) REFERENCES bairros(nome), FOREIGN KEY(bairro_destino) REFERENCES bairros(nome))");

        db.execSQL("CREATE TABLE historico (id INTEGER PRIMARY KEY AUTOINCREMENT, rg_paciente TEXT, hospital TEXT, data_registro TEXT, FOREIGN KEY(rg_paciente) REFERENCES pacientes(rg))");

        popularBanco(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS historico");
        db.execSQL("DROP TABLE IF EXISTS adjacencias_grafo");
        db.execSQL("DROP TABLE IF EXISTS hospitais");
        db.execSQL("DROP TABLE IF EXISTS bairros");
        db.execSQL("DROP TABLE IF EXISTS pacientes");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    private void popularBanco(SQLiteDatabase db) {
        String s1 = PasswordHelper.generateSalt();
        String h1 = PasswordHelper.hashPassword("1234", s1);
        db.execSQL("INSERT INTO usuarios (email, senha_hash, salt, role) VALUES ('medico@gmail.com', '" + h1 + "', '" + s1 + "', 'SOCORRISTA')");

        String s2 = PasswordHelper.generateSalt();
        String h2 = PasswordHelper.hashPassword("1234", s2);
        db.execSQL("INSERT INTO usuarios (email, senha_hash, salt, role) VALUES ('gabriel@gmail.com', '" + h2 + "', '" + s2 + "', 'PACIENTE')");

        db.execSQL("INSERT INTO pacientes (id_usuario, nome_completo, rg, tipo_sanguineo, alergias) VALUES (2, 'Gabriel Alex', '60333555-1', 'O+', 'Dipirona')");

        db.execSQL("INSERT INTO historico (rg_paciente, hospital, data_registro) VALUES ('60333555-1', 'HMU Guarulhos', '10/04/2026')");

        db.execSQL("INSERT INTO bairros VALUES ('Centro', -23.4628, -46.5333)");
        db.execSQL("INSERT INTO bairros VALUES ('Macedo', -23.4685, -46.5380)");
        db.execSQL("INSERT INTO bairros VALUES ('Bosque Maia', -23.4569, -46.5330)");
        db.execSQL("INSERT INTO bairros VALUES ('Cecap', -23.4958, -46.5212)");
        db.execSQL("INSERT INTO bairros VALUES ('Taboao', -23.4702, -46.5011)");
        db.execSQL("INSERT INTO bairros VALUES ('Pimentas', -23.4396, -46.4178)");
        db.execSQL("INSERT INTO bairros VALUES ('Bonsucesso', -23.4447, -46.4123)");
        db.execSQL("INSERT INTO bairros VALUES ('Vila Galvao', -23.4440, -46.5617)");
        db.execSQL("INSERT INTO bairros VALUES ('Jardim Sao Joao', -23.4577, -46.4865)");
        db.execSQL("INSERT INTO bairros VALUES ('Cumbica', -23.4553, -46.4730)");
        db.execSQL("INSERT INTO bairros VALUES ('Parque Mikail', -23.4100, -46.5500)");
        db.execSQL("INSERT INTO bairros VALUES ('Jardim Presidente Dutra', -23.4350, -46.4850)");

        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('HMU Guarulhos', 'Centro', 300, 240)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Padre Bento', 'Macedo', 250, 200)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Geral de Guarulhos', 'Taboao', 400, 380)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Carlos Chagas', 'Centro', 150, 120)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Stella Maris', 'Bosque Maia', 180, 170)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('UPA Cumbica', 'Cumbica', 120, 90)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('UPA Sao Joao', 'Jardim Sao Joao', 100, 95)");

        addAdj(db, "Centro", "Macedo");
        addAdj(db, "Centro", "Bosque Maia");
        addAdj(db, "Centro", "Taboao");
        addAdj(db, "Macedo", "Bosque Maia");
        addAdj(db, "Macedo", "Cecap");
        addAdj(db, "Bosque Maia", "Vila Galvao");
        addAdj(db, "Cecap", "Taboao");
        addAdj(db, "Cecap", "Cumbica");
        addAdj(db, "Taboao", "Cumbica");
        addAdj(db, "Cumbica", "Jardim Sao Joao");
        addAdj(db, "Cumbica", "Jardim Presidente Dutra");
        addAdj(db, "Jardim Presidente Dutra", "Pimentas");
        addAdj(db, "Pimentas", "Bonsucesso");
        addAdj(db, "Vila Galvao", "Parque Mikail");
    }

    private void addAdj(SQLiteDatabase db, String b1, String b2) {
        db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + b1 + "', '" + b2 + "')");
        db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + b2 + "', '" + b1 + "')");
    }
}