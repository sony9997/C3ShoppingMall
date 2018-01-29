package com.c3.jbz.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.c3.jbz.R;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.Notice;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class NoticeFragment extends Fragment implements MessageView<Notice> {

    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MessagePresenter messagePresenter;
    private RecyclerView recyclerView;
    private View emptyView = null;
    private NoticeRecyclerViewAdapter noticeRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoticeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        emptyView = view.findViewById(R.id.tv_empty);
        if (recyclerView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            noticeRecyclerViewAdapter = new NoticeRecyclerViewAdapter(messagePresenter);
            recyclerView.setAdapter(noticeRecyclerViewAdapter);
        }
        final LiveData<List<Notice>> listLiveData = messagePresenter.getAppDatabase().noticeDao().loadAllNotice();
        listLiveData.observe(messagePresenter.getMessagesActivity(), new Observer<List<Notice>>() {
            @Override
            public void onChanged(@Nullable List<Notice> messageInfos) {
                if (noticeRecyclerViewAdapter != null) {
                    noticeRecyclerViewAdapter.setListData(messageInfos);
                    checkContent();
                    noticeRecyclerViewAdapter.notifyDataSetChanged();
                    listLiveData.removeObserver(this);
                }
            }
        });
        return view;
    }


    @Override
    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    @Override
    public void addData(Notice notice) {
        if (noticeRecyclerViewAdapter != null) {
            noticeRecyclerViewAdapter.addData(notice);
        }
        checkContent();
    }

    public void checkContent() {
        if (noticeRecyclerViewAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void deleteMessageDatas() {
        if (noticeRecyclerViewAdapter != null) {
            noticeRecyclerViewAdapter.deleteMessageDatas();
        }
    }

    @Override
    public void checkedAll(boolean checked) {
        if (noticeRecyclerViewAdapter != null) {
            noticeRecyclerViewAdapter.checkedAll(checked);
        }
    }
}
