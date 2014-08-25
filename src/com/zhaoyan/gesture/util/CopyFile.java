package com.zhaoyan.gesture.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.zhaoyan.gesture.R;

import android.content.Context;

public class CopyFile {
	public static void copyFile(Context context, String filePath, String resourceName) {
		File file = new File(filePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				InputStream inputStream = context.getResources()
						.openRawResource(R.raw.gestures);
				byte[] buffer = new byte[4096];
				while (inputStream.read(buffer) > 0) {
					fileOutputStream.write(buffer);
				}
				inputStream.close();
				fileOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
