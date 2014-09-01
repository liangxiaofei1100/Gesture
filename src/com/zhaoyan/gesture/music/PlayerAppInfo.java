package com.zhaoyan.gesture.music;

import android.graphics.drawable.Drawable;

public class PlayerAppInfo {
	private String packageName;
	private String label;
	private Drawable logo;
	private boolean choice;
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Drawable getLogo() {
		return logo;
	}
	public void setLogo(Drawable logo) {
		this.logo = logo;
	}
	public boolean isChoice() {
		return choice;
	}
	public void setChoice(boolean choice) {
		this.choice = choice;
	}
	
}
