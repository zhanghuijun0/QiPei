package com.gammainfo.qipei.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.widget.Toast;

import com.gammainfo.qipei.MainActivity;

/**
 * 全局异常处理
 * 
 * @author ligl
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler INSTANCE;
	private Context mContext;

	private Properties mDeviceCrashInfo = new Properties();
	private static final String VERSION_NAME = "versionName";
	private static final String VERSION_CODE = "versionCode";
	private static final String STACK_TRACE = "STACK_TRACE";
	private static final String CRASH_REPORTER_EXTENSION = ".cr";

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	public void init(Context ctx) {
		mContext = ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			ToastHelper.make(mContext, "ex:"+ex);
			Toast.makeText(mContext, "ex:"+ex, Toast.LENGTH_SHORT).show();
//			Intent intent = new Intent(mContext, MainActivity.class);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//					| Intent.FLAG_ACTIVITY_NEW_TASK);
//			mContext.startActivity(intent);
//			android.os.Process.killProcess(android.os.Process.myPid());
//			System.exit(10);
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return true;
		}
		//final String msg = ex.getLocalizedMessage();
		if (Constant.DEBUG) {
			ex.printStackTrace();
		}
		// new Thread() {
		// @Override
		// public void run() {
		// Looper.prepare();
		// Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		// Looper.loop();
		// }
		//
		// }.start();
		// collectCrashDeviceInfo(mContext);
		// String crashFileName = saveCrashInfoToFile(ex);
		// sendCrashReportsToServer(mContext);
		return true;
	}

	public void sendPreviousReportsToServer() {
		sendCrashReportsToServer(mContext);
	}

	private void sendCrashReportsToServer(Context ctx) {
		String[] crFiles = getCrashReportFiles(ctx);
		if (crFiles != null && crFiles.length > 0) {
			TreeSet<String> sortedFiles = new TreeSet<String>();
			sortedFiles.addAll(Arrays.asList(crFiles));

			for (String fileName : sortedFiles) {
				File cr = new File(ctx.getFilesDir(), fileName);
				postReport(cr);
				cr.delete();
			}
		}
	}

	private void postReport(File file) {
	}

	private String[] getCrashReportFiles(Context ctx) {
		File filesDir = ctx.getFilesDir();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(CRASH_REPORTER_EXTENSION);
			}
		};
		return filesDir.list(filter);
	}

	private String saveCrashInfoToFile(Throwable ex) {
		Writer info = new StringWriter();
		PrintWriter printWriter = new PrintWriter(info);
		ex.printStackTrace(printWriter);

		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}

		String result = info.toString();
		printWriter.close();
		mDeviceCrashInfo.put(STACK_TRACE, result);

		try {
			long timestamp = System.currentTimeMillis();
			String fileName = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;
			FileOutputStream trace = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			mDeviceCrashInfo.store(trace, "");
			trace.flush();
			trace.close();
			return fileName;
		} catch (Exception e) {
			if (Constant.DEBUG) {
				e.printStackTrace();
			}
			// Log.e(TAG, "an error occured while writing report file...", e);
		}
		return null;
	}

	public void collectCrashDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				mDeviceCrashInfo.put(VERSION_NAME,
						pi.versionName == null ? "not set" : pi.versionName);
				mDeviceCrashInfo.put(VERSION_CODE, pi.versionCode);
			}
		} catch (NameNotFoundException e) {
			if (Constant.DEBUG) {
				e.printStackTrace();
			}
			// Log.e(TAG, "Error while collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				mDeviceCrashInfo.put(field.getName(), field.get(null));
				if (Constant.DEBUG) {
					System.out.println(field.getName() + " : "
							+ field.get(null));
				}
			} catch (Exception e) {
				if (Constant.DEBUG) {
					e.printStackTrace();
				}
				// Log.e(TAG, "Error while collect crash info", e);
			}

		}

	}

}
