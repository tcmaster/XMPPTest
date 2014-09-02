package com.example.xmpptest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * 
 * @author lixiaosong
 * @category 常用工具集合类
 */
public class Utils {
	/*
	 * 简易版的 判断账户是否是邮箱或者11位数字 暂时没发现错误
	 * 
	 * @author lizhe
	 */
	public static boolean judgeAccount(String account) {

		return (account
				.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*") || account
				.matches("\\d{11}"));
	}

	public static void toast(Context context, String content) {
		Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * @param pswd
	 *            密码
	 * @category 判断密码是否符合6-20位字母或数字
	 */
	public static boolean isGoodPassword(String pswd) {
		Pattern pattern = Pattern.compile("[0-9a-zA-Z]{6,20}");
		return pattern.matcher(pswd).matches();
	}

	/**
	 * 判断该字符串是否为空
	 */
	public static boolean stringIsNullOrEmpty(String str) {
		if (str == null || str.equals(""))
			return true;
		else
			return false;
	}

	/**
	 * @category 将Uri转换为绝对地址
	 * @param contentUri
	 *            Uri内容
	 * @param context
	 *            上下文
	 * @return 绝对地址
	 */
	public static String getRealPathFromURI(Uri contentUri, Context context) {
		String res = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj,
				null, null, null);
		if (cursor.moveToFirst()) {
			;
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;
	}

	/**
	 * @category 将bitmap转换成文件
	 * @param bm
	 *            想转换的图片
	 * @return 转换好的文件
	 */
	public static File createBitmapFile(Bitmap bm) {
		ByteArrayOutputStream bAO = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bAO);
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).getAbsolutePath()
				+ File.separator + System.currentTimeMillis() + ".jpeg");
		FileOutputStream fOS = null;
		try {
			fOS = new FileOutputStream(file);
			fOS.write(bAO.toByteArray());
			return file;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fOS != null)
				try {
					fOS.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}

	/**
	 * 
	 * 隐藏软键盘
	 */

	public static void hideSoftInputMode(Context context, View windowToken) {

		InputMethodManager imm = ((InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE));

		imm.hideSoftInputFromWindow(windowToken.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);

	}

	/**
	 * 
	 * 弹出软键盘
	 */

	public static void showSoftInputMode(Context context, View windowToken) {

		final InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);

		imm.showSoftInput(windowToken, InputMethodManager.SHOW_FORCED);

	}

	/**
	 * <功能详细描述>判断网络是否可用<br>
	 * 
	 * @param context
	 * @return<br>
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否联网
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConn(Context context) {
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				String name = info.getTypeName();
				Log.e("chat", "联网方式" + name);
				return true;
			} else {
				Log.e("chat", "断网");
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	/**
	 * 判断首字母是否以数字开头
	 * 
	 * @param name
	 *            判断的字符串
	 * @return true为以数字开头，false为非数字开头
	 */
	public static boolean isNickName(String name) {
		char isNumber = name.charAt(0);
		if (isNumber <= 57 && isNumber >= 48) {
			return true;
		}
		return false;
	}

	/**
	 * 拼接图片地址
	 * 
	 * @param result
	 *            原图片地址
	 * @param size
	 *            要拼接上的字符串
	 * @return 拼接完的图片地址
	 */
	public static String processResultStr(String result, String size) {
		String after = result.substring(result.lastIndexOf("/") + 1,
				result.length());
		String before = result.substring(0, result.lastIndexOf("/") + 1);
		return before + size + after;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 获取当前时间
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurrentTime(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		String currentTime = sdf.format(date);
		return currentTime;
	}

	public static String getCurrentTime() {
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 对被旋转过的图片进行旋转校正
	 */
	public static int rotateImg(String filePath) {
		try {
			ExifInterface exifInterface = new ExifInterface(filePath);
			int tag = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			if (tag == ExifInterface.ORIENTATION_ROTATE_90) {
				return 90;
			} else if (tag == ExifInterface.ORIENTATION_ROTATE_180) {
				return 180;
			} else if (tag == ExifInterface.ORIENTATION_ROTATE_270) {
				return 270;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 旋转图片
	 * 
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 如果src的字符串为空或者等于"",则返回want的字符串，如果不满足，则返回src
	 * 
	 */
	public static String getNewStr(String src, String want) {
		if (src == null || src.equals("") || src.equals("null"))
			return want;
		else
			return src;
	}

	/**
	 * 获取网络图片,并将其保存在文件中,会返回保存的路径
	 * 
	 * @param url
	 * @return
	 */
	public static String getNetBitmap(String url) {

		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);

		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				FileOutputStream fOS = null;
				try {
					inputStream = entity.getContent();
					if (Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						File file = new File(Environment
								.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_DCIM)
								.getAbsolutePath()
								+ File.separator + "聚可乐文件图片存储");
						if (!file.exists())
							file.mkdirs();
						File bmFile = new File(file.getAbsolutePath()
								+ File.separator + System.currentTimeMillis()
								+ ".jpg");
						fOS = new FileOutputStream(bmFile);
						byte[] data = new byte[1024];
						int len = 0;
						while ((len = inputStream.read(data)) != -1) {
							fOS.write(data, 0, len);
						}
						return bmFile.getAbsolutePath();
					}
					return null;
				} catch (OutOfMemoryError e) {
					System.gc();
					return null;
				} finally {
					if (inputStream != null) {
						inputStream.close();
						inputStream = null;
					}
					if (fOS != null) {
						fOS.close();
						fOS = null;
					}
					entity.consumeContent();
				}
			}
		} catch (OutOfMemoryError o) {
			System.gc();
		} catch (IOException e) {
			getRequest.abort();
		} catch (IllegalStateException e) {
			getRequest.abort();
		} catch (Exception e) {
			getRequest.abort();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}

	/**
	 * 按照yyyy-MM-dd HH:mm:ss格式化时间
	 * 
	 * @param time
	 *            时间
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	/**
	 * 弹出网络不好的对话框
	 */
	public static AlertDialog getNetAlertDialog(final Activity act) {
		AlertDialog mConnectionDialog = new AlertDialog.Builder(act)
				.setTitle("网络错误").setMessage("没有连接到任何网络，是否现在检查网络设置?")
				.setPositiveButton("设置网络", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						act.startActivityForResult(new Intent(
								Settings.ACTION_SETTINGS), 2);
					}
				}).setNegativeButton("退出", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						act.finish();
					}
				}).create();

		mConnectionDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				act.finish();
			}
		});
		return mConnectionDialog;
	}
}
