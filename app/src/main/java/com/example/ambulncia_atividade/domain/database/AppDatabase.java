package com.example.ambulncia_atividade.domain.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ambulncia_atividade.domain.database.dao.GrafoDao;
import com.example.ambulncia_atividade.domain.database.dao.PacienteDao;
import com.example.ambulncia_atividade.domain.database.dao.UsuarioDao;
import com.example.ambulncia_atividade.domain.database.entity.AdjacenciaGrafo;
import com.example.ambulncia_atividade.domain.database.entity.Bairro;
import com.example.ambulncia_atividade.domain.database.entity.Historico;
import com.example.ambulncia_atividade.domain.database.entity.Hospital;
import com.example.ambulncia_atividade.domain.database.entity.Paciente;
import com.example.ambulncia_atividade.domain.database.entity.Usuario;
import com.example.ambulncia_atividade.domain.security.PasswordHelper;

import java.util.concurrent.Executors;

@Database(entities = {Usuario.class, Paciente.class, Bairro.class, Hospital.class, AdjacenciaGrafo.class, Historico.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();
    public abstract PacienteDao pacienteDao();
    public abstract GrafoDao grafoDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sos_leitos_room.db")
                            .allowMainThreadQueries()
                            .addCallback(roomCallback) // Adicionando o gatilho de população
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback executado apenas uma vez quando o banco é criado
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Usa thread separada para não travar a tela principal durante a criação
            Executors.newSingleThreadExecutor().execute(() -> {
                popularBancoInicial(db);
            });
        }
    };

    private static void popularBancoInicial(SupportSQLiteDatabase db) {
        // Mapeando a inserção dos usuários
        String saltMedico = PasswordHelper.generateSalt();
        String hashMedico = PasswordHelper.hashPassword("1234", saltMedico);
        db.execSQL("INSERT INTO usuarios (email, senha_hash, salt, role) VALUES ('medico@gmail.com', '" + hashMedico + "', '" + saltMedico + "', 'SOCORRISTA')");

        String saltPaciente = PasswordHelper.generateSalt();
        String hashPaciente = PasswordHelper.hashPassword("1234", saltPaciente);
        db.execSQL("INSERT INTO usuarios (email, senha_hash, salt, role) VALUES ('gabriel@gmail.com', '" + hashPaciente + "', '" + saltPaciente + "', 'PACIENTE')");

        db.execSQL("INSERT INTO pacientes (id_usuario, nome_completo, rg, tipo_sanguineo, alergias) VALUES (2, 'Gabriel Alex', '60333555-1', 'O+', 'Dipirona, Frutos do Mar')");

        db.execSQL("INSERT INTO historico (rg_paciente, hospital, data_registro) VALUES ('60333555-1', 'UPA Butantã', '10/04/2026')");
        db.execSQL("INSERT INTO historico (rg_paciente, hospital, data_registro) VALUES ('60333555-1', 'Hospital das Clínicas', '15/04/2026')");

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
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('PS Alto de Pinheiros', 'Alto de Pinheiros', 215, 215)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('UPA Butantã', 'Butantã', 250, 190)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital das Clínicas', 'Pinheiros', 500, 498)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Sancta Maggiore', 'Itaim Bibi', 80, 79)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Campo Limpo', 'Campo Limpo', 140, 95)");
        db.execSQL("INSERT INTO hospitais (nome, nome_bairro, vagas_totais, vagas_ocupadas) VALUES ('Hospital Albert Einstein', 'Morumbi', 300, 298)");

        String[][] conexoes = {
                {"Lapa", "Alto de Pinheiros"}, {"Lapa", "Vila Madalena"},
                {"Alto de Pinheiros", "Vila Madalena"}, {"Alto de Pinheiros", "Pinheiros"}, {"Alto de Pinheiros", "Butantã"},
                {"Vila Madalena", "Pinheiros"},
                {"Pinheiros", "Itaim Bibi"}, {"Pinheiros", "Morumbi"},
                {"Butantã", "Morumbi"},
                {"Itaim Bibi", "Santo Amaro"},
                {"Morumbi", "Santo Amaro"}, {"Morumbi", "Campo Limpo"},
                {"Santo Amaro", "Interlagos"},
                {"Campo Limpo", "Santo Amaro"}, {"Campo Limpo", "Capão Redondo"},
                {"Capão Redondo", "Interlagos"}
        };

        for (String[] c : conexoes) {
            db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + c[0] + "', '" + c[1] + "')");
            db.execSQL("INSERT INTO adjacencias_grafo (bairro_origem, bairro_destino) VALUES ('" + c[1] + "', '" + c[0] + "')");
        }
    }
}