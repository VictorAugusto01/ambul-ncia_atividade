package com.example.ambulncia_atividade.domain.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Informações do Banco
    private static final String DATABASE_NAME = "AmbulanciaDB.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela de Usuários
        // Guardamos o hash e o salt gerados pelo PasswordHelper
        String createTableUsuarios = "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "senha_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "role TEXT NOT NULL)"; // role = 'PACIENTE' ou 'MEDICO'

        // Tabela de Detalhes do Paciente (ligada ao usuário)
        String createTablePacientes = "CREATE TABLE pacientes (" +
                "id_usuario INTEGER PRIMARY KEY, " +
                "nome_completo TEXT NOT NULL, " +
                "rg TEXT UNIQUE NOT NULL, " +
                "tipo_sanguineo TEXT, " +
                "alergias TEXT, " +
                "contato_familiar TEXT, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuarios(id))";

        // Tabela de Bairros (Os Nós do Grafo)
        String createTableBairros = "CREATE TABLE bairros (" +
                "nome TEXT PRIMARY KEY, " +
                "x_pct REAL, " +
                "y_pct REAL)";

        // Tabela de Adjacências (As Arestas do Grafo )
        // Se a Lapa faz fronteira com Pinheiros, gravamos (Lapa, Pinheiros)
        String createTableAdjacencias = "CREATE TABLE adjacencias_grafo (" +
                "bairro_origem TEXT, " +
                "bairro_destino TEXT, " +
                "PRIMARY KEY (bairro_origem, bairro_destino), " +
                "FOREIGN KEY(bairro_origem) REFERENCES bairros(nome), " +
                "FOREIGN KEY(bairro_destino) REFERENCES bairros(nome))";

        // Tabela de Hospitais (O peso/dados dos Nós)
        String createTableHospitais = "CREATE TABLE hospitais (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "nome_bairro TEXT NOT NULL, " +
                "vagas_totais INTEGER NOT NULL, " +
                "vagas_ocupadas INTEGER NOT NULL, " +
                "FOREIGN KEY(nome_bairro) REFERENCES bairros(nome))";

        // Executando a criação
        db.execSQL(createTableUsuarios);
        db.execSQL(createTablePacientes);
        db.execSQL(createTableBairros);
        db.execSQL(createTableAdjacencias);
        db.execSQL(createTableHospitais);

        popularDadosIniciais(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Se a versão do banco mudar, apaga tudo e recria
        db.execSQL("DROP TABLE IF EXISTS hospitais");
        db.execSQL("DROP TABLE IF EXISTS adjacencias_grafo");
        db.execSQL("DROP TABLE IF EXISTS bairros");
        db.execSQL("DROP TABLE IF EXISTS pacientes");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
    public void popularDadosIniciais(SQLiteDatabase db) {
        // 1. Nós do Grafo (Bairros com X e Y para a UI da sua amiga)
        inserirBairro(db, "Lapa", 0.18f, 0.12f);
        inserirBairro(db, "Perdizes", 0.45f, 0.06f);
        inserirBairro(db, "Vila Madalena", 0.72f, 0.15f);
        inserirBairro(db, "Pinheiros", 0.70f, 0.38f);
        inserirBairro(db, "Alto de Pinheiros", 0.36f, 0.30f);
        inserirBairro(db, "Itaim Bibi", 0.80f, 0.58f);
        inserirBairro(db, "Butantã", 0.18f, 0.45f);
        inserirBairro(db, "Morumbi", 0.48f, 0.55f);
        inserirBairro(db, "Santo Amaro", 0.75f, 0.76f);
        inserirBairro(db, "Campo Limpo", 0.40f, 0.76f);
        inserirBairro(db, "Capão Redondo", 0.18f, 0.76f);
        inserirBairro(db, "Interlagos", 0.52f, 0.90f);

        // 2. Arestas do Grafo (Ida e Volta para o BFS funcionar em ambos os sentidos)
        String[][] conexoes = {
                {"Lapa", "Perdizes"}, {"Lapa", "Alto de Pinheiros"}, {"Lapa", "Butantã"},
                {"Perdizes", "Vila Madalena"}, {"Perdizes", "Alto de Pinheiros"},
                {"Vila Madalena", "Pinheiros"}, {"Pinheiros", "Alto de Pinheiros"},
                {"Pinheiros", "Itaim Bibi"}, {"Alto de Pinheiros", "Butantã"},
                {"Itaim Bibi", "Morumbi"}, {"Itaim Bibi", "Santo Amaro"},
                {"Butantã", "Morumbi"}, {"Morumbi", "Campo Limpo"},
                {"Campo Limpo", "Capão Redondo"}, {"Campo Limpo", "Santo Amaro"},
                {"Campo Limpo", "Interlagos"}, {"Santo Amaro", "Interlagos"},
                {"Capão Redondo", "Interlagos"}
        };

        for (String[] c : conexoes) {
            inserirAdjacencia(db, c[0], c[1]);
            inserirAdjacencia(db, c[1], c[0]);
        }

        // 3. Hospitais (Vagas idênticas ao mockup visual)
        inserirHospital(db, "Hospital Metropolitano", "Lapa", 160, 135);
        inserirHospital(db, "São Camilo", "Perdizes", 250, 205);
        inserirHospital(db, "Pérola Byington", "Vila Madalena", 120, 75);
        inserirHospital(db, "Hospital das Clínicas", "Pinheiros", 360, 358);
        inserirHospital(db, "PS Alto de Pinheiros", "Alto de Pinheiros", 215, 215);
        inserirHospital(db, "São Luiz", "Itaim Bibi", 295, 294);
        inserirHospital(db, "UPA Butantã", "Butantã", 250, 190);
        inserirHospital(db, "Albert Einstein", "Morumbi", 140, 138);
        inserirHospital(db, "Santa Casa S.A.", "Santo Amaro", 155, 115);
        inserirHospital(db, "Hospital Campo Limpo", "Campo Limpo", 140, 95);
        inserirHospital(db, "UBS Capão Redondo", "Capão Redondo", 125, 115);
        inserirHospital(db, "Hospital Interlagos", "Interlagos", 130, 120);
    }
    private void inserirBairro(SQLiteDatabase db, String nomeBairro, float xPct, float yPct) {
        ContentValues values = new ContentValues();
        values.put("nome", nomeBairro);
        values.put("x_pct", xPct);
        values.put("y_pct", yPct);
        db.insert("bairros", null, values);
    }

    private void inserirAdjacencia(SQLiteDatabase db, String origem, String destino) {
        ContentValues values = new ContentValues();
        values.put("bairro_origem", origem);
        values.put("bairro_destino", destino);
        db.insert("adjacencias_grafo", null, values);
    }

    private void inserirHospital(SQLiteDatabase db, String nome, String bairro, int vagasTotais, int vagasOcupadas) {
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("nome_bairro", bairro);
        values.put("vagas_totais", vagasTotais);
        values.put("vagas_ocupadas", vagasOcupadas);
        db.insert("hospitais", null, values);
    }

}