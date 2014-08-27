package com.zhaoyan.gesture.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zhaoyan.gesture.R;

public class MessageSettingActivity extends BaseActivity implements OnClickListener {
	private EditText mContactEt, mMessageEt;
	private Button mSelectContactBtn, mConfirmBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sos_message_setting);
		initTitle(R.string.main_sos);
		intiView();
	}

	private void intiView() {
		mConfirmBtn = (Button) findViewById(R.id.sos_confirm_btn);
		mContactEt = (EditText) findViewById(R.id.sos_contact_et);
		mSelectContactBtn = (Button) findViewById(R.id.sos_contact_btn);
		mMessageEt = (EditText) findViewById(R.id.sos_message_et);
		mConfirmBtn.setOnClickListener(this);
		mSelectContactBtn.setOnClickListener(this);
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
}
