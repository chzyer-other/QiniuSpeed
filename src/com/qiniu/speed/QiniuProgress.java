package com.qiniu.speed;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created with IntelliJ IDEA.
 * User: cheney
 * Date: 4/23/13
 * Time: 9:51 PM
 */
public class QiniuProgress extends RelativeLayout {
	public QiniuProgress(Context context) {
		super(context);
		init();
	}

	public QiniuProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private ImageView bg;
	private ImageView now;
	private ImageView d;
	private void init() {
		bg = new ImageView(getContext());
		bg.setBackgroundResource(R.drawable.loading1);
		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		bg.setLayoutParams(lp);
		addView(bg, lp);

		now = new ImageView(getContext());
		now.setBackgroundResource(R.drawable.loading2);
		LayoutParams lp2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.ALIGN_LEFT, R.id.imageView);
		now.setLayoutParams(lp2);
		addView(now);


		d = new ImageView(getContext());
		d.setBackgroundResource(R.drawable.download);
		LayoutParams lp3 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		d.setLayoutParams(lp3);
		d.setVisibility(View.INVISIBLE);
		addView(d);
	}

	int width = 0;
	int all_width = 0;

	private int max;

	public void setMax(int i) {
		max = i;
	}

	public void setProgress(int i) {
		if (width <= 0 && all_width <= 0) {
			width = now.getWidth();
			all_width = bg.getWidth();
		}

		float p = (float)i / max;
		if (p>1) return;
		int nw = (int) ((all_width - width) * p) + width;
		LayoutParams lp = (LayoutParams) now.getLayoutParams();
		lp.width = nw;
		now.setLayoutParams(lp);
		d.setVisibility(View.GONE);
	}

	public void setBackProgress(int i) {
		d.setVisibility(View.VISIBLE);
		float p = (float)i / max;
		if (p>1) return;
		int nw = (int) ((all_width - width) * p) + width;
		LayoutParams lp = (LayoutParams) d.getLayoutParams();
		lp.width = nw;
		d.setLayoutParams(lp);
	}
}
