package com.c3.jbz.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.c3.jbz.BuildConfig;
import com.c3.jbz.R;
import com.c3.jbz.fragment.LogisticsFragment;
import com.c3.jbz.fragment.MessageFragment;
import com.c3.jbz.fragment.MessageView;
import com.c3.jbz.fragment.NoticeFragment;
import com.c3.jbz.presenter.MessagePresenter;
import com.c3.jbz.util.ToolsUtil;

public class MessagesActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener, View.OnClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout tabLayout;

    private MessagePresenter messagePresenter;
    private View tvChoiceAll;
    private String[] tabs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ToolsUtil.setStatusBarColor(this);
        messagePresenter = new MessagePresenter(this);
        tabs = getResources().getStringArray(R.array.section_format);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // tabLayout使用viewPager接收的tabSectionAdapter里设置的title
        tabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
            }
        }

        messagePresenter.parseBunlde(getIntent().getExtras());
        tabLayout.addOnTabSelectedListener(this);
        tvChoiceAll = findViewById(R.id.tv_choice_all);
        tvChoiceAll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    tvChoiceAll.setSelected(!tvChoiceAll.isSelected());
                }
                return false;
            }
        });
        findViewById(R.id.tv_delete).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (messagePresenter != null)
            messagePresenter.parseBunlde(intent.getExtras());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messagePresenter = null;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back: {
                finish();
                break;
            }
            case R.id.tv_delete: {
                tvChoiceAll.setSelected(!tvChoiceAll.isSelected());
                break;
            }
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            MessageView messageView = null;
            switch (position) {
                case BuildConfig.MSG_TYPE_NORMAL: {
                    messageView = new MessageFragment();
                    break;
                }
                case BuildConfig.MSG_TYPE_NOTICE: {
                    messageView = new NoticeFragment();
                    break;
                }
                case BuildConfig.MSG_TYPE_LOGISTICS: {
                    messageView = new LogisticsFragment();
                    break;
                }
            }
            if (messageView != null) {
                messageView.setMessagePresenter(messagePresenter);
            }
            return (Fragment) messageView;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }

        public View getTabView(int position) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_custom, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_tab_title);
            tv.setText(tabs[position]);

            if(messagePresenter.isRedDotNeedShow(position)){
                View redDot=view.findViewById(R.id.iv_dot);
                redDot.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    public void selectTab(int index, int notificationId) {
        if (index >= 0 && index < tabLayout.getTabCount()) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
            tabLayout.getTabAt(index).select();
        }
    }

    public void updateRedDotState(int position, boolean show) {
        if (position >= 0 && position < tabLayout.getTabCount()) {
            tabLayout.getTabAt(position).getCustomView().findViewById(R.id.iv_dot).setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
