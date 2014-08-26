package com.zhaoyan.gesture.sos;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;

public class MessageSender {
	public static boolean sendMessage(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"info", Context.MODE_PRIVATE);
		String number = sharedPreferences.getString("number", "");
		String info = sharedPreferences.getString("info", "help me!救救我!SOS");
		if (number.isEmpty())
			return false;
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(number, null, info, null, null);
		writeToDataBase(context, number, info);
		return true;
	}

	private static void writeToDataBase(Context context, String phoneNumber,
			String smsContent) {
		ContentValues values = new ContentValues();
		values.put("address", phoneNumber);
		values.put("body", smsContent);
		values.put("type", "2");
		values.put("read", "1");// "1"means has read ,1表示已读
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"),
				values);
	}
}
