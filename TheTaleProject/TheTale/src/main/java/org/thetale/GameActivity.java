package org.thetale;

import org.thetale.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private static final int HERO_DATA_LOADER_ID = 1;
    private static final int JOURNAL_DATA_LOADER_ID = 2;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private RequestManager requestManager;

    private LoaderManager.LoaderCallbacks<Cursor> myJournalListLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    GameActivity.this,
                    ServerContract.Journal.CONTENT_URI,
                    ServerContract.Journal.COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            myAdapter.swapCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            myAdapter.swapCursor(null);
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> myHeroLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    GameActivity.this,
                    ServerContract.HeroData.CONTENT_URI,
                    ServerContract.HeroData.ALL_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            applyHeroDataCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            applyHeroDataCursor(null);
        }
    };
    private Loader myHeroDataCursorLoader;
    private SimpleCursorAdapter myAdapter;
    private Loader<Cursor> myJournalDataCursorLoader;

    void applyHeroDataCursor(final Cursor cursor) {
        if (cursor == null) {

        } else {
            if (!cursor.moveToFirst()) return; // empty
            setTextViewValue(cursor, R.id.name_value, ServerContract.HeroData.NAME);
            setTextViewValue(cursor, R.id.race_value, ServerContract.HeroData.RACE);
            setTextViewValue(cursor, R.id.exp_value, ServerContract.HeroData.EXPERIENCE);
            setTextViewValue(cursor, R.id.exp_to_lvl_value, ServerContract.HeroData.EXP_TO_NEXT_LEVEL);
            setTextViewValue(cursor, R.id.hp_value, ServerContract.HeroData.HEALTH);
            setTextViewValue(cursor, R.id.max_hp_value, ServerContract.HeroData.MAX_HEALTH);
            setTextViewValue(cursor, R.id.power_value, ServerContract.HeroData.DESTINY_POINTS);
            setTextViewValue(cursor, R.id.current_action_value, ServerContract.HeroData.CURRENT_ACTION_DESCRIPTION);
        }
    }

    private void setTextViewValue(Cursor cursor, int textViewId, String columnName) {
        TextView v = (TextView) findViewById(textViewId);
        v.setText(cursor.getString(cursor.getColumnIndex(columnName)));
    }

    RequestManager.RequestListener requestListener = new RequestManager.RequestListener() {

        @Override
        public void onRequestFinished(Request request, Bundle resultData) {
            myHeroDataCursorLoader.startLoading();
            myJournalDataCursorLoader.startLoading();

//            myJournalListLoaderCallbacks.setRefreshing();
//            Request updateRequest = new Request(RequestFactory.ALL_DATA_REQUEST);
//            updateRequest.put("screen_name", "habrahabr");
//            requestManager.execute(updateRequest, requestListener);

//            TextView race_data = (TextView) findViewById(R.id.race_data);
            enableButton();
        }

        private void showError() {
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(getString(R.string.background)).
                    create().
                    show();
        }

        @Override
        public void onRequestDataError(Request request) {
            showError();
            enableButton();
        }

        @Override
        public void onRequestCustomError(Request request, Bundle resultData) {
            showError();
            enableButton();
        }

        @Override
        public void onRequestConnectionError(Request request, int statusCode) {
            showError();
            enableButton();
        }
    };

    public void enableButton() {
        findViewById(R.id.update_button).setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
//        final View contentView = findViewById(R.id.background_text);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, controlsView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        final View updateButton = findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                update();
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
//        contentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (TOGGLE_ON_CLICK) {
//                    mSystemUiHider.toggle();
//                } else {
//                    mSystemUiHider.show();
//                }
//            }
//        });

        requestManager = new RequestManager(this, ServerCommunicationService.class) {
        };

        ListView myJournalListView = (ListView) findViewById(R.id.journal_list);

        myAdapter = new SimpleCursorAdapter(this, R.layout.journal_list_layout, null, new String[]{ServerContract.Journal.DATE_V, ServerContract.Journal.TIME_V, ServerContract.Journal.DESCRIPTION}, new int[]{R.id.journal_date, R.id.journal_time, R.id.journal_message}, 0);
        myJournalListView.setAdapter(myAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        myHeroDataCursorLoader = getLoaderManager().initLoader(HERO_DATA_LOADER_ID, null, myHeroLoaderCallbacks);
        myJournalDataCursorLoader = getLoaderManager().initLoader(JOURNAL_DATA_LOADER_ID, null, myJournalListLoaderCallbacks);
    }

    public void update() {
        Request updateRequest = new Request(RequestFactory.ALL_DATA_REQUEST);
        updateRequest.put("account", "1");
        requestManager.execute(updateRequest, requestListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
