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

package be.hyperrail.android.irail.factories;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.perf.metrics.AddTrace;

import be.hyperrail.android.irail.contracts.IrailDataProvider;
import be.hyperrail.android.irail.contracts.IrailStationProvider;
import be.hyperrail.android.irail.db.StationsDb;
import be.hyperrail.android.irail.implementation.IrailApi;
import be.hyperrail.android.irail.implementation.Lc2IrailApi;
import be.hyperrail.android.irail.implementation.LinkedConnectionsApi;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

/**
 * Factory to provide singleton data providers (for stations and API calls) and a singleton parser.
 * This factory should be setup at application start. Once setup, context isn't required for calls which return an instance.
 */
public class IrailFactory {

    private static IrailStationProvider stationProviderInstance;
    private static IrailDataProvider dataProviderInstance;

    @AddTrace(name = "IrailFactory.setup")
    public static void setup(Context applicationContext) {
        stationProviderInstance = new StationsDb(applicationContext);

        String api = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("api", "irail");
        switch (api) {
            case "irail":
                dataProviderInstance = new IrailApi(applicationContext);
                break;
            case "lc2irail":
                dataProviderInstance = new Lc2IrailApi(applicationContext);
                break;
            case "lc":
                dataProviderInstance = new LinkedConnectionsApi(applicationContext);
                break;
        }
        FirebaseCrash.logcat(INFO.intValue(), "IrailFactory", "Set-up completed with API " + api);
        Toast.makeText(applicationContext, "Loaded API: " + api, Toast.LENGTH_LONG).show();
    }

    public static IrailStationProvider getStationsProviderInstance() {
        if (stationProviderInstance == null) {
            FirebaseCrash.logcat(SEVERE.intValue(), "Irail16Factory", "Failed to provide station provider! Call setup() before calling any factory method!");
            FirebaseCrash.report(new Exception("IrailStationProvider was requested before the factory was initialized"));
            throw new IllegalStateException();
        }
        return stationProviderInstance;
    }

    public static IrailDataProvider getDataProviderInstance() {
        if (dataProviderInstance == null) {
            FirebaseCrash.logcat(SEVERE.intValue(), "Irail16Factory", "Failed to provide data provider! Call setup() before calling any factory method!");
            FirebaseCrash.report(new Exception("IrailDataProvider was requested before the factory was initialized"));
            throw new IllegalStateException();
        }
        return dataProviderInstance;
    }
}
