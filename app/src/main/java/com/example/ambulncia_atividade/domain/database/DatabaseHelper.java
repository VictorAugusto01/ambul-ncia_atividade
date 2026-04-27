package com.example.ambulncia_atividade.domain.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Informações do Banco
    private static final String DATABASE_NAME = "AmbulanciaDB.db";
    private static final int DATABASE_VERSION = 2;

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
                "nome TEXT PRIMARY KEY)";

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
        // Criando os Nós do Grafo (Bairros)
        inserirBairro(db, "Lapa");
        inserirBairro(db, "Pinheiros");
        inserirBairro(db, "Butantã");
        inserirBairro(db, "Morumbi");

        // Criando as Arestas do Grafo (Adjacências - Quem é vizinho de quem)
        // Isso significa que a ambulância pode ir da Lapa para Pinheiros
        inserirAdjacencia(db, "Lapa", "Pinheiros");
        inserirAdjacencia(db, "Pinheiros", "Lapa"); // Grafo não-direcionado (Ida e Volta)

        inserirAdjacencia(db, "Pinheiros", "Butantã");
        inserirAdjacencia(db, "Butantã", "Pinheiros");

        inserirAdjacencia(db, "Butantã", "Morumbi");
        inserirAdjacencia(db, "Morumbi", "Butantã");

        // Adicionando os Hospitais (Os dados dentro de cada Nó)
        // Nome, Bairro, Vagas Totais, Vagas Ocupadas
        inserirHospital(db, "Hospital Albert Einstein", "Morumbi", 200, 198); // Quase lotado
        inserirHospital(db, "Hospital das Clínicas", "Pinheiros", 500, 450);
        inserirHospital(db, "Hospital Metropolitano", "Lapa", 100, 20); // Bastante vaga livre
        inserirHospital(db, "UPA Butantã", "Butantã", 50, 50); // 100% lotado
    }
    private void inserirBairro(SQLiteDatabase db, String nomeBairro) {
        ContentValues values = new ContentValues();
        values.put("nome", nomeBairro);
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