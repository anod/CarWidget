package com.anod.car.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.anod.car.home.actionbarcompat.ActionBarActivity;
import com.example.android.wizardpager.wizard.ui.StepPagerStrip;

/**
 * @author alex
 * @date 5/24/13
 */
public class WizardActivity extends ActionBarActivity {
	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static final int NUM_PAGES = 5;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally to access previous
	 * and next wizard steps.
	 */
	private ViewPager mPager;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	private StepPagerStrip mStepPagerStrip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_activity);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);

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

		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				mStepPagerStrip.setCurrentPage(position);
			}
		});
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
	 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
	 * sequence.
	 */
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return new ScreenSlidePageFragment(position);
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	public static class ScreenSlidePageFragment extends Fragment {
		private int mPosition;
		private static int[] sFragments = new int[] {
			R.layout.wizard_fragment_1,
			R.layout.wizard_fragment_1,
			R.layout.wizard_fragment_1,
			R.layout.wizard_fragment_1,
			R.layout.wizard_fragment_1
		};
		public ScreenSlidePageFragment(int mPosition) {
			this.mPosition = mPosition;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ViewGroup rootView = (ViewGroup) inflater.inflate(sFragments[mPosition], container, false);
			return rootView;
		}
	}
}