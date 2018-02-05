/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package be.hyperrail.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import be.hyperrail.android.R;
import be.hyperrail.android.adapter.OnRecyclerItemClickListener;
import be.hyperrail.android.adapter.RouteDetailCardAdapter;
import be.hyperrail.android.irail.contracts.RouteTimeDefinition;
import be.hyperrail.android.irail.db.Station;
import be.hyperrail.android.irail.implementation.Route;
import be.hyperrail.android.irail.implementation.TrainStub;
import be.hyperrail.android.irail.implementation.Transfer;
import be.hyperrail.android.irail.implementation.requests.IrailLiveboardRequest;

import static be.hyperrail.android.R.string.warning_not_realtime_datetime;

/**
 * Activity to show one specific route
 */
public class RouteDetailActivity extends RecyclerViewActivity<Route> {

    /**
     * The route to show
     */
    private Route route;
    private TextView vAlertsText;

    public static Intent createIntent(Context c, Route r) {
        Intent i = new Intent(c, RouteDetailActivity.class);
        i.putExtra("route", r);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        route = (Route) getIntent().getSerializableExtra("route");
        this.mShowDividers = false;

        super.onCreate(savedInstanceState);

        setTitle(route.getDepartureStation().getLocalizedName() + " - " + route.getArrivalStation().getLocalizedName());

        DateTimeFormatter df = DateTimeFormat.forPattern(getString(warning_not_realtime_datetime));
        setSubTitle(df.print(route.getDepartureTime()));

        // disable pull-to-refresh
        // TODO: support refreshing
        this.vRefreshLayout.setEnabled(false);

        this.vWarningNotRealtime.setVisibility(View.GONE);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_route_detail;
    }

    @Override
    protected int getMenuLayout() {
        // TODO: include notification options
        return R.menu.actionbar_main;
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        RouteDetailCardAdapter adapter = new RouteDetailCardAdapter(this, route, false);

        // Launch intents to view details / click through
        adapter.setOnItemClickListener(new OnRecyclerItemClickListener<Object>() {
            @Override
            public void onRecyclerItemClick(RecyclerView.Adapter sender, Object object) {
                Intent i = null;
                if (object instanceof Bundle) {
                    i = TrainActivity.createIntent(RouteDetailActivity.this,
                            (TrainStub) ((Bundle) object).getSerializable("train"),
                            (Station) ((Bundle) object).getSerializable("from"),
                            (Station) ((Bundle) object).getSerializable("to"),
                            (DateTime) ((Bundle) object).getSerializable("date"));

                } else if (object instanceof Transfer) {
                    i = LiveboardActivity.createIntent(RouteDetailActivity.this, new IrailLiveboardRequest(((Transfer) object).getStation(), RouteTimeDefinition.DEPART, null));
                }
                startActivity(i);
            }
        });
        return adapter;
    }

    @Override
    protected void getData() {
        // Not supported
    }

    @Override
    protected void getInitialData() {
        route = (Route) getIntent().getSerializableExtra("route");
    }

    @Override
    protected void showData(Route data) {
        // Not supported, already showing data by setting route on create
    }

    @Override
    public void markFavorite(boolean favorite) {
        // Not supported
    }

    @Override
    public boolean isFavorite() {
        // Not supported
        return false;
    }
}
