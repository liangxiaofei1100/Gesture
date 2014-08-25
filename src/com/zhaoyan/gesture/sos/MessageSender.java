package com.zhaoyan.gesture.sos;

import android.content.Context;
import android.telephony.SmsManager;

public class MessageSender {
	public static boolean sendMessage(Context context) {
		context.getSharedPreferences("", context.MODE_PRIVATE);
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage("18521304322", null, "Test SOS", null, null);
		return true;
	}
}
