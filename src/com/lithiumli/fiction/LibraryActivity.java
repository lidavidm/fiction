package com.lithiumli.fiction;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

import android.support.v4.view.ViewPager;
import android.support.v13.app.FragmentPagerAdapter;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;

import com.lithiumli.fiction.fragments.*;

public class LibraryActivity
    extends FictionActivity
    implements ViewPager.OnPageChangeListener
{
    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    TextView babSongTitle;
    TextView babSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab("Songs", SongsListFragment.class, null);
        mViewPager.setAdapter(mTabsAdapter);

        babSongTitle = (TextView) findViewById(R.id.bab_song_name);
        babSubtitle = (TextView) findViewById(R.id.bab_song_subtitle);
        babSubtitle.setSelected(true);

        getActionBar().setSubtitle("Fiction Music");
        getActionBar().setTitle("Songs");

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.bab);
        layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Intent intent = new Intent(FictionActivity.this,
                    //                            FictionPlaybackActivity.class);
                    // FictionActivity.this.startActivity(intent);
                }
            });
    }

    @Override
    public void onSongChange(Song song) {
        babSongTitle.setText(song.getTitle());
        babSubtitle.setText(song.getArtist() + " - " + song.getAlbum());
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(mTabsAdapter.getPageTitle(position));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int
                                positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class TabsAdapter extends FragmentPagerAdapter {
        private final Context mContext;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final String title;

            TabInfo(Class<?> _class, Bundle _args, String _title) {
                clss = _class;
                args = _args;
                title = _title;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mViewPager = pager;
            mViewPager.setAdapter(this);
        }

        public void addTab(String title, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args, title);
            mTabs.add(info);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).title;
        }
    }
}
