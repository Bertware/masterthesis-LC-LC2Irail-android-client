/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package be.hyperrail.android.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.joda.time.DateTime;

import java.io.Serializable;

import be.hyperrail.android.R;
import be.hyperrail.android.persistence.PersistentQueryProvider;
import be.hyperrail.android.util.DateTimePicker;
import be.hyperrail.android.util.OnDateTimeSetListener;

/**
 * An abstract class for activities which contain a recyclerview
 */
public abstract class ResultActivity extends AppCompatActivity implements OnDateTimeSetListener, Serializable {

    /**
     * History & favorites provider
     */
    protected PersistentQueryProvider mPersistentQueryProvider;

    /**
     * Favorite button in menu
     */
    protected MenuItem vFavoriteMenuItem;

    /**
     * The layout root
     */
    protected View vLayoutRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the layout & set the root
        setContentView(getLayout());
        vLayoutRoot = findViewById(R.id.activity);

        // Get and set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the Up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPersistentQueryProvider = new PersistentQueryProvider(this.getApplicationContext());
    }

    /**
     * Get the layout for this acitivity
     * @return The ID of the layout to use for this activity. By default a simple FrameLayout with toolbar is used.
     */
    @LayoutRes
    protected int getLayout(){
        return R.layout.activity_result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_time:
                // Show a datetime picker when needed
                DateTimePicker picker = new DateTimePicker(this);
                picker.setListener(this);
                picker.pick();
                return true;
            case R.id.action_favorite:
                // Mark or unmark as favorite
                markFavorite(!isFavorite());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the menu layout
     */
    protected
    @MenuRes
    int getMenuLayout() {
        return R.menu.actionbar_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(getMenuLayout(), menu);

        vFavoriteMenuItem = menu.findItem(R.id.action_favorite);
        if (vFavoriteMenuItem != null) {
            setFavoriteDisplayState(isFavorite());
        }

        return true;
    }


    @Override
    public abstract void onDateTimePicked(DateTime date);

    /**
     * Set or unset the favorite display state
     *
     * @param favorite true to mark this item as favorite, false to unmark it
     */
    public void setFavoriteDisplayState(boolean favorite) {
        if (favorite) {
            vFavoriteMenuItem.setIcon(R.drawable.ic_star);
            vFavoriteMenuItem.setTitle(R.string.action_unmark_as_favorite);
            tintDrawable(vFavoriteMenuItem.getIcon(), R.color.colorTextLight);
        } else {
            vFavoriteMenuItem.setIcon(R.drawable.ic_star_border);
            vFavoriteMenuItem.setTitle(R.string.action_mark_as_favorite);
            tintDrawable(vFavoriteMenuItem.getIcon(), R.color.colorTextLight);
        }
    }

    /**
     * Mark or unmark an item as favorite
     *
     * @param favorite Whether or not to mark as favorite
     */
    protected abstract void markFavorite(boolean favorite);

    protected abstract boolean isFavorite();

    /**
     * Tint a drawable
     *
     * @param drawable The drawable to tint
     * @param color    The color to tint the drawable with
     */
    private void tintDrawable(Drawable drawable, @ColorRes int color) {
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), color), PorterDuff.Mode.SRC_ATOP);
        }
    }

    /**
     * Set the subtitle of the activty
     *
     * @param title The text to set
     */
    public void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Set the subtitle of the activty
     *
     * @param title The text to set
     */
    @SuppressWarnings("unused")
    public void setTitle(@StringRes int title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Set the subtitle of the activty
     *
     * @param subtitle The text to set
     */
    public void setSubTitle(String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    /**
     * Set the subtitle of the activty
     *
     * @param subtitle The text to set
     */
    @SuppressWarnings("unused")
    public void setSubTitle(@StringRes int subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

}
