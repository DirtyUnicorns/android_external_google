/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.settings.overlay;

import android.support.annotation.Keep;
import com.android.settings.overlay.DockUpdaterFeatureProvider;
import com.google.android.settings.connecteddevice.dock.DockUpdaterFeatureProviderGoogleImpl;

public final class FeatureFactoryImpl extends com.android.settings.overlay.FeatureFactoryImpl {

    private DockUpdaterFeatureProvider mDockUpdaterFeatureProvider;

    @Override
    public DockUpdaterFeatureProvider getDockUpdaterFeatureProvider() {
        if (mDockUpdaterFeatureProvider == null) {
            mDockUpdaterFeatureProvider = new DockUpdaterFeatureProviderGoogleImpl();
        }
        return mDockUpdaterFeatureProvider;
    }
}
