package com.Irondelle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Pierre Boucher on 09/01/2018.
 */

public class CreateBDD extends SQLiteOpenHelper {
    private static final String TABLE_NUMERO = "table_numero";
    static final String COL_ID = "_id";
    static final String COL_NOM = "numero";
    private static final String CREATE_BDD = "CREATE TABLE " + TABLE_NUMERO + " ( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_NOM + "" + " TEXT NOT NULL);";

    /**
     * Constructeur de la classe CreateBDD
     *
     * @param context   Activité courante
     * @param name      Nom de la base de données
     * @param factory   null
     * @param version   La version de la base de données
     */
    public CreateBDD(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Création de la base de données
     *
     * @param db    Base de données
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD);
    }

    /**
     * Sert a créer une nouvelle version de la base de données
     *
     * @param db            Base de données
     * @param oldVersion    Numero de l'ancienne version de la base de données
     * @param newVersion    Numero de la nouvelle version de la base de données
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_NUMERO + ";");
        onCreate(db);
    }
}