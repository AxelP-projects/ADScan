package com.example.scannertest.bo;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class ScannedPhoto implements Parcelable {
    private List<Uri> uri = new ArrayList<>();
    private String name;
    private String folder;

    public ScannedPhoto() {
    }

    public ScannedPhoto(Uri uri, String name, String folder) {
        this.uri.add(uri);
        this.name = name;
        this.folder = folder;
    }


    protected ScannedPhoto(Parcel in) {
        uri = in.createTypedArrayList(Uri.CREATOR);
        name = in.readString();
        folder = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(uri);
        dest.writeString(name);
        dest.writeString(folder);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScannedPhoto> CREATOR = new Creator<ScannedPhoto>() {
        @Override
        public ScannedPhoto createFromParcel(Parcel in) {
            return new ScannedPhoto(in);
        }

        @Override
        public ScannedPhoto[] newArray(int size) {
            return new ScannedPhoto[size];
        }
    };

    public List<Uri> getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri.add(uri);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    @Override
    public String toString() {
        return "ScannedPhoto{" +
                "uri=" + uri +
                ", name='" + name + '\'' +
                ", folder='" + folder + '\'' +
                '}';
    }
}
