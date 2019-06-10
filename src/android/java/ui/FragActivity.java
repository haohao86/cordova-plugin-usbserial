package ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.nlscan.ComAssistant.R;
import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;

import java.util.ArrayList;

import domain.TabEntity;

/**
 * Created by Lenovo on 2017/3/27.
 */

public class FragActivity extends AppCompatActivity {

    private static final String TAG = "FragActivity";
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {
            "COM", "HID-POS", "USB serial"
    };


    private MyPagerAdapter mAdapter;

    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();

    private CommonTabLayout commonTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frgm);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorTablayout));
        }
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        final ViewPager vp =(ViewPager) findViewById(R.id.view_pager);

//        for (String title : mTitles) {
//            mFragments.add(SimpleCardFragment.getInstance(title));
//        }
        mFragments.add(new ComFragment());
        mFragments.add(new HidposFragment());
        mFragments.add(new CdcFragment());

        for  (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i],0,0));
        }

        mAdapter = new MyPagerAdapter(getSupportFragmentManager(),mFragments);
        vp.setAdapter(mAdapter);

//        SlidingTabLayout slidingTabLayout =(SlidingTabLayout) findViewById(R.id.sliding_layout);
//        slidingTabLayout.setViewPager(vp);

        commonTabLayout = (CommonTabLayout)findViewById(R.id.common_layout);

        commonTabLayout.setTabData(mTabEntities);
        commonTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                vp.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
                if (position == 0) {
//                    commonTabLayout.showMsg(0, mRandom.nextInt(100) + 1);
//                    UnreadMsgUtils.show(mTabLayout_2.getMsgView(0), mRandom.nextInt(100) + 1);
                }
            }
        });

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                commonTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int screenW = dm.widthPixels;
//        Log.i(TAG,"width:"+screenW);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }
        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar,menu);
//        return true;
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.backup:
//                showToast("back up");
//                break;
//            case R.id.delete:
//                showToast("delete");
//                break;
//            case R.id.settings:
//                showToast("settings");
//                break;
//            default:
//                break;
//        }
//        return true;
//    }
//
//    private void showToast(String msg){
//        Toast.makeText(FragActivity.this,msg, Toast.LENGTH_SHORT).show();
//    }
}
