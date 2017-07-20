package com.henriquenfaria.wisetrip.models;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class TravelerModel implements Parcelable, Serializable {

    public static final Parcelable.Creator<TravelerModel> CREATOR = new Parcelable
            .Creator<TravelerModel>() {
        @Override
        public TravelerModel createFromParcel(Parcel source) {
            return new TravelerModel(source);
        }

        @Override
        public TravelerModel[] newArray(int size) {
            return new TravelerModel[size];
        }
    };
    private String contactId;
    private String name;

    @Exclude
    private Uri photoUri;

    public TravelerModel() {
        // Required for Firebase
        contactId = "";
        name = "";
    }

    public TravelerModel(Cursor cursor) {
        if (cursor != null) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts
                    .PHOTO_THUMBNAIL_URI));

            if (!TextUtils.isEmpty(photoUri)) {
                this.photoUri = Uri.parse(photoUri);
            }

            contactId = Long.toString(cursor.getLong(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID)));
        }
    }

    protected TravelerModel(Parcel in) {
        this.contactId = in.readString();
        this.name = in.readString();
        this.photoUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public String getContactId() {
        return this.contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Exclude
    public Uri getPhotoUri() {
        return photoUri;
    }

    @Exclude
    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.contactId);
        dest.writeString(this.name);
        dest.writeParcelable(this.photoUri, flags);
    }
}
