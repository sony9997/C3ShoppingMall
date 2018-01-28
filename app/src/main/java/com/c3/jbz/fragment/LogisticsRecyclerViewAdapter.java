package com.c3.jbz.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.fragment.dummy.DummyContent.DummyItem;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.Logistics;

import java.util.ArrayList;
import java.util.List;

import static com.c3.jbz.activity.MessagesActivity.DEFAULT_TIME_FORMAT;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class LogisticsRecyclerViewAdapter extends RecyclerView.Adapter<LogisticsRecyclerViewAdapter.ViewHolder> {

    private MessagePresenter messagePresenter;
    private List<Logistics> listData;

    public LogisticsRecyclerViewAdapter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    public void setListData(List<Logistics> listData) {
        this.listData = listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_logistics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (listData == null || listData.isEmpty())
            return;
        Logistics messageInfo = listData.get(position);
        holder.mItem = messageInfo;
        holder.tv_title.setText(messageInfo.title);
        holder.tv_date.setText(DEFAULT_TIME_FORMAT.format(messageInfo.date));

        holder.iv_detail.setTag(messageInfo.clickLink);
        holder.iv_detail.setOnClickListener(onClickListener);

        holder.iv_icon.setImageResource(R.mipmap.empty);

        CheckBox checkBox= holder.cb_msg;
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(messageInfo.isChecked);
        checkBox.setTag(messageInfo);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String clickLink = (String) v.getTag();
            if (clickLink != null) {
                messagePresenter.openMainActivity(clickLink);
            }
        }
    };

    @Override
    public int getItemCount() {
        return listData != null ? listData.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public Logistics mItem;
        public final TextView tv_title;
        public final TextView tv_date;
        public final ImageView iv_detail;
        public final ImageView iv_icon;
        public final TextView tv_body;
        public CheckBox cb_msg;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_date = (TextView) view.findViewById(R.id.tv_date);
            tv_body = (TextView) view.findViewById(R.id.tv_body);
            iv_detail = (ImageView) view.findViewById(R.id.iv_detail);
            iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            cb_msg = (CheckBox) view.findViewById(R.id.cb_msg);
        }

    }

    public void addData(Logistics messageInfo) {
        if (listData == null) {
            listData = new ArrayList<Logistics>(3);
        }
        int size = listData.size();
        if (size >= BuildConfig.LIST_MAX_LEN) {
            int max = BuildConfig.LIST_MAX_LEN;
            int off = size - max;
            for (int i = off; off >= 0; i--) {
                listData.remove(max);
            }
        }
        listData.add(0, messageInfo);
        notifyDataSetChanged();
    }

    public void deleteMessageDatas(boolean isAll) {

        if (isAll) {
            AppExecutors.as().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    messagePresenter.getAppDatabase().logisticsDao().deleteLogistics(listData);
                    AppExecutors.as().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            listData.clear();
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        } else {
            final List<Logistics> list = new ArrayList<Logistics>(0);
            final List<Logistics> dellist = new ArrayList<Logistics>(0);
            for (Logistics messageInfo : listData) {
                if (messageInfo.isChecked) {
                    dellist.add(messageInfo);
                } else {
                    list.add(messageInfo);
                }
            }
            AppExecutors.as().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    messagePresenter.getAppDatabase().logisticsDao().deleteLogistics(dellist);
                    AppExecutors.as().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            listData.clear();
                            listData = null;
                            listData = list;
                            notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Logistics messageInfo = (Logistics) buttonView.getTag();
            int index = listData.indexOf(messageInfo);
            if (index >= 0)
                listData.get(index).isChecked = isChecked;
        }
    };

    public void checkedAll(boolean checked) {
        if (listData != null) {
            for (Logistics messageInfo : listData) {
                messageInfo.isChecked = checked;
            }
            notifyDataSetChanged();
        }
    }
}
