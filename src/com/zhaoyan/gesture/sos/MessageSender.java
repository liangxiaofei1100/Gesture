package com.zhaoyan.gesture.sos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

public class MessageSender {
	public static boolean sendMessage(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"info", Context.MODE_PRIVATE);
		String number = sharedPreferences.getString("number", "");
		String info = sharedPreferences.getString("info", "help me!救救我!SOS");
		saveTime(context, number);
		if (number.isEmpty()) {
			return false;
		}
		 SmsManager smsManager = SmsManager.getDefault();
		 smsManager.sendTextMessage(number, null, info, null, null);
		return true;
	}

//	private static void writeToDataBase(Context context, String phoneNumber,
//			String smsContent) {
//		ContentValues values = new ContentValues();
//		values.put("address", phoneNumber);
//		values.put("body", smsContent);
//		values.put("type", "2");
//		values.put("read", "1");// "1"means has read ,1表示已读
//		context.getContentResolver().insert(Uri.parse("content://sms/inbox"),
//				values);
//	}

	private static void saveTime(Context context, String number) {
		if (number.isEmpty()) {
			number = "Do not set the SOS number";
		}
		long time = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = new Date(time);
		String t1 = format.format(d1);
		File file = new File(context.getFilesDir().getAbsoluteFile() + "/sos");
		Log.e("ArbiterLiu", file.getAbsolutePath());
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.write(t1 + " -- " + number + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
