package com.zhaoyan.gesture.more;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.zhaoyan.gesture.R;
import com.zhaoyan.gesture.activity.BaseActivity;
import com.zhaoyan.gesture.common.ZYConstant;

public class AccountSettingNameActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "AccountSettingNameActivity";
	private Button mSaveButton;
	private Button mCanceButton;
	private EditText mNickNameEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting_name);

		initTitle(R.string.account_setting_name_titile);
		mBaseIntroductionView.setVisibility(View.GONE);
		initView();

		setAccountInfo();
	}

	private void setAccountInfo() {
		AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);

		String name = accountInfo.getUserName();
		mNickNameEditText.setText(name);
	}

	private void initView() {
		mSaveButton = (Button) findViewById(R.id.btn_save);
		mSaveButton.setOnClickListener(this);
		mCanceButton = (Button) findViewById(R.id.btn_cancel);
		mCanceButton.setOnClickListener(this);

		mNickNameEditText = (EditText) findViewById(R.id.et_nick_name);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			saveAndQuit();
			break;
		case R.id.btn_cancel:
			cancelAndQuit();
			break;
		default:
			break;
		}
	}

	private void cancelAndQuit() {
		hideInputMethodManager();
//		finishWithAnimation();
		finish();
	}

	private void saveAndQuit() {
		hideInputMethodManager();
		saveAccount();
//		finishWithAnimation();
		finish();
	}

	private void hideInputMethodManager() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				mNickNameEditText.getWindowToken(), 0);
	}

	private void saveAccount() {
		String name = mNickNameEditText.getText().toString();
		if (!TextUtils.isEmpty(name)) {
			// save account
			AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);
			accountInfo.setUserName(name);
			AccountHelper.saveCurrentAccount(this, accountInfo);
			// save user info
			UserInfo userInfo = UserHelper.loadLocalUser(this);
			userInfo.getUser().setUserName(name);
			UserHelper.saveLocalUser(this, userInfo);
			// Update UserManager.
			UserManager userManager = UserManager.getInstance();
			userManager.setLocalUser(userInfo.getUser());
			// Send broadcast
			Intent intent = new Intent(
					ZYConstant.CURRENT_ACCOUNT_CHANGED_ACTION);
			sendBroadcast(intent);

		}
	}
}
