package com.pm1ucenm.tl01examen122630020_122630012.Configuraciones;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

// Archivo: SQLiteConexion.java

public class SQLiteConexion extends SQLiteOpenHelper {

    // Constructor
    public SQLiteConexion(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        // Incrementa este número (por ejemplo, de 1 a 2)
        super(context, name, factory, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla contactos
        db.execSQL(Transacciones.CREATETABLECONTACTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Transacciones.DROPTABLECONTACTOS); // Borra la tabla vieja
        onCreate(db); // Crea la tabla con la nueva estructura
    }
}
