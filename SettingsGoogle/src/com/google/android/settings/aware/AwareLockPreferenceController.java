package com.google.android.settings.aware;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.SecurityFeatureProvider;
import com.android.settings.security.trustagent.TrustAgentManager;

public class AwareLockPreferenceController extends AwareTogglePreferenceController {

    private final LockPatternUtils mLockPatternUtils;
    private final TrustAgentManager mTrustAgentManager;

    public AwareLockPreferenceController(Context context, String str) {
        super(context, str);
        SecurityFeatureProvider securityFeatureProvider =
                FeatureFactory.getFactory(context).getSecurityFeatureProvider();
        mLockPatternUtils = securityFeatureProvider.getLockPatternUtils(context);
        mTrustAgentManager = securityFeatureProvider.getTrustAgentManager();
    }

    public int getAvailabilityStatus() {
        return (!mLockPatternUtils.isSecure(UserHandle.myUserId())
                || !mHelper.isGestureConfigurable()) ? 5 : 0;
    }

    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(), "aware_lock_enabled", 1) == 1;
    }

    public boolean setChecked(boolean z) {
        mHelper.writeFeatureEnabled("aware_lock_enabled", z);
        Settings.Secure.putInt(mContext.getContentResolver(), "aware_lock_enabled", z ? 1 : 0);
        return true;
    }

    public CharSequence getSummary() {
        CharSequence activeTrustAgentLabel =
                mTrustAgentManager.getActiveTrustAgentLabel(mContext, mLockPatternUtils);
        if (TextUtils.isEmpty(activeTrustAgentLabel)) {
            return mContext.getString(R.string.summary_placeholder);
        }
        return mContext.getString(R.string.lockpattern_settings_power_button_instantly_locks_summary,
                new Object[]{activeTrustAgentLabel});
    }
}
