package com.anod.car.home.ui;

import com.anod.car.home.R;
import com.anod.car.home.app.CarWidgetActivity;
import com.anod.car.home.prefs.preferences.AppTheme;
import com.anod.car.home.utils.Version;
import com.example.android.wizardpager.wizard.ui.StepPagerStrip;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author alex
 * @date 5/24/13
 */
public class WizardActivity extends CarWidgetActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final Integer TYPE_NEXT = 1;

    private static final Integer TYPE_FINISH = 2;

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

    public static final String EXTRA_PAGE = "page";

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    @Bind(R.id.pager)
    ViewPager mPager;

    @Bind(R.id.buttonNext)
    Button mNextButton;

    @Bind(R.id.buttonSkip)
    Button mSkipButton;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private StepPagerStrip mStepPagerStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_activity);

        ButterKnife.bind(this);
        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        int current = (getIntent() == null) ? 0 : getIntent().getIntExtra(EXTRA_PAGE, 0);
        mPager.setCurrentItem(current);
        mNextButton.setTag(TYPE_NEXT);

        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mPager.getCurrentItem() != position) {
                    mPager.setCurrentItem(position);
                }
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);
                if (position == (NUM_PAGES - 1)) {
                    mNextButton.setTag(TYPE_FINISH);
                    mNextButton.setText(R.string.finish);
                    mSkipButton.setVisibility(View.GONE);
                }
            }
        });
        mStepPagerStrip.setPageCount(NUM_PAGES);

    }

    @Override
    protected int getAppThemeRes(int theme) {
        return AppTheme.getNoActionBarResource(theme);
    }

    private void finishWizard() {
        WizardActivity.this.finish();
    }

    @OnClick(R.id.buttonSkip)
    public void onSkip() {
        finishWizard();
    }

    @OnClick(R.id.buttonNext)
    public void onNext() {
        if (mNextButton.getTag() == TYPE_FINISH) {
            finishWizard();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     *
     */
    public static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ScreenSlidePageFragment f = new ScreenSlidePageFragment();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            return f;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public static class ScreenSlidePageFragment extends Fragment {

        private static int[] sFragments = new int[]{
                R.layout.wizard_fragment_1,
                R.layout.wizard_fragment_2,
                R.layout.wizard_fragment_3,
                R.layout.wizard_fragment_4
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int pos = getArguments().getInt("position", 0);
            int layoutId = sFragments[pos];
            ViewGroup rootView = (ViewGroup) inflater.inflate(layoutId, container, false);

            TextView desc1 = (TextView) rootView.findViewById(R.id.desc1);
            TextView desc2 = (TextView) rootView.findViewById(R.id.desc2);
            if (layoutId == R.layout.wizard_fragment_1) {
                desc1.setText(Html.fromHtml(getString(R.string.welcome_text)));
            } else if (layoutId == R.layout.wizard_fragment_2) {
                desc1.setText(Html.fromHtml(getString(R.string.install_widget)));
            } else if (layoutId == R.layout.wizard_fragment_3) {
                desc1.setText(Html.fromHtml(getString(R.string.configure_text)));
                desc2.setText(Html.fromHtml(getString(R.string.open_settings_description)));
            } else if (layoutId == R.layout.wizard_fragment_4) {
                desc1.setText(Html.fromHtml(getString(R.string.detect_incar_description)));
                desc2.setText(Html.fromHtml(getString(R.string.adjust_incar_description)));
                TextView desc3 = (TextView) rootView.findViewById(R.id.desc3);
                desc3.setText(Html.fromHtml(getString(R.string.enable_incar_description)));

                Version v = new Version(getActivity());
                TextView desc4 = (TextView) rootView.findViewById(R.id.desc4);
                if (v.isFree()) {
                    desc4.setText(Html.fromHtml(
                            getString(R.string.trial_description, v.getMaxTrialTimes())));
                } else {
                    desc4.setVisibility(View.GONE);
                }
            }

            return rootView;
        }
    }
}