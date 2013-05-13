package com.qiniu.speed;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.qiniu.auth.CallRet;
import com.qiniu.auth.Client;
import com.qiniu.auth.JSONObjectRet;
import com.qiniu.resumable.BlkputRet;
import com.qiniu.resumable.ResumableIO;
import com.qiniu.resumable.RputExtra;
import com.qiniu.resumable.RputNotify;
import com.qiniu.utils.Utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MyActivity extends Activity implements View.OnClickListener {

	private static final String domain = "http://sdk-demo.qiniudn.com/";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initWidget();
	}

	private ImageView selectPic;
	private QiniuProgress progressBar;
	private TextView title;
	private TextView result;

	private void initWidget() {
		selectPic = (ImageView) findViewById(R.id.imageView1);
		selectPic.setOnClickListener(this);
		title = (TextView) findViewById(R.id.textView);
		result = (TextView) findViewById(R.id.textView1);

		progressBar = (QiniuProgress) findViewById(R.id.view);
		progressBar.setMax(100);

	}

	@Override
	public void onClick(View view) {
		if (view.equals(selectPic)) {
			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, 0);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (resultCode == RESULT_CANCELED) return;
		progressBar.setVisibility(View.VISIBLE);
		result.setVisibility(View.GONE);
		title.setVisibility(View.GONE);
		selectPic.setVisibility(View.INVISIBLE);
		Client.defaultClient().call("http://chenyeblog.sinaapp.com/token?demo=1", new CallRet() {
			@Override
			public void onSuccess(byte[] bytes) {
				doResumableUpload(new String(bytes), data.getData());
			}

			@Override
			public void onFailure(Exception e) {
				toast(e);
			}
		});

	}

	private void toast(Exception e) {
		Toast.makeText(this, e.getMessage(), 300).show();
	}

	private void doResumableUpload(String uptoken, Uri uri) {
		RputExtra extra = new RputExtra("demo");
		extra.mimeType = "image/png";
		final long fsize = Utils.getSizeFromUri(this, uri);
		extra.chunkSize = 128 * 1024;
		extra.notify = new RputNotify() {
			long uploaded = 0;
			@Override
			public synchronized void onNotify(int blkIdx, int blkSize, BlkputRet ret) {
				uploaded += blkSize;
				int progress = (int) (uploaded * 100 / fsize);
				progressBar.setProgress(progress);
			}
		};

		final String key = "android-speed-test/" + System.currentTimeMillis() + "/" + new Random().nextInt(100) + ".png";
		final long t = System.currentTimeMillis();
		ResumableIO.putFile(this, uptoken, key, uri, extra, new JSONObjectRet() {
			@Override
			public void onSuccess(JSONObject resp) {
				int spendtime = (int) (System.currentTimeMillis() - t);
				float speed = (float) (fsize / spendtime / 1.024);
				String r = "上传耗时" +  spendtime + "毫秒(" + (int) speed + " kb/s)";
				download(r, key, fsize);
			}

			@Override
			public void onFailure(Exception ex) {
				toast(ex);
			}
		});
	}

	private void download(final String r, String key, final long fsize) {
		final long t = System.currentTimeMillis();
		new Down().execute(domain + key, fsize, new Runnable() {
			@Override
			public void run() {
				long t2 = System.currentTimeMillis() - t;
				float speed = (float) (fsize / t2 / 1.024);
				String report = "测试结果:\n图片体积:" + fsize / 1024 + "kb\n" + r + "\n下载耗时" + t2 + "毫秒(" + (int) speed + " kb/s)";
				result.setText(report);
			}
		});
	}

	class Down extends AsyncTask {
		long fsize;
		Runnable run;
		@Override
		protected Object doInBackground(Object... objects) {
			run = (Runnable) objects[2];
			fsize = (Long) objects[1];
			try {
				HttpResponse resp = new DefaultHttpClient().execute(new HttpGet(objects[0].toString()));
				long a = resp.getEntity().getContentLength();
				byte[] readb = new byte[128 * 1024];
				long readed = 0;
				int read = 1;
				InputStream content = resp.getEntity().getContent();
				while (read > 0) {
					read = content.read(readb);
					if (read > 0) {
						readed += read;
						publishProgress(readed * 100 / fsize);
					}
				}
			} catch (Exception e) {
				toast(e);
				return null;
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			long s = (Long) values[0];
			progressBar.setBackProgress((int) s);
		}

		@Override
		protected void onPostExecute(Object o) {
			progressBar.setVisibility(View.INVISIBLE);
			progressBar.setBackProgress(0);
			progressBar.setProgress(0);

			title.setVisibility(View.VISIBLE);
			selectPic.setVisibility(View.VISIBLE);
			result.setVisibility(View.VISIBLE);
			run.run();
		}
	}

}
