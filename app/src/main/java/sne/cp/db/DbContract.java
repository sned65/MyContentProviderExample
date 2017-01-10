package sne.cp.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import sne.cp.BuildConfig;

import static android.provider.BaseColumns._ID;

/**
 * This class defines all publicly available elements,
 * like the authority, the content URIs of database tables,
 * the columns, the content types and also any intents
 * the app offers in addition to the provider.
 * <br/>
 * This class is also a facade over DB providing high-level operations.
 *
 * @see DbProvider
 */
public class DbContract
{
    private static final String TAG = DbContract.class.getName();

    /**
     * The authority of the database provider.
     */
    public static final String AUTHORITY = "sne.cp.db.DbProvider";

    /**
     * The content URI for the top-level DatabaseProvider authority.
     */
    public static final Uri CONTENT_URI=
            Uri.parse("content://"+AUTHORITY);

    /**
     * A selection clause for ID based queries.
     */
    public static final String SELECTION_ID_BASED = _ID + " = ? ";

    /**
     * Constants for the Clients table of the DB provider.
     */
    public static final class Clients
    {
        public static final String PATH = "clients";
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =  Uri.withAppendedPath(DbContract.CONTENT_URI, PATH);

        private static final String CONTENT_SUBTYPE = "/vnd.sne.cp.db_clients";
        /**
         * The mime type of a directory of clients.
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_SUBTYPE;
        /**
         * The mime type of a single client.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_SUBTYPE;

        /**
         * A projection of all columns in the clients table.
         */
        public static final String[] PROJECTION_ALL = {
                DbSchema.CLIENTS_PK + " AS " + _ID,
                DbSchema.CLIENTS_COL_FULLNAME,
                DbSchema.CLIENTS_COL_PHONE,
                DbSchema.CLIENTS_COL_EMAIL,
                DbSchema.CLIENTS_COL_SOCIAL
        };

        /**
         * The default sort order .
         */
        public static final String SORT_ORDER_DEFAULT = DbSchema.CLIENTS_COL_FULLNAME + " ASC";
    }


    public static void createClient(Context context, final Client client)
    {
        ContentValues values = new ContentValues();
        values.put(DbSchema.CLIENTS_PK, client.getId());
        values.put(DbSchema.CLIENTS_COL_FULLNAME, client.getName());
        values.put(DbSchema.CLIENTS_COL_PHONE, client.getPhone());
        values.put(DbSchema.CLIENTS_COL_EMAIL, client.getEmail());
        values.put(DbSchema.CLIENTS_COL_SOCIAL, client.getSocial());

        context.getContentResolver().insert(Clients.CONTENT_URI, values);
    }

    public static void updateClient(Context context, final Client client)
    {
        ContentValues values = new ContentValues();
        values.put(DbSchema.CLIENTS_PK, client.getId());
        values.put(DbSchema.CLIENTS_COL_FULLNAME, client.getName());
        values.put(DbSchema.CLIENTS_COL_PHONE, client.getPhone());
        values.put(DbSchema.CLIENTS_COL_EMAIL, client.getEmail());
        values.put(DbSchema.CLIENTS_COL_SOCIAL, client.getSocial());

        Uri uri = Uri.withAppendedPath(Clients.CONTENT_URI, client.getId());
        context.getContentResolver().update(uri, values, null, null);
    }

    public static void deleteClient(Context context, final String clientId)
    {
        final ContentResolver resolver = context.getContentResolver();
        new Thread(new Runnable()
        {
            public void run()
            {
                Uri uri = Uri.withAppendedPath(Clients.CONTENT_URI, clientId);
                int resCount = resolver.delete(uri, null, null);
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "Total of "+resCount+" clients deleted");
                }
            }
        }).start();
    }
}
