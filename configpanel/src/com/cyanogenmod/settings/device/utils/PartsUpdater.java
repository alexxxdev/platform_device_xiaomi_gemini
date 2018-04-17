package com.cyanogenmod.settings.device.utils;

import android.content.Context;

import com.android.settingslib.lineageos.preference.RemotePreferenceUpdater;
import com.android.settingslib.lineageos.preference.SettingsHelper;

public class PartsUpdater extends RemotePreferenceUpdater{

    public interface Refreshable extends SettingsHelper.OnSettingsChangeListener {
        public interface SummaryProvider {
            public String getSummary(Context context, String key);
        }
    }
}
