/*
 * Copyright (C) 2012 The CyanogenMod Project
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

package com.cyanogenmod.settings.device.profiles;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.cyanogenmod.settings.device.R;
import com.cyanogenmod.settings.device.utils.SwitchBar;

public class ProflesSettingsActivity extends SettingsDrawerActivity {

    private static final String TAG = "ProflesSettingsActivity";

    public static final String EXTRA_SHOW_FRAGMENT = ":settings:show_fragment";
    public static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title";
    public static final String EXTRA_SHOW_FRAGMENT_TITLE_RESID = ":settings:show_fragment_title_resid";
    public static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";

    private NFCProfileTagCallback mNfcProfileCallback;

    private CharSequence mInitialTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_settings_activity);

        String fragmentClass = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);

        if(fragmentClass==null || fragmentClass.isEmpty()){
            getFragmentManager().beginTransaction().replace(R.id.profiles_content, new ProfilesSettingsFragment()).commit();
        } else {
            Bundle initialArgs = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
            if (initialArgs == null) {
                initialArgs = new Bundle();
            }
            setTitleFromIntent(getIntent());
            switchToFragment(fragmentClass, initialArgs, -1, mInitialTitle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void finishPreferencePanel(Fragment caller, int resultCode, Intent resultData) {
        setResult(resultCode, resultData);
        finish();
    }

    public void startPreferencePanel(String fragmentClass, Bundle args, int titleRes,
                                     CharSequence titleText, Fragment resultTo, int resultRequestCode) {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.cyanogenmod.settings.device", "com.cyanogenmod.settings.device.profiles.ProflesSettingsActivity"));
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentClass);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, titleRes);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, titleText);

        Log.v(TAG, "Launching fragment: " + fragmentClass+" / "+intent);

        if (resultTo == null) {
            startActivity(intent);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    public void showButtonBar(boolean show) {
        findViewById(R.id.button_bar).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public Button getBackButton() {
        return (Button) findViewById(R.id.back_button);
    }

    public Button getNextButton() {
        return (Button) findViewById(R.id.next_button);
    }

    public void setNfcProfileCallback(NFCProfileTagCallback callback) {
        mNfcProfileCallback = callback;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (mNfcProfileCallback != null) {
                mNfcProfileCallback.onTagRead(detectedTag);
            }
            return;
        }
        super.onNewIntent(intent);
    }

    public SwitchBar getSwitchBar() {
        return (SwitchBar) findViewById(R.id.switch_bar);
    }

    private void setTitleFromIntent(Intent intent) {
        final int initialTitleResId = intent.getIntExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, -1);
        if (initialTitleResId > 0) {
            mInitialTitle = getResources().getString(initialTitleResId);
        } else {
            final String initialTitle = intent.getStringExtra(EXTRA_SHOW_FRAGMENT_TITLE);
            mInitialTitle = (initialTitle != null) ? initialTitle : getTitle();
        }
        setTitle(mInitialTitle);
    }

    public boolean switchToFragment(String fragmentClass, Bundle args, int titleRes,
                                    CharSequence titleText) {
        Fragment fragment = Fragment.instantiate(this, fragmentClass);
        if (fragment == null) {
            Log.v(TAG, "Invalid fragment! " + fragmentClass);
            return false;
        }
        return switchToFragment(fragment, args, titleRes, titleText);
    }

    private  boolean switchToFragment(Fragment fragment, Bundle args, int titleRes,
                                      CharSequence titleText) {
        Log.v(TAG, "Launching fragment: " + fragment.getClass().getName());

        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.profiles_content, fragment);
        if (titleRes > 0) {
            transaction.setBreadCrumbTitle(titleRes);
        } else if (titleText != null) {
            transaction.setBreadCrumbTitle(titleText);
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
        return true;
    }
}
