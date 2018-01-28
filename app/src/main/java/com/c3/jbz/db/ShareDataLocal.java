package com.c3.jbz.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.util.Log;

import com.c3.jbz.app.C3App;

public class ShareDataLocal {
	private static ShareDataLocal cm = null;

	public static ShareDataLocal as() {
		if (cm == null) {
			cm = new ShareDataLocal();
		}
		return cm;
	}

	private ShareDataLocal() {

	}

	private Context context;
	private String name;
	private int mode;

	public void init(Context context) {
		Log.d("sd","init:"+context);
		this.context = context;
		String packageName = context.getApplicationInfo().packageName;
		name = String.format("sd_%s", packageName);
		mode = Context.MODE_PRIVATE;
	}

	public SharedPreferences getSharedPreferences() {
		if(context==null)
			context= C3App.app;
//		Log.d("sd","getSharedPreferences:"+context);
		return context.getSharedPreferences(name, mode);
	}

	public Editor getEditor() {
		return getSharedPreferences().edit();
	}

	public boolean setStringValue(String key, String value) {
		Editor editor=getEditor();
		if (value == null) {
			editor.remove(key);
		} else {
			editor.putString(key, value);
		}
		return editor.commit();
	}

	public String getStringValue(String key, String def) {
		return getSharedPreferences().getString(key, def);
	}

	public boolean setBytesValue(String key, byte[] value) {
		Editor editor=getEditor();
		if (value == null) {
			editor.remove(key);
		} else {
			String strList = new String(Base64.encode(value, Base64.DEFAULT));
			editor.putString(key, strList);
		}
		return editor.commit();
	}

	public byte[] getBytesValue(String key) {
		String value = getSharedPreferences().getString(key, null);
		if (value != null) {
			return Base64.decode(value.getBytes(), Base64.DEFAULT);
		}
		return null;
	}

	public void setIntValue(String key, int value) {
		Editor editor = getEditor();
		editor.putInt(key, value);
		editor.commit();
	}

	public int getIntValue(String key, int def) {
		return getSharedPreferences().getInt(key, def);
	}

	public void setLongValue(String key, long value) {
		Editor editor = getEditor();
		editor.putLong(key, value);
		editor.commit();
	}

	public long getLongValue(String key, long def) {
		return getSharedPreferences().getLong(key, def);
	}

	public boolean removeValue(String key) {
		Editor editor = getEditor();
		editor.remove(key);
		return editor.commit();
	}

	public void clear() {
		Editor editor = getEditor();
		editor.clear();
		editor.commit();
	}

	public void setBooleanValue(String key, boolean value) {
		Editor editor = getEditor();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	public boolean getBooleanValue(String key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key, defaultValue);
	}

	public boolean containsKey(String key){
		return getSharedPreferences().contains(key);
	}

	public void register(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		getSharedPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	public void unregister(SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		getSharedPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}
}
