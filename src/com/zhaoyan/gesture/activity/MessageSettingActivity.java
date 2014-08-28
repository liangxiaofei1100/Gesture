package com.zhaoyan.gesture.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.zhaoyan.gesture.R;

public class MessageSettingActivity extends BaseActivity implements
		OnClickListener {
	private EditText mContactEt, mMessageEt;
	private Button mConfirmBtn;
	private ImageButton mSelectContactBtn;
	public static final String SHARED_NAME = "gesture_info", NUMBER = "number",
			INFO = "info";
	private String number,info;

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
		mConfirmBtn = (Button) findViewById(R.id.sos_confirm_btn);
		mContactEt = (EditText) findViewById(R.id.sos_contact_et);
		mSelectContactBtn = (ImageButton) findViewById(R.id.sos_contact_btn);
		mMessageEt = (EditText) findViewById(R.id.sos_message_et);
		mConfirmBtn.setOnClickListener(this);
		mSelectContactBtn.setOnClickListener(this);
		if(!number.isEmpty()){
			mContactEt.setText(number);
		}
		if(!info.isEmpty()){
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
		case R.id.sos_confirm_btn:
			saveInfo();
			finish();
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
				String num = this.getContactPhone(cursor);
				Log.e("ArbiterLiu", "number " + num);
				mContactEt.setText(num);
			}
			break;

		default:
			break;
		}
	}

	private String getContactPhone(Cursor cursor) {
		// TODO Auto-generated method stub
		int phoneColumn = cursor
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
		int phoneNum = cursor.getInt(phoneColumn);
		String result = "";
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
			if (phone.moveToFirst()) {
				for (; !phone.isAfterLast(); phone.moveToNext()) {
					int index = phone
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					int typeindex = phone
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					int phone_type = phone.getInt(typeindex);
					String phoneNumber = phone.getString(index);
					result = phoneNumber;
					// switch (phone_type) {//此处请看下方注释
					// case 2:
					// result = phoneNumber;
					// break;
					//
					// default:
					// break;
					// }
				}
				if (!phone.isClosed()) {
					phone.close();
				}
			}
		}
		return result;
	}

	private void saveInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		if (mContactEt != null) {
			String s = mContactEt.getText().toString();
			if (!s.isEmpty()) {
				editor.putString(NUMBER, s);
			}
		}
		if (mMessageEt != null) {
			String s = mMessageEt.getText().toString();
			if (!s.isEmpty()) {
				editor.putString(INFO, s);
			}
		}
		editor.commit();
	}

	private void getInfo(){
		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_NAME,
				Activity.MODE_PRIVATE);
		number=sharedPreferences.getString(NUMBER, "");
		info=sharedPreferences.getString(INFO, "");
	}
}
