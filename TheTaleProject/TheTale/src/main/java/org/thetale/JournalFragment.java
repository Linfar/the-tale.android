package org.thetale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxykeep.datadroid.requestmanager.RequestManager;

/**
 * Created by Andrey.Titov on 10/18/13.
 */
public class JournalFragment extends Fragment {
    private final RequestManager myRequestManager;
    private final Context myParentContext;

    public JournalFragment(Context parentContext, RequestManager requestManager) {
        myParentContext = parentContext;
        myRequestManager = requestManager;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.journal_fragment_layout, container, false);
        assert inflate != null;
//        final View updateButton = inflate.findViewById(R.id.update_button);
//        assert updateButton != null;
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                v.setEnabled(false);
//                update();
//            }
//        });

//        ListView myJournalListView = (ListView) inflate.findViewById(R.id.journal_list);
//        assert myJournalListView != null;

//        myAdapter = new SimpleCursorAdapter(myParentContext, R.layout.journal_list_layout, null, new String[]{ServerContract.Journal.DATE_V, ServerContract.Journal.TIME_V, ServerContract.Journal.DESCRIPTION}, new int[]{R.id.journal_date, R.id.journal_time, R.id.journal_message}, 0);
//        myJournalListView.setAdapter(myAdapter);

        return inflate;
    }
}