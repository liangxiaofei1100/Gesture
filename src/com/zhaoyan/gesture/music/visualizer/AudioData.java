package com.zhaoyan.gesture.music.visualizer;

// Data class to explicitly indicate that these bytes are raw audio data
public class AudioData {
	public AudioData(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] bytes;
}
