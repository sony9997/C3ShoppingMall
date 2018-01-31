package com.c3.jbz.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.MessageInfo;

import java.util.List;

/**
 *
 */
public class MessageFragment extends Fragment implements MessageView<MessageInfo> {
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MessagePresenter messagePresenter;
    private MessageRecyclerViewAdapter messageRecyclerViewAdapter;
    private RecyclerView recyclerView;
    private View emptyView = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MessageFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        emptyView = view.findViewById(R.id.tv_empty);
        if (recyclerView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            messageRecyclerViewAdapter = new MessageRecyclerViewAdapter(messagePresenter);
            recyclerView.setAdapter(messageRecyclerViewAdapter);
        }
        String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
        final LiveData<List<MessageInfo>> listLiveData = messagePresenter.getAppDatabase().messageInfoDao().loadAllMessageInfo(userId);
        listLiveData.observe(messagePresenter.getMessagesActivity(), new Observer<List<MessageInfo>>() {
            @Override
            public void onChanged(@Nullable List<MessageInfo> messageInfos) {
                if (messageRecyclerViewAdapter != null) {
                    messageRecyclerViewAdapter.setListData(messageInfos);
                    checkContent();
                    messageRecyclerViewAdapter.notifyDataSetChanged();
                    listLiveData.removeObserver(this);
                }
            }
        });
        return view;
    }

    public void checkContent() {
        if (messageRecyclerViewAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    public void addData(@NonNull MessageInfo messageInfo) {
        if (messageRecyclerViewAdapter != null) {
            messageRecyclerViewAdapter.addData(messageInfo);
        }
        checkContent();
    }

    @Override
    public void deleteMessageDatas() {
        if (messageRecyclerViewAdapter != null) {
            messageRecyclerViewAdapter.deleteMessageDatas();
        }
    }

    @Override
    public void checkedAll(boolean checked) {
        if (messageRecyclerViewAdapter != null) {
            messageRecyclerViewAdapter.checkedAll(checked);
        }
    }
}
