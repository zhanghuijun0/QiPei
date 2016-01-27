package com.gammainfo.qipei.adapter;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gammainfo.qipei.R;

public class AreaAdapter extends BaseAdapter {
	private Activity mContext;
	private ArrayList<Area> mAreaList;

	/**
	 * 是否显示城市简写
	 */
	private boolean mIsShort;

	/**
	 * AreaAdapter构造器
	 * 
	 * @param context
	 * @param jsonArray
	 *            数据源，格式：[ {"beijing"=>["北京市","北京"]}, {"liaoning"=>["辽宁省","辽宁"]}
	 *            ]
	 * @param isShort
	 *            是否是显示简称
	 */
	public AreaAdapter(Activity context, JSONArray jsonArray, boolean isShort) {
		mContext = context;
		mIsShort = isShort;
		build(jsonArray);
	}

	private void build(JSONArray jsonArray) {
		JSONObject item;
		JSONArray names;
		int size = jsonArray.length();
		mAreaList = new ArrayList<AreaAdapter.Area>(size);
		for (int i = 0; i < size; i++) {
			try {
				Area area = new Area();
				item = jsonArray.getJSONObject(i);
				Iterator<?> it = item.keys();
				if (it.hasNext()) {
					area.mKey = it.next().toString();
					names = item.getJSONArray(area.mKey);
					area.mFullName = names.getString(0);
					area.mShortName = names.getString(1);
				}
				mAreaList.add(area);
			} catch (Exception e) {

			}
		}
	}

	public ArrayList<Area> getDataSource() {
		return mAreaList;
	}

	public void setDataSource(JSONArray jsonArray) {
		build(jsonArray);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mAreaList.size();
	}

	public Object getItem(int position) {
		return mAreaList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mContext.getLayoutInflater().inflate(
					R.layout.list_listitem_area, null);
		}
		TextView tvTitle = (TextView) convertView;
		Area area = mAreaList.get(position);
		if (mIsShort) {
			tvTitle.setText(area.mShortName);
		} else {
			tvTitle.setText(area.mFullName);
		}
		return convertView;
	}

	public String getItemKey(int position) {
		return mAreaList.get(position).mKey;
	}

	/**
	 * 获取城市名称
	 * 
	 * @param position
	 * @return 依据isShort返回命名或简称
	 */
	public String getItemValue(int position) {
		Area area = mAreaList.get(position);
		if (mIsShort) {
			return area.mShortName;
		}
		return area.mFullName;
	}

	static class Area {
		String mFullName;
		String mShortName;
		String mKey;
	}
}