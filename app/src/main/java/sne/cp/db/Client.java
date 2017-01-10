package sne.cp.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Single row in the Clients table.
 */
public class Client implements Parcelable
{
    private static final String DUMMY_ID = "DUMMY";
    public static final Client DUMMY = new Client(DUMMY_ID);

    private String _id;
    private String _name;
    private String _phone;
    private String _email;
    private String _social;

    public static boolean isDummy(Client client)
    {
        return DUMMY_ID.equals(client.getId());
    }

    public Client()
    {
        _id = UUID.randomUUID().toString();
    }

    @SuppressWarnings("SameParameterValue")
    private Client(String id)
    {
        _id = id;
    }

    @Override
    public String toString()
    {
        return isDummy(this) ? "Client[DUMMY]" : "Client["+_id+", "+_name+"]";
    }

    private Client(Parcel in)
    {
        _id = in.readString();
        _name = in.readString();
        _phone = in.readString();
        _email = in.readString();
        _social = in.readString();

        //in.readTypedList(_projects, Project.CREATOR);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(_id);
        dest.writeString(_name);
        dest.writeString(_phone);
        dest.writeString(_email);
        dest.writeString(_social);

        //dest.writeTypedList(_projects);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Client> CREATOR = new Parcelable.Creator<Client>()
    {
        @Override
        public Client createFromParcel(Parcel in)
        {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size)
        {
            return new Client[size];
        }
    };

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getPhone()
    {
        return _phone;
    }

    public void setPhone(String phone)
    {
        _phone = phone;
    }

    public String getEmail()
    {
        return _email;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public String getSocial()
    {
        return _social;
    }

    public void setSocial(String social)
    {
        _social = social;
    }
}
