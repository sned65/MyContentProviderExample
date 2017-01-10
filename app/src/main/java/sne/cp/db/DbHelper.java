package sne.cp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Singleton class for database manipulations.
 */
public class DbHelper extends SQLiteOpenHelper implements DbSchema
{
    private static final String TAG = DbHelper.class.getName();

    private static final String CLIENTS_TYPED_COLUMNS =
            CLIENTS_PK + " TEXT PRIMARY KEY, " +
                    CLIENTS_COL_FULLNAME + " TEXT, " +
                    CLIENTS_COL_PHONE + " TEXT, " +
                    CLIENTS_COL_EMAIL + " TEXT, " +
                    CLIENTS_COL_SOCIAL + " TEXT";

    private static final String[] DB_CREATE_SQL = new String[] {
            "CREATE TABLE "+ TBL_CLIENTS + "(" + CLIENTS_TYPED_COLUMNS + ");"
    };

    private static DbHelper _instance;

    //////////////////////////////////////////////////////////////////
    public synchronized static DbHelper getInstance(Context ctx)
    {
        if (_instance == null)
        {
            _instance = new DbHelper(ctx.getApplicationContext());
        }
        return _instance;
    }

    private DbHelper(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        for (String sql : DB_CREATE_SQL)
        {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new RuntimeException("No upgrade available");
    }
}
