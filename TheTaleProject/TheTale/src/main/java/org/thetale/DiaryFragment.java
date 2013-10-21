package org.thetale;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

/**
 * Created by Andrey.Titov on 10/18/13.
 */
public class DiaryFragment extends Fragment {
    private final Context myParentContext;
    private final RequestManager myRequestManager;
    private SimpleCursorAdapter myAdapter;

    private Loader myHeroDataCursorLoader;
    private Loader<Cursor> myJournalDataCursorLoader;

    private LoaderManager.LoaderCallbacks<Cursor> myHeroLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    myParentContext,
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
        TextView v = (TextView) getView().findViewById(textViewId);
        v.setText(cursor.getString(cursor.getColumnIndex(columnName)));
    }

    public void enableButton() {
        getView().findViewById(R.id.update_button).setEnabled(true);
    }

    public void update() {
        Request updateRequest = new Request(RequestFactory.ALL_DATA_REQUEST);
        updateRequest.put("account", "1");
        myRequestManager.execute(updateRequest, requestListener);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(myParentContext);
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

    private LoaderManager.LoaderCallbacks<Cursor> myJournalListLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
            return new CursorLoader(
                    myParentContext,
                    ServerContract.Journal.CONTENT_URI,
                    ServerContract.Journal.COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
            myAdapter.changeCursor(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            myAdapter.changeCursor(null);
        }
    };

    public DiaryFragment(Context parentContext, RequestManager requestManager) {
        myParentContext = parentContext;
        myRequestManager = requestManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.diary_fragment_layout, container, false);
        assert inflate != null;
        final View updateButton = inflate.findViewById(R.id.update_button);
        assert updateButton != null;
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                update();
            }
        });

        ListView myJournalListView = (ListView) inflate.findViewById(R.id.journal_list);
        assert myJournalListView != null;

        myAdapter = new SimpleCursorAdapter(myParentContext, R.layout.journal_list_layout, null, new String[]{ServerContract.Journal.DATE_V, ServerContract.Journal.TIME_V, ServerContract.Journal.DESCRIPTION}, new int[]{R.id.journal_date, R.id.journal_time, R.id.journal_message}, 0);
        myJournalListView.setAdapter(myAdapter);

        return inflate;
    }

    @Override
    public void onStart() {
        super.onStart();
        myJournalDataCursorLoader = getLoaderManager().initLoader(GameActivity.JOURNAL_DATA_LOADER_ID, null, myJournalListLoaderCallbacks);
        myHeroDataCursorLoader = getLoaderManager().initLoader(GameActivity.HERO_DATA_LOADER_ID, null, myHeroLoaderCallbacks);
    }
}