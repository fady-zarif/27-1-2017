package com.tromke.mydrive;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tromke.mydrive.Fragments.LocationFragment;
import com.tromke.mydrive.Fragments.TripsFragment;

/**
 * Created by drrao on 1/6/2017.
 */
public class Pager extends FragmentStatePagerAdapter {
    int tabCount;

    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new LocationFragment();
            case 1:

                return new TripsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof TripsFragment) {
            ((TripsFragment)object).refresh();
        }
        return super.getItemPosition(object);
    }
}
