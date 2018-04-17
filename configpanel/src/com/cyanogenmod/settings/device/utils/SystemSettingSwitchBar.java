package com.cyanogenmod.settings.device.utils;

import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Switch;

import com.android.settingslib.lineageos.preference.SettingsHelper;

public class SystemSettingSwitchBar implements SwitchBar.OnSwitchChangeListener,
        SettingsHelper.OnSettingsChangeListener {

    private Context mContext;
    private SwitchBar mSwitchBar;
    private boolean mListeningToOnSwitchChange = false;

    private boolean mStateMachineEvent;

    private final String mSettingKey;
    private final int mDefaultState;

    private final SwitchBarChangeCallback mCallback;
    public interface SwitchBarChangeCallback {
        public void onEnablerChanged(boolean isEnabled);
    }

    public SystemSettingSwitchBar(Context context, SwitchBar switchBar, String key,
                                             boolean defaultState, SwitchBarChangeCallback callback) {
        mContext = context;
        mSwitchBar = switchBar;
        mSettingKey = key;
        mDefaultState = defaultState ? 1 : 0;
        mCallback = callback;
        setupSwitchBar();
    }

    public void setupSwitchBar() {
        setSwitchState();
        if (!mListeningToOnSwitchChange) {
            mSwitchBar.addOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = true;
        }
        mSwitchBar.show();
    }

    public void teardownSwitchBar() {
        if (mListeningToOnSwitchChange) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            mListeningToOnSwitchChange = false;
        }
        mSwitchBar.hide();
    }

    public void resume(Context context) {
        mContext = context;
        if (!mListeningToOnSwitchChange) {
            mSwitchBar.addOnSwitchChangeListener(this);
            SettingsHelper.get(mContext).startWatching(
                    this, Settings.System.getUriFor(mSettingKey));

            mListeningToOnSwitchChange = true;
        }
    }

    public void pause() {
        if (mListeningToOnSwitchChange) {
            mSwitchBar.removeOnSwitchChangeListener(this);
            SettingsHelper.get(mContext).stopWatching(this);

            mListeningToOnSwitchChange = false;
        }
    }

    private void setSwitchBarChecked(boolean checked) {
        mStateMachineEvent = true;
        mSwitchBar.setChecked(checked);
        mStateMachineEvent = false;
        if (mCallback != null) {
            mCallback.onEnablerChanged(checked);
        }
    }

    private void setSwitchState() {
        boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                mSettingKey, mDefaultState) == 1;
        mStateMachineEvent = true;
        setSwitchBarChecked(enabled);
        mStateMachineEvent = false;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }

        // Handle a switch change
        Settings.System.putInt(mContext.getContentResolver(),
                mSettingKey, isChecked ? 1 : 0);

        if (mCallback != null) {
            mCallback.onEnablerChanged(isChecked);
        }
    }

    @Override
    public void onSettingsChanged(Uri uri) {
        setSwitchState();
    }
}