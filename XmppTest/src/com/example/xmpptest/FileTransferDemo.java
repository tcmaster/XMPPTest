package com.example.xmpptest;

import java.io.File;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.chat.XMPPChat;
import com.example.chat.XMPPChat.SendFileCallBack;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;

/**
 * 文件发送（接收）类
 * 
 */
public class FileTransferDemo extends Activity {

	private static final int PICKPICTURE = 1;// 相册的requestCode
	private static final int TAKEPHOTO = 2;// 拍照的requestCode
	/**
	 * 临时存放拍照所得的相片的地址
	 */
	String tempName;
	/**
	 * 该页面的大布局
	 */
	@ViewInject(R.id.trans_ll)
	LinearLayout trans_ll;
	/**
	 * 要给哪个用户发送图片
	 */
	@ViewInject(R.id.trans_name_et)
	EditText name_et;
	/**
	 * 标注对方的客户端
	 */
	@ViewInject(R.id.trans_client_et)
	EditText client_et;
	/**
	 * 点击图片进行发送
	 */
	@ViewInject(R.id.trans_btn)
	Button trans_btn;
	/**
	 * 接收文件的广播接收器
	 */
	private MyReceiver receiver;
	/**
	 * 本类广播接收器的action
	 */
	public static final String TRANSFERFILE = "transfer";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_transfer_demo);
		receiver = new MyReceiver();
		registerReceiver(receiver, new IntentFilter(TRANSFERFILE));
		ViewUtils.inject(this);
	}

	@OnClick(R.id.trans_btn)
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.trans_btn:
			String name = name_et.getText().toString();
			String client = client_et.getText().toString();
			if ((name != null && !name.equals(""))
					&& (client != null && !client.equals(""))) {
				// 获取图片，进行处理
				AlertDialog.Builder builder = new AlertDialog.Builder(
						FileTransferDemo.this);
				builder.setTitle("从哪获取图片？")
						.setMessage("请选择获取图片的位置")
						.setPositiveButton("相册",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										getPhotoFromGallery();
									}
								})
						.setNegativeButton("拍照",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										getPhotoByTakePicture();
									}
								}).create().show();
			} else {
				Toast.makeText(FileTransferDemo.this, "用户名/客户端不能为空",
						Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
	}

	private void getPhotoFromGallery() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, PICKPICTURE);
	}

	private void getPhotoByTakePicture() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			tempName = System.currentTimeMillis() + ".jpg";
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DCIM).getAbsolutePath()
					+ File.separator + tempName);
			Uri u = Uri.fromFile(file);
			Log.v("lixiaosong", "我要往这里放照片" + file.getAbsolutePath());
			Intent getImageByCamera = new Intent(
					"android.media.action.IMAGE_CAPTURE");
			getImageByCamera.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
			getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, u);
			startActivityForResult(getImageByCamera, TAKEPHOTO);
		} else {
			Toast.makeText(this, "未检测到SD卡，无法拍照获取图片", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKPICTURE && resultCode == RESULT_OK) {
			if (data != null) {
				Uri uri = data.getData();
				String path = Utils.getRealPathFromURI(uri,
						FileTransferDemo.this);
				File file = new File(path);
				sendFile(file);

			}
		} else if (requestCode == TAKEPHOTO && resultCode == RESULT_OK) {
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DCIM).getAbsolutePath()
					+ File.separator + tempName);
			String phoneName = android.os.Build.MODEL;
			Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
			File resultFile = Utils.createBitmapFile(bm);
			sendFile(file);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 使用XMPP传送文件
	 * 
	 * @param file
	 *            要传送的文件
	 */
	private void sendFile(File file) {
		try {
			XMPPChat.getInstance().sendFile(name_et.getText().toString(), file,
					client_et.getText().toString(), new SendFileCallBack() {

						@Override
						public void onResult(String filePath, Status result) {
							if (result == Status.cancelled) {
								Utils.toast(FileTransferDemo.this, "传输已经取消");
							} else if (result == Status.complete) {
								// 像本页面添加一张图片文件
								ImageView iv = new ImageView(
										FileTransferDemo.this);
								LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
										150, 150);
								Bitmap bm = ThumbnailUtils.extractThumbnail(
										BitmapFactory.decodeFile(filePath),
										150, 150);
								lp.gravity = Gravity.CENTER_HORIZONTAL;
								lp.bottomMargin = 20;
								lp.topMargin = 20;
								iv.setScaleType(ScaleType.FIT_XY);
								iv.setLayoutParams(lp);
								iv.setImageBitmap(bm);
								trans_ll.addView(iv);
							} else if (result == Status.error) {
								Utils.toast(FileTransferDemo.this, "传输出现异常");
							}
						}

						@Override
						public void onProgress(double progress) {

						}
					});
		} catch (XMPPException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String filePath = intent.getStringExtra("localurl");
			ImageView iv = new ImageView(FileTransferDemo.this);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(150,
					150);
			Bitmap bm = ThumbnailUtils.extractThumbnail(
					BitmapFactory.decodeFile(filePath), 150, 150);
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			lp.bottomMargin = 20;
			lp.topMargin = 20;
			iv.setScaleType(ScaleType.FIT_XY);
			iv.setLayoutParams(lp);
			iv.setImageBitmap(bm);
			trans_ll.addView(iv);
		}

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();
	}
}
