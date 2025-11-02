package com.pm1ucenm.tl01examen122630020_122630012.Configuraciones;

public class Transacciones {

    // Nombre DB
    public static final String DBNAME = "PM01UCENM";

    // Nombre de la tabla contactos
    public static final String TableContactos = "contactos";

    // Campos de la tabla
    public static final String id = "id";
    public static final String pais = "pais";
    public static final String nombre = "nombre";
    public static final String telefono = "telefono";
    public static final String nota = "nota";
    public static final String imagen = "imagen";

    // DDL - Crear tabla
    public static final String CREATETABLECONTACTOS =
            "CREATE TABLE " + TableContactos + " (" +
                    id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    pais + " TEXT NOT NULL, " +
                    nombre + " TEXT NOT NULL, " +
                    telefono + " TEXT NOT NULL, " +
                    nota + " TEXT, " +
                    imagen + " TEXT)";

    // Eliminar tabla
    public static final String DROPTABLECONTACTOS =
            "DROP TABLE IF EXISTS " + TableContactos;

    // SELECT todos los registros
    public static final String SELECTALLCONTACTOS =
            "SELECT * FROM " + TableContactos;

    // INSERT - ejemplo con parámetros
    public static final String INSERTCONTACTO =
            "INSERT INTO " + TableContactos +
                    "(" + pais + ", " + nombre + ", " + telefono + ", " + nota + ", " + imagen + ") " +
                    "VALUES (?, ?, ?, ?, ?)";

    // UPDATE - ejemplo con parámetros
    public static final String UPDATECONTACTO =
            "UPDATE " + TableContactos + " SET " +
                    pais + " = ?, " +
                    nombre + " = ?, " +
                    telefono + " = ?, " +
                    nota + " = ?, " +
                    imagen + " = ? " +
                    "WHERE " + id + " = ?";

    // DELETE - ejemplo con parámetro
    public static final String DELETECONTACTO =
            "DELETE FROM " + TableContactos + " WHERE " + id + " = ?";

    // SELECT por ID - ejemplo con parámetro
    public static final String SELECTCONTACTOBYID =
            "SELECT * FROM " + TableContactos + " WHERE " + id + " = ?";
}
