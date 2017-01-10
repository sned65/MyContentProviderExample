package sne.cp.db;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import sne.cp.MainActivity;

/**
 * Implementation of interface the LoaderManager will call
 * to report about changes in the state of the client loader.
 */
public class ClientLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor>
{
    private final RecyclerView _clientListView;

    public ClientLoaderCallbacks(RecyclerView clientListView)
    {
        _clientListView = clientListView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(_clientListView.getContext(), DbContract.Clients.CONTENT_URI,
                DbContract.Clients.PROJECTION_ALL,
                null, null, DbHelper.CLIENTS_COL_FULLNAME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        getAdapter().setClientCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        getAdapter().setClientCursor(null);
    }

    private MainActivity.ClientListAdapter getAdapter()
    {
        return (MainActivity.ClientListAdapter) _clientListView.getAdapter();
    }
}
