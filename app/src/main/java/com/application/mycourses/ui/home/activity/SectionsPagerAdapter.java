package com.application.mycourses.ui.home.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.application.mycourses.R;
import com.application.mycourses.ui.home.activity.tab.MaterialFragment;
import com.application.mycourses.ui.home.activity.tab.MemberFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TILES =new int[]{R.string.material,R.string.member};
    private final Context mContext;

    public SectionsPagerAdapter(@NonNull  Context mContext,  FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new MaterialFragment();
            case 1:
                return new MemberFragment();
            default:
                return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TILES[position]);
    }
}
