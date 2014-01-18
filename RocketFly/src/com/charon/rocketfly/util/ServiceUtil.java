package com.charon.rocketfly.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceUtil {

	/**
	 * 判断当前该服务是否在运行
	 * 
	 * @param context
	 * @param cls
	 * @return
	 */
	public static boolean isServiceRunning(Context context, Class<?> cls) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = activityManager
				.getRunningServices(1024);
		for (RunningServiceInfo service : runningServices) {
			if (cls.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
