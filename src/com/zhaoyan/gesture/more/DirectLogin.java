package com.zhaoyan.gesture.more;


import android.content.Context;
import android.util.Log;

public class DirectLogin implements ILogin {
	private static final String TAG = "DirectLogin";
	private Context mContext;

	public DirectLogin(Context context) {
		mContext = context;
	}

	@Override
	public boolean login() {
		Log.d(TAG, "login");
		// Logout previous account.
		AccountHelper.logoutCurrentAccount(mContext);
		
		// Set account.
		AccountInfo accountInfo = AccountHelper.getTouristAccount(mContext);
		if (accountInfo == null) {
			// there is no tourist account.
			accountInfo = new AccountInfo();
			accountInfo.setUserName(android.os.Build.MANUFACTURER);
			accountInfo.setHeadId(0);
			accountInfo.setTouristAccount(JuyouData.Account.TOURIST_ACCOUNT_TRUE);
			accountInfo = AccountHelper.addAccount(mContext,
					accountInfo);
		}
		AccountHelper.setAccountLogin(mContext, accountInfo);

		// Set userinfo
		UserInfo userInfo = UserHelper.loadLocalUser(mContext);
		if (userInfo == null) {
			// This is the first time launch. Set user info.
			userInfo = new UserInfo();
			userInfo.setUser(new User());
			userInfo.setType(JuyouData.User.TYPE_LOCAL);
		}
		userInfo.getUser().setUserName(accountInfo.getUserName());
		userInfo.getUser().setUserID(0);
		userInfo.setHeadId(accountInfo.getHeadId());
		Log.e("ArbiterLiu", ""+accountInfo.getHeadData().length);
		userInfo.setHeadBitmapData(accountInfo.getHeadData());
		UserHelper.saveLocalUser(mContext, userInfo);
		return true;
	}
}
