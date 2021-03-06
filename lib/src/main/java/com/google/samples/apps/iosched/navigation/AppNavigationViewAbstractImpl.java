/*
 * Copyright (c) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.samples.apps.iosched.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import com.google.samples.apps.iosched.archframework.PresenterImpl;
import com.google.samples.apps.iosched.archframework.UpdatableView;
import com.google.samples.apps.iosched.lib.R;
import com.google.samples.apps.iosched.navigation.NavigationModel.NavigationItemEnum;
import com.google.samples.apps.iosched.navigation.NavigationModel.NavigationQueryEnum;
import com.google.samples.apps.iosched.navigation.NavigationModel.NavigationUserActionEnum;

/**
 * This abstract class implements both {@link UpdatableView} and {@link AppNavigationView}, without
 * any specific UI implementation details. This uses the {@link com.google.samples.apps.iosched
 * .archframework} for getting its data and processing user actions. Some methods which are UI
 * specific are left abstract. Extend this class for full navigation functionality.
 */
public abstract class AppNavigationViewAbstractImpl implements
        UpdatableView<NavigationModel, NavigationQueryEnum, NavigationUserActionEnum>,
        AppNavigationView {

    private static final long BOTTOM_NAV_ANIM_GRACE = 115L;
    private UserActionListener<NavigationUserActionEnum> mUserActionListener;

    protected Activity mActivity;

    private final Handler mHandler = new Handler();

    protected NavigationItemEnum mSelfItem;

    @Override
    public void displayData(final NavigationModel model, final NavigationQueryEnum query) {
        switch (query) {
            case LOAD_ITEMS:
                displayNavigationItems(model.getItems());
                break;
        }
    }

    @Override
    public void displayErrorMessage(final NavigationQueryEnum query) {
        switch (query) {
            case LOAD_ITEMS:
                // No error message displayed
                break;
        }
    }

    @Override
    public void activityReady(Activity activity, NavigationItemEnum self) {
        mActivity = activity;
        mSelfItem = self;

        setUpView();

        NavigationModel model = new NavigationModel();
        PresenterImpl<NavigationModel, NavigationQueryEnum, NavigationUserActionEnum> presenter
                = new PresenterImpl<>(model, this, NavigationUserActionEnum.values(),
                NavigationQueryEnum.values());
        presenter.loadInitialQueries();
        addListener(presenter);
    }

    @Override
    public void updateNavigationItems() {
        mUserActionListener.onUserAction(NavigationUserActionEnum.RELOAD_ITEMS, null);
    }

    @Override
    public abstract void displayNavigationItems(final NavigationItemEnum[] items);

    @Override
    public abstract void setUpView();

    @Override
    public abstract void showNavigation();

    @Override
    public void itemSelected(final NavigationItemEnum item) {
        if (item.getClassToLaunch() != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mActivity.startActivity(new Intent(mActivity, item.getClassToLaunch()));
                    if (item.finishCurrentActivity()) {
                        mActivity.finish();
                        mActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }
            }, BOTTOM_NAV_ANIM_GRACE);
        }
    }

    @Override
    public void displayUserActionResult(final NavigationModel model,
            final NavigationUserActionEnum userAction, final boolean success) {
        switch (userAction) {
            case RELOAD_ITEMS:
                displayNavigationItems(model.getItems());
                break;
        }
    }

    @Override
    public Uri getDataUri(final NavigationQueryEnum query) {
        // This feature has no Uri
        return null;
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void addListener(final UserActionListener<NavigationUserActionEnum> listener) {
        mUserActionListener = listener;
    }
}
