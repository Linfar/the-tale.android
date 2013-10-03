package org.thetale;

import com.foxykeep.datadroid.requestmanager.Request;

/**
 * Created by Andrey.Titov on 9/19/13.
 */
public class RequestFactory {
    public static final int ALL_DATA_REQUEST = 1;

    public static Request getDataRequest(String account) {
        Request r = new Request(ALL_DATA_REQUEST);
        r.put("account", account);
        return r;
    }
}
