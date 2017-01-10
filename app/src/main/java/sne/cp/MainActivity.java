package sne.cp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import sne.cp.db.Client;
import sne.cp.db.ClientLoaderCallbacks;
import sne.cp.db.DbContract;
import sne.cp.db.DbProvider;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getName();

    public static final String EXTRA_CLIENT = "client";
    private static final int CLIENT_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        RecyclerView clientListView = (RecyclerView) findViewById(R.id.client_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        clientListView.setLayoutManager(layoutManager);

        ClientListAdapter adapter = new ClientListAdapter(this);
        clientListView.setAdapter(adapter);

        ClientLoaderCallbacks clientLoaderCallbacks = new ClientLoaderCallbacks(clientListView);
        getLoaderManager().initLoader(CLIENT_LOADER_ID, null, clientLoaderCallbacks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_clients, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
        case R.id.menu_create_client:
        {
            Intent client = new Intent(this, CreateClientActivity.class);
            startActivity(client);
            return true;
        }

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showClientList(boolean flag)
    {
        if (flag)
        {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            findViewById(R.id.client_list).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            findViewById(R.id.client_list).setVisibility(View.GONE);
        }
    }

    public class ClientListAdapter extends RecyclerView.Adapter<RowHolder>
    {
        private final Activity _activity;
        private Cursor _clientCursor;

        ClientListAdapter(Activity activity)
        {
            _activity = activity;
        }

        @Override
        public RowHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_row, parent, false);

            return new RowHolder(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolder holder, int position)
        {
            if (_clientCursor == null || _clientCursor.getCount() == 0)
            {
                holder.bindModel(Client.DUMMY);
            }
            else
            {
                _clientCursor.moveToPosition(position);
                holder.bindModel(_clientCursor);
            }
        }

        @Override
        public int getItemCount()
        {
            if (_clientCursor == null)
            {
                return 1;
            }
            else
            {
                int n = _clientCursor.getCount();
                if (n == 0)
                    return 1;
                else
                    return n;
            }
        }

        public void setClientCursor(Cursor c)
        {
            _clientCursor = c;
            notifyDataSetChanged();
            showClientList(true);
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder
    {
        private final Activity _activity;
        private Client _client;
        private final TextView _clientName;
        private final TextView _clientPhone;
        private final TextView _clientSocial;
        private final TextView _clientEmail;
        private final TextView _clientPhoneLabel;
        private final TextView _clientSocialLabel;
        private final TextView _clientEmailLabel;
        private final ImageButton _clientEditBtn;
        private final ImageButton _clientRemoveBtn;

        RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _clientName = (TextView) itemView.findViewById(R.id.client_name);
            _clientPhone = (TextView) itemView.findViewById(R.id.client_phone);
            _clientSocial = (TextView) itemView.findViewById(R.id.client_social);
            _clientEmail = (TextView) itemView.findViewById(R.id.client_email);
            _clientPhoneLabel = (TextView) itemView.findViewById(R.id.client_phone_label);
            _clientSocialLabel = (TextView) itemView.findViewById(R.id.client_social_label);
            _clientEmailLabel = (TextView) itemView.findViewById(R.id.client_email_label);

            _clientEditBtn = (ImageButton) itemView.findViewById(R.id.btn_edit_client);
            _clientEditBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    edit();
                }
            });
            _clientRemoveBtn = (ImageButton) itemView.findViewById(R.id.btn_remove_client);
            _clientRemoveBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    remove();
                }
            });
        }

        void bindModel(Client client)
        {
            int visibility;
            if (Client.isDummy(client))
            {
                _client = null;
                _clientName.setText(_clientName.getResources().getString(R.string.no_clients));
                _clientPhone.setText(null);
                _clientSocial.setText(null);
                _clientEmail.setText(null);
                visibility = View.GONE;
            }
            else
            {
                _client = client;
                _clientName.setText(_client.getName());
                _clientPhone.setText(_client.getPhone());
                _clientSocial.setText(_client.getSocial());
                _clientEmail.setText(_client.getEmail());
                visibility = View.VISIBLE;
            }

            _clientPhone.setVisibility(visibility);
            _clientSocial.setVisibility(visibility);
            _clientEmail.setVisibility(visibility);
            _clientPhoneLabel.setVisibility(visibility);
            _clientSocialLabel.setVisibility(visibility);
            _clientEmailLabel.setVisibility(visibility);
            _clientEditBtn.setVisibility(visibility);
            _clientRemoveBtn.setVisibility(visibility);
        }

        void bindModel(Cursor c)
        {
            Client client = DbProvider.createClientFromCursor(c);
            bindModel(client);
        }

        private void edit()
        {
            Intent client = new Intent(_activity, CreateClientActivity.class);
            client.putExtra(EXTRA_CLIENT, _client);
            _activity.startActivity(client);
        }

        private void remove()
        {
            if (_client == null) return;
            DbContract.deleteClient(_activity, _client.getId());
        }
    }
}
