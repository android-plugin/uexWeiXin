package org.zywx.wbpalmstar.plugin.uexweixin.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 自定义log工具类
 * 
 * The class for print log
 */
public class MLog {

	/**
	 * 自定义选项
	 * 
	 * 可以根据需要自行修改
	 * 
	 * @changeable
	 */
	private static final String TAG = "uexWeiXin";// TAG
	private static final String DEVELOPER_NAME = "@waka@";// 开发者姓名
	private static final String FOLDER_NAME = "MLogs";// 文件夹名称

	/**
	 * 将log写入文件
	 */
	private boolean log2fileSwitch = false;// 将log写入文件的开关
	private static final String SWITCH_FILE_NAME = "mlog_switch.txt";// SD卡中控制是否写入文件的文件名
	private String logPath;// log保存在SD卡中的路径，我这里写死在SD卡根目录（主要是每次传context太麻烦），为/storage/emulated/0/MLogs/mlog_yyyy-MM-dd_HH-mm-ss.log，日期可变化，在应用程序每次打开时自动生成，保证一次只写在一个log文件中
	private SimpleDateFormat dateFormat;// 日期格式;
	private Date date;// 日期;

	/**
	 * 饿汉式单例
	 */
	private static MLog instance = new MLog();

	/**
	 * 获得单例
	 * 
	 * @return
	 */
	public static MLog getIns() {
		return instance;
	}

	/**
	 * 私有构造方法
	 */
	private MLog() {

		// 如果外部储存可用
		if (Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {// 如果外部储存可用

			// 判断SD卡中的开关文件是否存在
			File file = new File(Environment.getExternalStorageDirectory(), SWITCH_FILE_NAME);

			// 如果该文件存在
			if (file.exists()) {

				log2fileSwitch = true;// 开关置为true
				dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
				date = new Date();

				// 默认直接使用SD卡根目录
				logPath = getLogPath(Environment.getExternalStorageDirectory().getPath());
			}
		}
	}

	/**
	 * 得到log存放路径
	 * 
	 * @param rootPath
	 * @return
	 */
	private String getLogPath(String rootPath) {

		logPath = rootPath + "/" + FOLDER_NAME;
		File file = new File(logPath);
		if (!file.exists()) {
			file.mkdir();// 如果文件夹不存在，创建所有上级文件夹
		}
		logPath = logPath + "/log_" + dateFormat.format(date) + ".log";// log日志名，使用时间命名，保证不重复
		Log.i(TAG, "logPath = " + logPath);

		return logPath;
	}

	/**
	 * 初始化方法(可选)
	 * 
	 * 若调用了初始化方法，则使用谷歌推荐的外部存储路径，否则直接使用SD卡根目录
	 * 
	 * @param context
	 */
	public void init(Context context) {

		// 如果外部储存可用
		if (Environment.MEDIA_MOUNTED.equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {

			// 获得外部存储路径,默认路径为/storage/emulated/0/Android/data/包名/files
			// 这个路径的好处是会在app被卸载时自动删除
			String cleanPath = context.getExternalFilesDir(null).getPath();
			Log.i(TAG, "cleanPath = " + cleanPath);

			logPath = getLogPath(cleanPath);
		}
	}

	/**
	 * Get The Current Function Name
	 * 
	 * 得到当前方法名
	 * 
	 * @return
	 */
	private String getFunctionName() {
		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(this.getClass().getName())) {
				continue;
			}
			return DEVELOPER_NAME + " [ " + Thread.currentThread().getName() + ": " + st.getFileName() + ":" + st.getLineNumber() + " " + st.getMethodName() + " ]";
		}
		return null;
	}

	/**
	 * The Log Level:i
	 * 
	 * @param str
	 */
	public void i(Object str) {

		String name = getFunctionName();
		if (name != null) {
			Log.i(TAG, name + " - " + str);

			if (log2fileSwitch) {
				writeToFile('i', name + " - " + str);
			}
		} else {
			Log.i(TAG, str.toString());

			if (log2fileSwitch) {
				writeToFile('i', str.toString());
			}
		}

	}

	/**
	 * The Log Level:d
	 * 
	 * @param str
	 */
	public void d(Object str) {

		String name = getFunctionName();
		if (name != null) {
			Log.d(TAG, name + " - " + str);

			if (log2fileSwitch) {
				writeToFile('d', name + " - " + str);
			}
		} else {
			Log.d(TAG, str.toString());

			if (log2fileSwitch) {
				writeToFile('d', str.toString());
			}
		}
	}

	/**
	 * The Log Level:V
	 * 
	 * @param str
	 */
	public void v(Object str) {

		String name = getFunctionName();
		if (name != null) {
			Log.v(TAG, name + " - " + str);

			if (log2fileSwitch) {
				writeToFile('v', name + " - " + str);
			}
		} else {
			Log.v(TAG, str.toString());

			if (log2fileSwitch) {
				writeToFile('v', str.toString());
			}
		}
	}

	/**
	 * The Log Level:w
	 * 
	 * @param str
	 */
	public void w(Object str) {

		String name = getFunctionName();
		if (name != null) {
			Log.w(TAG, name + " - " + str);

			if (log2fileSwitch) {
				writeToFile('w', name + " - " + str);
			}
		} else {
			Log.w(TAG, str.toString());

			if (log2fileSwitch) {
				writeToFile('w', str.toString());
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param str
	 */
	public void e(Object str) {

		String name = getFunctionName();
		if (name != null) {
			Log.e(TAG, name + " - " + str);

			if (log2fileSwitch) {
				writeToFile('e', name + " - " + str);
			}
		} else {
			Log.e(TAG, str.toString());

			if (log2fileSwitch) {
				writeToFile('e', str.toString());
			}
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param ex
	 */
	public void e(Exception ex) {

		Log.e(TAG, "error", ex);

		if (log2fileSwitch) {
			writeToFile('e', ex.getMessage());
		}
	}

	/**
	 * The Log Level:e
	 * 
	 * @param log
	 * @param tr
	 */
	public void e(String log, Throwable tr) {

		String line = getFunctionName();
		Log.e(TAG, "{Thread:" + Thread.currentThread().getName() + "}" + "[" + DEVELOPER_NAME + " " + line + ":] " + log + "\n", tr);

		if (log2fileSwitch) {
			writeToFile('e', "{Thread:" + Thread.currentThread().getName() + "}" + "[" + DEVELOPER_NAME + line + ":] " + log + "\n" + tr.getMessage());
		}
	}

	/**
	 * 将log写入文件
	 * 
	 * @param type
	 * @param tag
	 * @param msg
	 */
	private void writeToFile(char type, String msg) {

		String log = dateFormat.format(date) + " " + type + " " + TAG + " " + msg + "\n";// log日志内容，可以自行定制

		FileOutputStream fos = null;// FileOutputStream会自动调用底层的close()方法，不用关闭
		BufferedWriter bw = null;
		try {

			fos = new FileOutputStream(logPath, true);// 这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
			bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(log);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();// 关闭缓冲流
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}