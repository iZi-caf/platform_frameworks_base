/*
Copyright (c) 2016, The Linux Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
     From this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.android.systemui;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.statusbar.policy.BatteryController;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;

public class BatteryLevelTextView extends TextView implements
        BatteryController.BatteryStateChangeCallback{

    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";

    private BatteryController mBatteryController;
    private boolean mShow;
    private boolean mBatteryCharging = false;


    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            loadShowBatteryTextSetting();
            setVisibility((mShow || mBatteryCharging) ? View.VISIBLE : View.GONE);
        }
    };

    public BatteryLevelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadShowBatteryTextSetting();
        setVisibility(mShow || mBatteryCharging ? View.VISIBLE : View.GONE);
    }

    private void loadShowBatteryTextSetting() {
        mShow = 0 != Settings.System.getInt(getContext().getContentResolver(),
            STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
    }

    public void setBatteryCharging(boolean isCharging){
        mBatteryCharging = isCharging;
        loadShowBatteryTextSetting();
        setVisibility(mShow || mBatteryCharging ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        setText(getResources().getString(R.string.battery_level_template, level));
    }

    public void setBatteryController(BatteryController batteryController) {
        if(batteryController != null){
            mBatteryController = batteryController;
            mBatteryController.addStateChangedCallback(this);
        }
    }

    @Override
    public void onPowerSaveChanged() {
        // Not used
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor(
                STATUS_BAR_SHOW_BATTERY_PERCENT), false, mObserver);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBatteryController != null) {
            mBatteryController.removeStateChangedCallback(this);
        }
    }
}
