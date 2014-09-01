package com.zhaoyan.gesture.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhaoyan.common.view.IntroductionView;
import com.zhaoyan.gesture.R;

public class MessageSettingActivity extends BaseActivity implements
		OnClickListener {
	private EditText mContactEt, mMessageEt;
	private Button mConfirmBtn, mCleanBtn;
	private ImageButton mSelectContactBtn;
	public static final String SHARED_NAME = "gesture_info", NUMBER = "number",
			INFO = "info";
	private String number, info;
	private Dialog mSelectNumberDialog;
	private TextView mInfoLengthTv;
	private IntroductionView mIntroductionView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_message_setting);
		initTitle(R.string.main_sos);
		getInfo();
		intiView();
	}

	private void intiView() {
		mConfirmBtn = (Button) findViewById(R.id.btn_save);
		mContactEt = (EditText) findViewById(R.id.sos_contact_et);
		mSelectContactBtn = (ImageButton) findViewById(R.id.sos_contact_btn);
		mMessageEt = (EditText) findViewById(R.id.sos_message_et);
		mInfoLengthTv = (TextView) findViewById(R.id.info_length_tv);
		mCleanBtn = (Button) findViewById(R.id.btn_cancel);
		mIntroductionView = mBaseIntroductionView;
		mIntroductionView.setIntroductionText("画手势，做动作");
		mIntroductionView.setIntroductionText(getString(R.string.introduction_message));
		Intent intent = new Intent();
		intent.setClass(this, GestureShowActivity.class);
		intent.putExtra("name", "求救");
		mIntroductionView.setShowGestureIntent(intent);
		mConfirmBtn.setOnClickListener(this);
		mSelectContactBtn.setOnClickListener(this);
		mCleanBtn.setOnClickListener(this);
		mMessageEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				int len = s.length();
				if (len > 0)
					mInfoLengthTv.setText(s.length() + "个字");
				else
					mInfoLengthTv.setText("");
			}
		});
		if (!number.isEmpty()) {
			mContactEt.setText(number);
		}
		if (!info.isEmpty()) {
			mMessageEt.setText(info);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sos_contact_btn:
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			this.startActivityForResult(intent, 1);
			break;
		case R.id.btn_save:
			saveInfo();
			finish();
			break;
		case R.id.btn_cancel:
			mContactEt.setText("");
			mMessageEt.setText("");
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor cursor = getContentResolver().query(contactData, null,
						null, null, null);
				cursor.moveToFirst();
				getContactPhone(cursor);
			}
			break;

		default:
			break;
		}
	}

	private void getContactPhone(Cursor cursor) {
		// TODO Auto-generated method stub
		List<String> numberList = null;
		int phoneColumn = cursor
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int phoneNum = cursor.getInt(phoneColumn);
		if (phoneNum > 0) {
			// 获得联系人的ID号
			int idColumn = cursor.getColumnIndex(ContactsContract.Contacts._ID);
			String contactId = cursor.getString(idColumn);
			// 获得联系人电话的cursor
			Cursor phone = getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
							+ contactId, null, null);
			if (numberList == null)
				numberList = new ArrayList<String>();
			numberList.clear();
			if (phone.moveToFirst()) {
				for (; !phone.isAfterLast(); phone.moveToNext()) {
					int index = phone
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					numberList.add(phone.getString(index));
				}
				if (!phone.isClosed()) {
					phone.close();
				}
			}
			setPhoneNumber(numberList);
		}
	}

	private void saveInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		if (mContactEt != null) {
			String s = mContactEt.getText().toString();
			editor.putString(NUMBER, s);
		}
		if (mMessageEt != null) {
			String s = mMessageEt.getText().toString();
			editor.putString(INFO, s);
		}
		editor.commit();
	}

	private void getInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME,
				Activity.MODE_PRIVATE);
		number = sharedPreferences.getString(NUMBER, "");
		info = sharedPreferences.getString(INFO, "");
	}

	private void setPhoneNumber(List<String> numList) {
		if (numList == null || numList.size() == 0) {
			return;
		} else if (numList.size() == 1) {
			number = numList.get(0);
			mContactEt.setText(number);
		} else {
			Builder builder = new Builder(this);
			int num = numList.size();
			builder.setTitle("选择号码");
			final CharSequence[] a = new CharSequence[num];
			for (int i = 0; i < num; i++) {
				a[i] = numList.get(i);
			}
			builder.setSingleChoiceItems(a, 0,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							number = (String) a[which];
							mContactEt.setText(number);
							mSelectNumberDialog.dismiss();
						}
					});
			mSelectNumberDialog = builder.create();
			mSelectNumberDialog.show();
		}
	}
}
