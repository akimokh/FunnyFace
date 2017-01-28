package com.example.yonko.funnyface.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.example.yonko.funnyface.R;
import com.example.yonko.funnyface.adapters.GridFragmentPagerAdapter;

public class StickerChooserActivity extends AppCompatActivity implements GridFragmentPagerAdapter.ItemClickCallback{
    private static final String TAG = StickerChooserActivity.class.getSimpleName();

    public static String RESULT_KEY = "RESULT_KEY";
    public static String POSITION = "POSITION";
    
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private GridFragmentPagerAdapter mGridFragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        setContentView(R.layout.activity_sticker_chooser);
        setupWindowAnimations();

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Face Part");
        }

        mGridFragmentPagerAdapter = new GridFragmentPagerAdapter(this, getSupportFragmentManager());
        mGridFragmentPagerAdapter.setOnItemClickCallback(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mGridFragmentPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        Bundle lastPositionBundle = getIntent().getExtras();
        if (lastPositionBundle != null) {
            int lastPosition = lastPositionBundle.getInt(POSITION, -1);
            if(lastPosition > -1) {
                mViewPager.setCurrentItem(lastPosition);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, mTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mViewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
    }

    @Override
    public void onItemClick(Object item) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_KEY, (String) item);
        resultIntent.putExtra(POSITION, mViewPager.getCurrentItem());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /*
    * Setup transition animations
    * */
    private void setupWindowAnimations() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition enterTrans = new Explode();
            getWindow().setEnterTransition(enterTrans);

            Transition returnTrans = new Slide();
            getWindow().setReturnTransition(returnTrans);
        }
    }
}
