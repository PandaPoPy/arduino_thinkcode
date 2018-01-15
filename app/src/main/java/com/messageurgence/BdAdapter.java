package com.messageurgence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Pierre Boucher on 09/01/2018.
 */
public class BdAdapter {
    static final int VERSION_BDD = 1;
    private static final String NOM_BDD = "numero.db";
    static final String TABLE_NUMERO = "table_numero";
    static final String COL_ID = "_id";
    static final int NUM_COL_ID = 0;
    static final String COL_NUMERO = "numero";
    static final int NUM_COL_NUMERO = 1;
    private CreateBDD bdNumero;
    private Context context;
    private SQLiteDatabase db;

    /**
     * Constructeur de la classe BdAdapter
     *
     * @param context   Activité en cours
     */
    public BdAdapter(Context context) {
        this.context = context;
        bdNumero = new CreateBDD(context, NOM_BDD, null, VERSION_BDD);
    }

    /**
     * Ouverture de la base de données
     */
    public void open() {
        db = bdNumero.getWritableDatabase();
    }

    /**
     * Fermeture de a base de données
     */
    public void close() {
        db.close();
    }

    /**
     * Fonction permettant la mise a jour de la base données avec le nouveau numero de telephone
     *
     * @param numero    Nouveau numero de telephone
     */
    public void updateNumero(String numero) {
        removeNumero();
        insertNumero(numero);
    }

    /**
     * Fonction permettant l'insertion d'un nouveau numero dans la base de données
     *
     * @param numero    Nouveau numero de telephone
     * @return          Retourne l'id de la nouvelle ligne inséré
     */
    public long insertNumero(String numero) {
        ContentValues values = new ContentValues();
        values.put(COL_NUMERO, numero);
        return db.insert(TABLE_NUMERO, null, values);
    }

    /**
     * Fonction permettant de retirer les numero de telephone present dans la base de données
     *
     * @return Retourne 1 si la fontion à marché, 0 sinon
     */
    public int removeNumero() {
        return db.delete(TABLE_NUMERO, COL_NUMERO + " LIKE '%'", null);
    }

    /**
     * Fonction permettant de récuperer les numeros de telephones de la base de données
     *
     * @return  Retourne tout les numeros de la base de donnée dans un type Cursor
     */
    public Cursor getNumero() {
        return db.rawQuery("SELECT numero FROM table_numero", null);
    }
}