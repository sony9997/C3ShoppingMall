package com.c3.jbz.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.c3.jbz.R;
import com.c3.jbz.dummy.DummyContent.DummyItem;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.MessageInfo;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified
 * TODO: Replace the implementation with code for your data type.
 */
public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private MessagePresenter messagePresenter;
    private LiveData<List<MessageInfo>> listLiveData;

    public MessageRecyclerViewAdapter(MessagePresenter messagePresenter) {
        this.messagePresenter=messagePresenter;
        listLiveData=messagePresenter.getAppDatabase().messageInfoDao().loadAllMessageInfo();
        listLiveData.observe(messagePresenter.getMessagesActivity(), new Observer<List<MessageInfo>>() {
            @Override
            public void onChanged(@Nullable List<MessageInfo> messageInfos) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        List<MessageInfo> mValues=listLiveData!=null?listLiveData.getValue():null;
        if(mValues==null||mValues.isEmpty())
            return;
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).title);
        holder.mContentView.setText(mValues.get(position).body);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listLiveData.getValue()!=null?listLiveData.getValue().size():0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public MessageInfo mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
