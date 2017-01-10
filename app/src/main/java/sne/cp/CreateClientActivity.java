package sne.cp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sne.cp.db.Client;
import sne.cp.db.DbContract;

/**
 * Create or edit Client.
 */
public class CreateClientActivity extends AppCompatActivity
{
    private Client _oldClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);

        _oldClient = getIntent().getParcelableExtra(MainActivity.EXTRA_CLIENT);
        fillFields();

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cancel();
            }
        });

        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });
    }

    private void fillFields()
    {
        if (_oldClient == null) return;

        TextView nameView = (TextView) findViewById(R.id.client_name);
        TextView phoneView = (TextView) findViewById(R.id.client_phone);
        TextView socialView = (TextView) findViewById(R.id.client_social);
        TextView emailView = (TextView) findViewById(R.id.client_email);

        nameView.setText(_oldClient.getName());
        phoneView.setText(_oldClient.getPhone());
        socialView.setText(_oldClient.getSocial());
        emailView.setText(_oldClient.getEmail());
    }

    private void cancel()
    {
        finish();
    }

    private void save()
    {
        TextView nameView = (TextView) findViewById(R.id.client_name);
        TextView phoneView = (TextView) findViewById(R.id.client_phone);
        TextView socialView = (TextView) findViewById(R.id.client_social);
        TextView emailView = (TextView) findViewById(R.id.client_email);

        Client client;
        if (_oldClient == null)
        {
            client = new Client();
        }
        else
        {
            client = _oldClient;
        }

        client.setName(nameView.getText().toString().trim());
        client.setPhone(phoneView.getText().toString());
        client.setSocial(socialView.getText().toString());
        client.setEmail(emailView.getText().toString());

        if (_oldClient == null)
        {
            DbContract.createClient(this, client);
        }
        else
        {
            DbContract.updateClient(this, client);
        }
        finish();
    }
}
