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
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.AppExecutors;
import com.c3.jbz.vo.MessageInfo;

import java.util.ArrayList;
import java.util.List;

import static com.c3.jbz.activity.MessagesActivity.DEFAULT_TIME_FORMAT;
import static com.c3.jbz.activity.MessagesActivity.LIST_ITEM_TIME_FORMAT;

/**
 * specified
 * TODO: Replace the implementation with code for your data type.
 */
public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private MessagePresenter messagePresenter;
    private List<MessageInfo> listData;

    public MessageRecyclerViewAdapter(MessagePresenter messagePresenter) {
        this.messagePresenter=messagePresenter;

    }

    public void setListData(List<MessageInfo> listData){
        this.listData=listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(listData==null||listData.isEmpty())
            return;
        MessageInfo messageInfo=listData.get(position);
        holder.mItem = messageInfo;

        holder.tv_pushtime.setText(DEFAULT_TIME_FORMAT.format(messageInfo.receiveTime));
        holder.tv_title.setText(messageInfo.title);
        holder.tv_body.setText(messageInfo.body);
        holder.tv_head.setText(messageInfo.head);
        holder.tv_foot.setText(messageInfo.foot);
        holder.tv_date.setText(LIST_ITEM_TIME_FORMAT.format(messageInfo.date));
        View tvDetail=holder.tv_detail;
        tvDetail.setTag(messageInfo.clickLink);
        tvDetail.setOnClickListener(onClickListener);
        CheckBox checkBox= holder.cb_msg;
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(messageInfo.isChecked);
        checkBox.setTag(messageInfo);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    @Override
    public int getItemCount() {
        return listData!=null?listData.size():0;
    }

    public void addData(MessageInfo messageInfo){
        if(listData==null){
            listData=new ArrayList<MessageInfo>(3);
        }
        int size=listData.size();
        if(size>= BuildConfig.LIST_MAX_LEN){
            int max=BuildConfig.LIST_MAX_LEN;
            int off=size-max;
            for(int i=off;off>=0;i--){
                listData.remove(max);
            }
        }
        listData.add(0,messageInfo);
        notifyDataSetChanged();
    }

    public void deleteMessageDatas(){
        final List<MessageInfo> list=new ArrayList<MessageInfo>(0);
        final List<MessageInfo> dellist=new ArrayList<MessageInfo>(0);
        for (MessageInfo messageInfo:listData){
            if(messageInfo.isChecked){
                dellist.add(messageInfo);
            }else {
                list.add(messageInfo);
            }
        }
        AppExecutors.as().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                messagePresenter.getAppDatabase().messageInfoDao().deleteMessageInfo(dellist);
                AppExecutors.as().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        listData.clear();
                        listData=null;
                        listData=list;
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener=new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            MessageInfo messageInfo= (MessageInfo) buttonView.getTag();
            int index=listData.indexOf(messageInfo);
            if(index>=0)
                listData.get(index).isChecked=isChecked;
        }
    };

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String clickLink= (String) v.getTag();
            if(clickLink!=null){
                messagePresenter.openMainActivity(clickLink);
            }
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView tv_pushtime;
        public final TextView tv_title;
        public final TextView tv_date;
        public final TextView tv_head;
        public final TextView tv_body;
        public final TextView tv_foot;
        public final TextView tv_detail;
        public final CheckBox cb_msg;
        public MessageInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            tv_pushtime = (TextView) view.findViewById(R.id.tv_pushtime);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_date = (TextView) view.findViewById(R.id.tv_date);
            tv_body = (TextView) view.findViewById(R.id.tv_body);
            tv_head = (TextView) view.findViewById(R.id.tv_head);
            tv_foot = (TextView) view.findViewById(R.id.tv_foot);
            tv_detail = (TextView) view.findViewById(R.id.tv_detail);
            cb_msg = (CheckBox) view.findViewById(R.id.cb_msg);
        }
    }

    public void checkedAll(boolean checked) {
        if(listData!=null){
            for (MessageInfo messageInfo:listData){
                messageInfo.isChecked=checked;
            }
            notifyDataSetChanged();
        }
    }
}
