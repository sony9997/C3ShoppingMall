package com.c3.jbz.fragment;

import com.c3.jbz.presenter.MessagePresenter;

/**
 * @author hedong
 * @date 2018/1/26
 */

public interface MessageView<MessageData> {
    void setMessagePresenter(MessagePresenter messagePresenter);

    void addData(MessageData data);

    void deleteMessageDatas();

    void checkedAll(boolean checked);
}
