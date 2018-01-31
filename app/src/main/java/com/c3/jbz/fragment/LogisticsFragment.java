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

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.db.ShareDataLocal;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.Logistics;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class LogisticsFragment extends Fragment implements MessageView<Logistics> {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MessagePresenter messagePresenter;
    private RecyclerView recyclerView;
    private View emptyView = null;
    private LogisticsRecyclerViewAdapter logisticsRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LogisticsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logistics_list, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        emptyView = view.findViewById(R.id.tv_empty);
        if (recyclerView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            logisticsRecyclerViewAdapter = new LogisticsRecyclerViewAdapter(messagePresenter);
            recyclerView.setAdapter(logisticsRecyclerViewAdapter);
        }
        String userId= ShareDataLocal.as().getStringValue(BuildConfig.KEY_USERID,null);
        final LiveData<List<Logistics>> listLiveData = messagePresenter.getAppDatabase().logisticsDao().loadAllLogistics(userId);
        listLiveData.observe(messagePresenter.getMessagesActivity(), new Observer<List<Logistics>>() {
            @Override
            public void onChanged(@Nullable List<Logistics> messageInfos) {
                if (logisticsRecyclerViewAdapter != null) {
                    logisticsRecyclerViewAdapter.setListData(messageInfos);
                    checkContent();
                    logisticsRecyclerViewAdapter.notifyDataSetChanged();
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
    public void addData(Logistics logistics) {
        if (logisticsRecyclerViewAdapter != null) {
            logisticsRecyclerViewAdapter.addData(logistics);
        }
        checkContent();
    }

    public void checkContent() {
        if (logisticsRecyclerViewAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void deleteMessageDatas() {
        if (logisticsRecyclerViewAdapter != null) {
            logisticsRecyclerViewAdapter.deleteMessageDatas();
        }
    }

    @Override
    public void checkedAll(boolean checked) {
        if (logisticsRecyclerViewAdapter != null) {
            logisticsRecyclerViewAdapter.checkedAll(checked);
        }
    }
}
