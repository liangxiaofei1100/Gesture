package com.zhaoyan.gesture.more;

import java.io.Serializable;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Serializable, Parcelable {

	private static final long serialVersionUID = 1967485881803917696L;

	private String mName = "Unkown";
	private int mUserID;

	private SystemInfo mSystemInfo;

	public User() {

	}
	
	public int getUserID() {
		return mUserID;
	}

	public void setUserID(int userID) {
		mUserID = userID;
	}

	public void setUserName(String name) {
		mName = name;
	}

	public String getUserName() {
		return mName;
	}

	public void setSystemInfo(SystemInfo systemInfo) {
		mSystemInfo = systemInfo;
	}

	public SystemInfo getSystemInfo() {
		return mSystemInfo;
	}

	@Override
	public String toString() {
		return "User [mName=" + mName + ", mNetworkMode=" + ", mUserID="
				+ mUserID + ", mSystemInfo=" + mSystemInfo + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mUserID);
		dest.writeString(mName);
		dest.writeSerializable(mSystemInfo);
	}

	public void readFromParcel(Parcel source) {
		mUserID = source.readInt();
		mName = source.readString();
		mSystemInfo = (SystemInfo) source.readSerializable();
	}

	public User(Parcel source) {
		readFromParcel(source);
	}

	public static Creator<User> CREATOR = new Parcelable.Creator<User>() {

		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}

		@Override
		public User[] newArray(int size) {
			return new User[size];
		}

	};
}
