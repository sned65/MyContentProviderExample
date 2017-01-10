package sne.cp.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import sne.cp.BuildConfig;

import static sne.cp.db.DbSchema.CLIENTS_COL_EMAIL;
import static sne.cp.db.DbSchema.CLIENTS_COL_FULLNAME;
import static sne.cp.db.DbSchema.CLIENTS_COL_PHONE;
import static sne.cp.db.DbSchema.CLIENTS_COL_SOCIAL;
import static sne.cp.db.DbSchema.CLIENTS_PK;
import static sne.cp.db.DbSchema.TBL_CLIENTS;

/**
 * Wraps access to {@link DbHelper}.
 */
public class DbProvider extends ContentProvider
{
    private static final String TAG = DbProvider.class.getName();

    // helper constants for use with the UriMatcher
    private static final int CLIENT_LIST = 1;
    private static final int CLIENT_ID = 2;

    private static final UriMatcher URI_MATCHER;
    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(DbContract.AUTHORITY, DbContract.Clients.PATH, CLIENT_LIST);
        URI_MATCHER.addURI(DbContract.AUTHORITY, DbContract.Clients.PATH+"/*", CLIENT_ID);
    }

    private DbHelper _db;
    private final ThreadLocal<Boolean> _inBatchMode = new ThreadLocal<>();

    @Override
    public boolean onCreate()
    {
        _db = DbHelper.getInstance(getContext());
        return (_db != null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            return DbContract.Clients.CONTENT_TYPE;
        case CLIENT_ID:
            return DbContract.Clients.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "query " + uri);
        }

        SQLiteDatabase db = _db.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthorityUri = false;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            builder.setTables(TBL_CLIENTS);
            break;
        case CLIENT_ID:
            builder.setTables(TBL_CLIENTS);
            // limit query to one row at most:
            builder.appendWhere(CLIENTS_PK + " = '"
                    + uri.getLastPathSegment() + "'");
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI for query: " + uri);
        }

        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);

        // if we want to be notified of any changes:
        if (useAuthorityUri)
        {
            cursor.setNotificationUri(getContext().getContentResolver(), DbContract.CONTENT_URI);
        }
        else
        {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "insert " + uri);
        }
        if (URI_MATCHER.match(uri) != CLIENT_LIST)
        {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        if (URI_MATCHER.match(uri) == CLIENT_LIST)
        {
            db.insert(TBL_CLIENTS, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return getUriForId(uri, TBL_CLIENTS, values);
        }
        else
        {
            return null;
        }
    }

    private Uri getUriForId(Uri uri, String table, ContentValues values)
    {
        String id = values.getAsString(pk(table));
        return Uri.withAppendedPath(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "delete " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        int delCount;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            delCount = db.delete(TBL_CLIENTS, selection, selectionArgs);
            break;
        case CLIENT_ID:
            String idStr = uri.getLastPathSegment();
            String where = CLIENTS_PK + " = '" + idStr + "'";
            if (!TextUtils.isEmpty(selection))
            {
                where += " AND " + selection;
            }
            delCount = db.delete(TBL_CLIENTS, where, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI for deletion: " + uri);
        }

        // notify all listeners of changes:
        if (delCount > 0 && isNotInBatchMode())
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "update " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        int updateCount;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            updateCount = db.update(TBL_CLIENTS, values, selection, selectionArgs);
            break;
        case CLIENT_ID:
            String idStr = uri.getLastPathSegment();
            String where = CLIENTS_PK + " = '" + idStr + "'";
            if (!TextUtils.isEmpty(selection))
            {
                where += " AND " + selection;
            }
            updateCount = db.update(TBL_CLIENTS, values, where, selectionArgs);
            break;
        default:
            // no support for updating photos!
            throw new IllegalArgumentException("Unsupported URI for update: " + uri);
        }

        // notify all listeners of changes:
        if (updateCount > 0 && isNotInBatchMode())
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Override
    public @NonNull ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException
    {
        SQLiteDatabase db = _db.getWritableDatabase();
        _inBatchMode.set(true);
        // the next line works because SQLiteDatabase
        // uses a thread local SQLiteSession object for
        // all manipulations
        db.beginTransaction();
        try
        {
            final ContentProviderResult[] retResult = super.applyBatch(operations);
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(DbContract.CONTENT_URI, null);
            return retResult;
        }
        finally
        {
            _inBatchMode.remove();
            db.endTransaction();
        }
    }

    public static Client createClientFromCursor(Cursor c)
    {
        Client client = new Client();
        int idx;
        idx = c.getColumnIndex(BaseColumns._ID); // CLIENTS_PK
        client.setId(c.getString(idx));
        idx = c.getColumnIndex(CLIENTS_COL_FULLNAME);
        client.setName(c.getString(idx));
        idx = c.getColumnIndex(CLIENTS_COL_PHONE);
        client.setPhone(c.getString(idx));
        idx = c.getColumnIndex(CLIENTS_COL_EMAIL);
        client.setEmail(c.getString(idx));
        idx = c.getColumnIndex(CLIENTS_COL_SOCIAL);
        client.setSocial(c.getString(idx));
        return client;
    }

    private static String pk(String table)
    {
        switch (table)
        {
        case TBL_CLIENTS:
            return CLIENTS_PK;
        default:
            throw new IllegalArgumentException(TAG+" Unknown table " + table);
        }
    }

    private boolean isNotInBatchMode()
    {
        return _inBatchMode.get() == null || !_inBatchMode.get();
    }
}
