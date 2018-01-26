package com.c3.jbz.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.c3.jbz.R;
import com.c3.jbz.dummy.DummyContent;
import com.c3.jbz.dummy.DummyContent.DummyItem;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.vo.MessageInfo;

/**
 *
 */
public class MessageFragment extends Fragment implements MessageView{
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private MessagePresenter messagePresenter;

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

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MessageRecyclerViewAdapter(messagePresenter));
        }
        return view;
    }

    @Override
    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter=messagePresenter;
    }
}
