package com.c3.jbz.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.dummy.DummyContent.DummyItem;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.Notice;

import java.util.ArrayList;
import java.util.List;

import static com.c3.jbz.activity.MessagesActivity.DEFAULT_TIME_FORMAT;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class NoticeRecyclerViewAdapter extends RecyclerView.Adapter<NoticeRecyclerViewAdapter.ViewHolder> {

    private MessagePresenter messagePresenter;
    private List<Notice> listData;

    public NoticeRecyclerViewAdapter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    public void setListData(List<Notice> listData) {
        this.listData = listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (listData == null || listData.isEmpty())
            return;
        Notice messageInfo = listData.get(position);
        holder.mItem = messageInfo;

        holder.tv_title.setText(messageInfo.title);
        holder.tv_date.setText(DEFAULT_TIME_FORMAT.format(messageInfo.date));
        holder.ll_item.setTag(messageInfo.clickLink);
        holder.ll_item.setOnClickListener(onClickListener);

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
        public final TextView tv_title;
        public final TextView tv_date;
        public Notice mItem;
        public CheckBox cb_msg;
        public View ll_item;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_date = (TextView) view.findViewById(R.id.tv_date);
            cb_msg = (CheckBox) view.findViewById(R.id.cb_msg);
            ll_item=view.findViewById(R.id.ll_item);
        }

    }

    public void addData(Notice messageInfo) {
        if (listData == null) {
            listData = new ArrayList<Notice>(3);
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
                    messagePresenter.getAppDatabase().noticeDao().deleteNotice(listData);
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
            final List<Notice> list = new ArrayList<Notice>(0);
            final List<Notice> dellist = new ArrayList<Notice>(0);
            for (Notice messageInfo : listData) {
                if (messageInfo.isChecked) {
                    dellist.add(messageInfo);
                } else {
                    list.add(messageInfo);
                }
            }
            AppExecutors.as().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    messagePresenter.getAppDatabase().noticeDao().deleteNotice(dellist);
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
            Notice messageInfo = (Notice) buttonView.getTag();
            int index = listData.indexOf(messageInfo);
            if (index >= 0)
                listData.get(index).isChecked = isChecked;
        }
    };

    public void checkedAll(boolean checked) {
        if (listData != null) {
            for (Notice messageInfo : listData) {
                messageInfo.isChecked = checked;
            }
            notifyDataSetChanged();
        }
    }
}
