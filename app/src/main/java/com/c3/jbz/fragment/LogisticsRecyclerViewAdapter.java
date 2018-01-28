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
import com.c3.jbz.fragment.dummy.DummyContent.DummyItem;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.Logistics;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * TODO: Replace the implementation with code for your data type.
 */
public class LogisticsRecyclerViewAdapter extends RecyclerView.Adapter<LogisticsRecyclerViewAdapter.ViewHolder> {

    private MessagePresenter messagePresenter;
    private LiveData<List<Logistics>> listLiveData;

    public LogisticsRecyclerViewAdapter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
        listLiveData = messagePresenter.getAppDatabase().logisticsDao().loadAllLogistics();
        listLiveData.observe(messagePresenter.getMessagesActivity(), new Observer<List<Logistics>>() {
            @Override
            public void onChanged(@Nullable List<Logistics> logistics) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_logistics, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        List<Logistics> mValues = listLiveData != null ? listLiveData.getValue() : null;
        if (mValues == null || mValues.isEmpty())
            return;
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).title);
        holder.mContentView.setText(mValues.get(position).msgId);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return listLiveData.getValue() != null ? listLiveData.getValue().size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Logistics mItem;

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
