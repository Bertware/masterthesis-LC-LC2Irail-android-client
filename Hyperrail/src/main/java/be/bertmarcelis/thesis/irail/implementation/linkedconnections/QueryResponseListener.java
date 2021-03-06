package be.bertmarcelis.thesis.irail.implementation.linkedconnections;

import android.support.annotation.NonNull;

import be.bertmarcelis.thesis.irail.contracts.IRailErrorResponseListener;
import be.bertmarcelis.thesis.irail.contracts.IRailSuccessResponseListener;

/**
 * Created in be.hyperrail.android.irail.implementation.linkedconnections on 18/04/2018.
 */
public class QueryResponseListener implements IRailErrorResponseListener, IRailSuccessResponseListener<LinkedConnections> {
    private final LinkedConnectionsProvider mProvider;
    private final LinkedConnectionsQuery mQueryFunction;

    public QueryResponseListener(LinkedConnectionsProvider provider, LinkedConnectionsQuery query) {
        mProvider = provider;
        mQueryFunction = query;
    }

    @Override
    public void onSuccessResponse(@NonNull LinkedConnections data, Object tag) {
        int status = mQueryFunction.onQueryResult(data);
        if (status < 0) {
            mProvider.getLinkedConnectionsByUrl(data.previous, this, this, tag);
        }
        if (status > 0) {
            mProvider.getLinkedConnectionsByUrl(data.next, this, this, tag);
        }
    }

    @Override
    public void onErrorResponse(@NonNull Exception e, Object tag) {
        mQueryFunction.onQueryFailed(e, tag);
    }

    public interface LinkedConnectionsQuery {
        int onQueryResult(LinkedConnections data);

        void onQueryFailed(Exception e, Object tag);
    }

}
