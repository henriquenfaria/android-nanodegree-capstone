package com.henriquenfaria.wisetrip.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.henriquenfaria.wisetrip.R;
import com.henriquenfaria.wisetrip.data.TempSimpleRecyclerAdapter;
import com.henriquenfaria.wisetrip.data.ViewPagerAdapter;
import com.henriquenfaria.wisetrip.fragments.ExpenseListFragment;
import com.henriquenfaria.wisetrip.models.Attribution;
import com.henriquenfaria.wisetrip.models.TempVersionModel;
import com.henriquenfaria.wisetrip.models.Trip;
import com.henriquenfaria.wisetrip.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/* Activity to display all related data of a specific Trip */
public class TripDetailsActivity extends AppCompatActivity {

    private static final String SAVE_IS_SHARED_ELEMENT_TRANSITION =
            "save_is_shared_element_transition";
    private static final int TAB_EXPENSES_POSITION = 0;
    private static final int TAB_BUDGETS_POSITION = 1;
    private static final int TAB_PLACES_POSITION = 2;

    @BindView(R.id.attribution_container)
    protected LinearLayout mAttributionContainer;
    @BindView(R.id.trip_photo)
    protected ImageView mTripPhoto;
    @BindView(R.id.trip_photo_protection)
    protected View mTripPhotoProtection;
    @BindView(R.id.attribution_prefix)
    protected TextView mAttributionPrefix;
    @BindView(R.id.trip_title)
    protected TextView mTripTitle;
    @BindView(R.id.attribution_content)
    protected TextView mAttributionContent;
    @BindView(R.id.fab)
    protected FloatingActionButton mFab;
    @BindView(R.id.tablayout)

    protected TabLayout mTabLayout;
    private Trip mTrip;
    private boolean mIsSharedElementTransition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrip = getIntent().getParcelableExtra(Constants.Extra.EXTRA_TRIP);
        if (mTrip == null) {
            Toast.makeText(this, R.string.could_not_load_trip_details, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_trip_details);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            mIsSharedElementTransition = savedInstanceState.getBoolean
                    (SAVE_IS_SHARED_ELEMENT_TRANSITION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        } else {
            supportPostponeEnterTransition();
        }


        setTripBackdropPhoto(mTrip.getId());
        setupAppBarLayout();
        setupToolbar();
        setupFab();
        setupViewPager();
        setTransitionNames();
    }

    private void setupToolbar() {
        Toolbar toolbar = ButterKnife.findById(TripDetailsActivity.this, R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = ButterKnife.findById(toolbar, R.id.trip_title);
        title.setText(mTrip.getTitle());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }
    }

    private void setupFab() {
        mFab.setImageResource(getFabImageResource());
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mTabLayout.getSelectedTabPosition()) {
                    case TAB_EXPENSES_POSITION:
                        Intent intent = new Intent(TripDetailsActivity.this,
                                ExpenseFactoryActivity.class);
                        intent.putExtra(Constants.Extra.EXTRA_TRIP, mTrip);
                        startActivity(intent);
                        break;
                    case TAB_BUDGETS_POSITION:
                        //TODO: Implement
                        Toast.makeText(TripDetailsActivity.this, R.string.budgets,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case TAB_PLACES_POSITION:
                        //TODO: Implement
                        Toast.makeText(TripDetailsActivity.this, R.string.places,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void setupAppBarLayout() {
        AppBarLayout appbarLayout = ButterKnife.findById(TripDetailsActivity.this, R.id
                .appbarlayout);
        appbarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case COLLAPSED:
                    case IDLE:
                        mIsSharedElementTransition = false;
                        break;
                    case EXPANDED:
                        mIsSharedElementTransition = true;
                        break;
                }

            }
        });
    }

    private void setTransitionNames() {
        String tripId = mTrip.getId();
        ViewCompat.setTransitionName(mTripPhoto,
                Constants.Transition.PREFIX_TRIP_PHOTO + tripId);
        ViewCompat.setTransitionName(mTripPhotoProtection,
                Constants.Transition.PREFIX_TRIP_PHOTO_PROTECTION + tripId);
        ViewCompat.setTransitionName(mAttributionContainer,
                Constants.Transition.PREFIX_TRIP_ATTRIBUTION + tripId);
        ViewCompat.setTransitionName(mTripTitle,
                Constants.Transition.PREFIX_TRIP_TITLE + tripId);

    }

    private int getFabImageResource() {
        switch (mTabLayout.getSelectedTabPosition()) {
            case TAB_EXPENSES_POSITION:
                return R.drawable.ic_fab_expense;
            case TAB_BUDGETS_POSITION:
                return R.drawable.ic_fab_budget;
            case TAB_PLACES_POSITION:
                return R.drawable.ic_fab_place;
            default:
                return R.drawable.ic_fab_plus;
        }
    }

    private void animateFab() {
        if (!mFab.isShown()) {
            mFab.setImageResource(getFabImageResource());
            mFab.show();
        } else {
            mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    mFab.setImageResource(getFabImageResource());
                    mFab.show();
                }
            });
        }
    }


    private void setupViewPager() {
        ViewPager viewPager = ButterKnife.findById(TripDetailsActivity.this, R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //TODO: Temp code
        /*adapter.addFragment(new TempDummyFragment(android.R.color.background_light), getString(R
                .string.expenses));*/
        adapter.addFragment(ExpenseListFragment.newInstance(mTrip), getString(R.string.expenses));
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), getString(R
                .string.budgets));
        adapter.addFragment(new TempDummyFragment(android.R.color.background_light), getString(R
                .string.places));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                animateFab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_IS_SHARED_ELEMENT_TRANSITION, mIsSharedElementTransition);
        super.onSaveInstanceState(outState);
    }

    private void setTripBackdropPhoto(String tripId) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directoryFile = cw.getDir(Constants.General.DESTINATION_PHOTO_DIR,
                Context.MODE_PRIVATE);
        final File photoFile = new File(directoryFile, tripId);

        ImageView tripPhotoBackdrop = ButterKnife.findById(this, R.id.trip_photo);
        Picasso.with(this)
                .load(photoFile)
                .networkPolicy(
                        NetworkPolicy.NO_CACHE,
                        NetworkPolicy.NO_STORE,
                        NetworkPolicy.OFFLINE)
                .noFade()
                .noPlaceholder()
                .error(R.drawable.trip_photo_default)
                .into(tripPhotoBackdrop, new Callback() {
                    @Override
                    public void onSuccess() {
                        displayPhotoAttribution(mTrip.getId(), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }

                    @Override
                    public void onError() {
                        displayPhotoAttribution(mTrip.getId(), false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        } else {
                            supportStartPostponedEnterTransition();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // To animate transition like back button press
                if (mIsSharedElementTransition) {
                    supportFinishAfterTransition();
                } else {
                    finish();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mIsSharedElementTransition) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    private void displayPhotoAttribution(String tripId, boolean shouldDisplay) {


        if (mAttributionContainer != null) {
            if (shouldDisplay) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference attributionsReference = firebaseDatabase
                        .getReference()
                        .child("attributions")
                        .child(currentUser.getUid())
                        .child(tripId);

                attributionsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Timber.d("onDataChange");
                        if (mAttributionContainer != null) {
                            Attribution attribution = dataSnapshot.getValue(Attribution.class);
                            if (attribution != null && !TextUtils.isEmpty(attribution.getText())) {
                                Spanned result;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    result = Html.fromHtml(attribution.getText(),
                                            Html.FROM_HTML_MODE_LEGACY);
                                } else {
                                    result = Html.fromHtml(attribution.getText());
                                }

                                mAttributionContent.setText(result);
                                mAttributionContent.setMovementMethod(
                                        LinkMovementMethod.getInstance());
                                mAttributionPrefix.setVisibility(View.VISIBLE);
                                mAttributionContent.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.d("onCancelled");
                        //TODO: Implementation needed?
                    }
                });
            } else {
                mAttributionPrefix.setVisibility(View.GONE);
                mAttributionContent.setVisibility(View.GONE);
            }
        }
    }

    //TODO: Temp dummy Fragment code
    public static class TempDummyFragment extends Fragment {
        int color;
        TempSimpleRecyclerAdapter adapter;

        public TempDummyFragment() {
        }

        @SuppressLint("ValidFragment")
        public TempDummyFragment(int color) {
            this.color = color;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
                savedInstanceState) {
            View view = inflater.inflate(R.layout.temp_dummy_fragment, container, false);

            final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.dummyfrag_bg);
            frameLayout.setBackgroundColor(color);

            RecyclerView recyclerView = (RecyclerView) view.findViewById(
                    R.id.dummyfrag_scrollableview);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()
                    .getBaseContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setHasFixedSize(true);

            List<String> list = new ArrayList<>();
            for (int i = 0; i < TempVersionModel.data.length; i++) {
                list.add(TempVersionModel.data[i]);
            }

            adapter = new TempSimpleRecyclerAdapter(list);
            recyclerView.setAdapter(adapter);

            return view;
        }
    }

    private static abstract class AppBarStateChangeListener implements
            AppBarLayout.OnOffsetChangedListener {

        private State mCurrentState = State.IDLE;

        @Override
        public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED);
                }
                mCurrentState = State.EXPANDED;
            } else if (Math.abs(i) >= appBarLayout.getTotalScrollRange()) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED);
                }
                mCurrentState = State.COLLAPSED;
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE);
                }
                mCurrentState = State.IDLE;
            }
        }

        public abstract void onStateChanged(AppBarLayout appBarLayout, State state);

        public enum State {
            EXPANDED,
            COLLAPSED,
            IDLE
        }
    }
}
