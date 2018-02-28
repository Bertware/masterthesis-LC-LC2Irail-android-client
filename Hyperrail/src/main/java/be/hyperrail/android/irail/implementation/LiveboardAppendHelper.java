/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package be.hyperrail.android.irail.implementation;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Objects;

import be.hyperrail.android.irail.contracts.IRailErrorResponseListener;
import be.hyperrail.android.irail.contracts.IRailSuccessResponseListener;
import be.hyperrail.android.irail.contracts.IrailDataProvider;
import be.hyperrail.android.irail.contracts.RouteTimeDefinition;
import be.hyperrail.android.irail.factories.IrailFactory;
import be.hyperrail.android.irail.implementation.requests.IrailLiveboardRequest;
import be.hyperrail.android.util.ArrayUtils;

/**
 * A class which allows to append liveboards.
 * TODO: move to IrailApi API implementation as this is API specific
 */
public class LiveboardAppendHelper implements IRailSuccessResponseListener<LiveBoard>, IRailErrorResponseListener {

    private final int TAG_APPEND = 0;
    private final int TAG_PREPEND = 1;

    private int attempt = 0;
    private DateTime lastSearchTime;
    private LiveBoard originalLiveboard;

    private IRailSuccessResponseListener<LiveBoard> successResponseListener;
    private IRailErrorResponseListener errorResponseListener;

    IrailDataProvider api = IrailFactory.getDataProviderInstance();

    public void appendLiveboard(@NonNull LiveBoard liveBoard, IRailSuccessResponseListener<LiveBoard> successResponseListener,
                                IRailErrorResponseListener errorResponseListener) {

        this.successResponseListener = successResponseListener;
        this.errorResponseListener = errorResponseListener;

        this.originalLiveboard = liveBoard;

        if (liveBoard.getStops().length > 0) {
            if (liveBoard.getStops()[liveBoard.getStops().length - 1].getType() == VehicleStopType.DEPARTURE) {
                this.lastSearchTime = liveBoard.getStops()[liveBoard.getStops().length - 1].getDepartureTime().plusMinutes(1);
            } else {
                this.lastSearchTime = liveBoard.getStops()[liveBoard.getStops().length - 1].getArrivalTime().plusMinutes(1);
            }
        } else {
            this.lastSearchTime = liveBoard.getSearchTime().plusHours(1);
        }

        IrailLiveboardRequest request = new IrailLiveboardRequest(liveBoard, liveBoard.getTimeDefinition(), lastSearchTime);
        request.setCallback(this, this, TAG_APPEND);
        api.getLiveboard(request);
    }

    public void prependLiveboard(@NonNull LiveBoard liveBoard, IRailSuccessResponseListener<LiveBoard> successResponseListener,
                                 IRailErrorResponseListener errorResponseListener) {
        this.successResponseListener = successResponseListener;
        this.errorResponseListener = errorResponseListener;

        this.originalLiveboard = liveBoard;

        if (liveBoard.getStops().length > 0) {
            if (liveBoard.getStops()[liveBoard.getStops().length - 1].getType() == VehicleStopType.DEPARTURE) {
                this.lastSearchTime = liveBoard.getStops()[0].getDepartureTime().minusHours(1);
            } else {
                this.lastSearchTime = liveBoard.getStops()[0].getArrivalTime().minusHours(1);
            }
        } else {
            this.lastSearchTime = liveBoard.getSearchTime().minusHours(1);
        }
        IrailLiveboardRequest request = new IrailLiveboardRequest(liveBoard, liveBoard.getTimeDefinition(), lastSearchTime);
        request.setCallback(this, this, TAG_PREPEND);
        api.getLiveboardBefore(request);
    }

    @Override
    public void onSuccessResponse(@NonNull LiveBoard data, Object tag) {
        switch ((int) tag) {
            case TAG_APPEND:

                VehicleStop[] newStops = data.getStops();

                if (newStops.length > 0) {
                    // It can happen that a scheduled departure was before the search time.
                    // In this case, prevent duplicates by searching the first stop which isn't before
                    // the searchdate, and removing all earlier stops.
                    int i = 0;

                    if (data.getTimeDefinition() == RouteTimeDefinition.DEPART) {
                        while (i < newStops.length && newStops[i].getDepartureTime().isBefore(data.getSearchTime())) {
                            i++;
                        }
                    } else {
                        while (i < newStops.length && newStops[i].getArrivalTime().isBefore(data.getSearchTime())) {
                            i++;
                        }
                    }

                    if (i > 0) {
                        if (i <= data.getStops().length - 1) {
                            newStops = Arrays.copyOfRange(data.getStops(), i, data.getStops().length - 1);
                        } else {
                            newStops = new VehicleStop[0];
                        }
                    }
                }

                if (newStops.length > 0) {

                    VehicleStop[] mergedStops = ArrayUtils.concatenate(originalLiveboard.getStops(), newStops);
                    LiveBoard merged = new LiveBoard(originalLiveboard, mergedStops, originalLiveboard.getSearchTime(), originalLiveboard.getTimeDefinition());
                    this.successResponseListener.onSuccessResponse(merged, tag);
                } else {
                    // No results, search two hours further in case this day doesn't have results.
                    // Skip 2 hours at once, possible due to large API pages.
                    attempt++;
                    lastSearchTime = lastSearchTime.plusHours(2);

                    if (attempt < 12) {
                        IrailLiveboardRequest request = new IrailLiveboardRequest(originalLiveboard, RouteTimeDefinition.DEPART, lastSearchTime);
                        request.setCallback(this, this, TAG_APPEND);
                        api.getLiveboard(request);
                    } else {
                        if (this.successResponseListener != null) {
                            this.successResponseListener.onSuccessResponse(originalLiveboard, this);
                        }
                    }
                }
                break;
            case TAG_PREPEND:
                if (data.getStops().length > 0) {
                    // TODO: prevent duplicates by checking arrival time
                    // Both arrays are sorted chronologically
                    // Scanning back-to-front in the original array is O(n), which is acceptable for now
                    // Binary search would be tricky since trains might have a new delay, they are chronologically based on the actual real departure time!
                    VehicleStop[] originalStops = null;
                    for (int i = originalLiveboard.getStops().length - 1; i >= 0 && originalStops == null; i--) {
                        VehicleStop s = originalLiveboard.getStops()[i];
                        if (Objects.equals(s.getDepartureSemanticId(), data.getStops()[data.getStops().length - 1].getDepartureSemanticId())) {
                            // All before this stop are duplicates
                            // TODO: investigate if this code is totally bug free: aren't we ignoring too much?
                            originalStops = Arrays.copyOfRange(originalLiveboard.getStops(), i + 1, originalLiveboard.getStops().length - 1);
                        }
                    }
                    if (originalStops == null) {
                        originalStops = originalLiveboard.getStops();
                    }
                    VehicleStop[] mergedStops = ArrayUtils.concatenate(data.getStops(), originalStops);
                    LiveBoard merged = new LiveBoard(originalLiveboard, mergedStops, originalLiveboard.getSearchTime(), originalLiveboard.getTimeDefinition());
                    this.successResponseListener.onSuccessResponse(merged, tag);
                } else {
                    attempt++;
                    lastSearchTime = lastSearchTime.minusHours(1);

                    if (attempt < 12) {
                        IrailLiveboardRequest request = new IrailLiveboardRequest(originalLiveboard, RouteTimeDefinition.DEPART, lastSearchTime);
                        request.setCallback(this, this, TAG_PREPEND);
                        api.getLiveboardBefore(request);
                    } else {
                        if (this.successResponseListener != null) {
                            this.successResponseListener.onSuccessResponse(originalLiveboard, this);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onErrorResponse(@NonNull Exception e, Object tag) {
        if (this.errorResponseListener != null) {
            this.errorResponseListener.onErrorResponse(e, this);
        }
    }
}
