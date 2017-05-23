package com.henriquenfaria.wisetrip.models;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Traveler implements Parcelable {

    private String mName;
    private Uri mPhotoUri;

    @Exclude
    private long mId;

    public Traveler() {
    }

    public Traveler(String name, Uri photoUri, long id) {
        mName = name;
        mPhotoUri = photoUri;
        mId = id;
    }

    public Traveler(Cursor cursor) {
        if (cursor != null) {
            mName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts
                    .PHOTO_THUMBNAIL_URI));

            if (!TextUtils.isEmpty(photoUri)) {
                mPhotoUri = Uri.parse(photoUri);
            }

            mId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        mPhotoUri = photoUri;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeParcelable(this.mPhotoUri, flags);
        dest.writeLong(this.mId);
    }

    protected Traveler(Parcel in) {
        this.mName = in.readString();
        this.mPhotoUri = in.readParcelable(Uri.class.getClassLoader());
        this.mId = in.readLong();
    }

    public static final Parcelable.Creator<Traveler> CREATOR = new Parcelable.Creator<Traveler>() {
        @Override
        public Traveler createFromParcel(Parcel source) {
            return new Traveler(source);
        }

        @Override
        public Traveler[] newArray(int size) {
            return new Traveler[size];
        }
    };
}
