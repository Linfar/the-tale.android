package org.thetale;

import com.foxykeep.datadroid.service.RequestService;

/**
 * Created by Andrey.Titov on 9/19/13.
 */
public class ServerCommunicationService extends RequestService {
    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
            case RequestFactory.ALL_DATA_REQUEST:
                return new ServerOperation();
            default:
                return null;
        }
    }

}
