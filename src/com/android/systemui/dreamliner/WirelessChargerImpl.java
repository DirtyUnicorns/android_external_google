package com.google.android.systemui.dreamliner;

import android.os.Looper;
import android.util.Log;
import android.os.IHwBinder.DeathRecipient;
import android.os.Handler;
import com.android.internal.annotations.VisibleForTesting.Visibility;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import vendor.google.wireless_charger.V1_0.DockInfo;
import vendor.google.wireless_charger.V1_0.IWirelessCharger;
import vendor.google.wireless_charger.V1_0.IWirelessCharger.challengeCallback;
import vendor.google.wireless_charger.V1_0.IWirelessCharger.getInformationCallback;
import vendor.google.wireless_charger.V1_0.IWirelessCharger.isDockPresentCallback;
import vendor.google.wireless_charger.V1_0.IWirelessCharger.keyExchangeCallback;
import vendor.google.wireless_charger.V1_0.KeyExchangeResponse;

public class WirelessChargerImpl extends WirelessCharger implements DeathRecipient,
        isDockPresentCallback {
    private static final long MAX_POLLING_TIMEOUT_NS = TimeUnit.SECONDS.toNanos(5L);
    private isDockPresentCallback mCallback;
    private final Handler mHandler;
    private long mPollingStartedTimeNs;
    private final Runnable mRunnable;
    private IWirelessCharger mWirelessCharger;

    public WirelessChargerImpl() {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mRunnable = new Runnable() {
            @Override
            public void run() {
                WirelessChargerImpl.this.isDockPresentInternal(WirelessChargerImpl.this);
            }
        };
    }

    private ArrayList<Byte> convertPrimitiveArrayToArrayList(final byte[] array) {
        if (array != null && array.length > 0) {
            final ArrayList<Byte> list = new ArrayList<Byte>();
            for (int length = array.length, i = 0; i < length; ++i) {
                list.add(array[i]);
            }
            return list;
        }
        return null;
    }

    private void initHALInterface() {
        if (this.mWirelessCharger == null) {
            try {
                (this.mWirelessCharger = IWirelessCharger.getService()).linkToDeath((DeathRecipient)this, 0L);
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("no wireless charger hal found: ");
                String message;
                if (ex == null) {
                    message = "null";
                }
                else {
                    message = ex.getMessage();
                }
                sb.append(message);
                Log.i("Dreamliner-WLC_HAL", sb.toString());
                this.mWirelessCharger = null;
            }
        }
    }

    private void isDockPresentInternal(final isDockPresentCallback isDockPresentCallback) {
        this.initHALInterface();
        if (this.mWirelessCharger != null) {
            try {
                this.mWirelessCharger.isDockPresent(isDockPresentCallback);
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("isDockPresent fail: ");
                sb.append(ex.getMessage());
                Log.i("Dreamliner-WLC_HAL", sb.toString());
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @Override
    public void asyncIsDockPresent(final IsDockPresentCallback isDockPresentCallback) {
        this.initHALInterface();
        if (this.mWirelessCharger != null) {
            this.mPollingStartedTimeNs = System.nanoTime();
            this.mCallback = new IsDockPresentCallbackWrapper(isDockPresentCallback);
            this.mHandler.removeCallbacks(this.mRunnable);
            this.mHandler.postDelayed(this.mRunnable, 100L);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @Override
    public void challenge(final byte b, final byte[] array, final ChallengeCallback challengeCallback) {
        this.initHALInterface();
        if (this.mWirelessCharger != null) {
            try {
                this.mWirelessCharger.challenge(b, this.convertPrimitiveArrayToArrayList(array), (IWirelessCharger.challengeCallback)new ChallengeCallbackWrapper(challengeCallback));
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("challenge fail: ");
                sb.append(ex.getMessage());
                Log.i("Dreamliner-WLC_HAL", sb.toString());
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @Override
    public void getInformation(final GetInformationCallback getInformationCallback) {
        this.initHALInterface();
        if (this.mWirelessCharger != null) {
            try {
                this.mWirelessCharger.getInformation((IWirelessCharger.getInformationCallback)new GetInformationCallbackWrapper(getInformationCallback));
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("getInformation fail: ");
                sb.append(ex.getMessage());
                Log.i("Dreamliner-WLC_HAL", sb.toString());
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @Override
    public void keyExchange(final byte[] array, final KeyExchangeCallback keyExchangeCallback) {
        this.initHALInterface();
        if (this.mWirelessCharger != null) {
            try {
                this.mWirelessCharger.keyExchange(this.convertPrimitiveArrayToArrayList(array), (IWirelessCharger.keyExchangeCallback)new KeyExchangeCallbackWrapper(keyExchangeCallback));
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("keyExchange fail: ");
                sb.append(ex.getMessage());
                Log.i("Dreamliner-WLC_HAL", sb.toString());
            }
        }
    }

    public void onValues(final boolean b, final byte b2, final byte b3, final boolean b4, final int n) {
        if (System.nanoTime() < this.mPollingStartedTimeNs + WirelessChargerImpl.MAX_POLLING_TIMEOUT_NS && n == 0) {
            this.mHandler.postDelayed(this.mRunnable, 100L);
            return;
        }
        if (this.mCallback == null) {
            return;
        }
        this.mCallback.onValues(b, b2, b3, b4, n);
        this.mCallback = null;
    }

    public void serviceDied(final long n) {
        Log.i("Dreamliner-WLC_HAL", "serviceDied");
        this.mWirelessCharger = null;
    }

    final class ChallengeCallbackWrapper implements challengeCallback
    {
        private ChallengeCallback mCallback;

        public ChallengeCallbackWrapper(final ChallengeCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void onValues(final byte b, final ArrayList<Byte> list) {
            this.mCallback.onCallback(new Byte(b), list);
        }
    }

    final class GetInformationCallbackWrapper implements getInformationCallback
    {
        private GetInformationCallback mCallback;

        public GetInformationCallbackWrapper(final GetInformationCallback mCallback) {
            this.mCallback = mCallback;
        }

        private com.google.android.systemui.dreamliner.DockInfo convertDockInfo(final DockInfo dockInfo) {
            return new com.google.android.systemui.dreamliner.DockInfo(dockInfo.manufacturer, dockInfo.model, dockInfo.serial, new Byte(dockInfo.type));
        }

        @Override
        public void onValues(final byte b, final DockInfo dockInfo) {
            this.mCallback.onCallback(new Byte(b), this.convertDockInfo(dockInfo));
        }
    }

    final class IsDockPresentCallbackWrapper implements isDockPresentCallback
    {
        private IsDockPresentCallback mCallback;

        public IsDockPresentCallbackWrapper(final IsDockPresentCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void onValues(final boolean b, final byte b2, final byte b3, final boolean b4, final int n) {
            this.mCallback.onCallback(b, b2, b3, b4, n);
        }
    }

    final class KeyExchangeCallbackWrapper implements keyExchangeCallback
    {
        private KeyExchangeCallback mCallback;

        public KeyExchangeCallbackWrapper(final KeyExchangeCallback mCallback) {
            this.mCallback = mCallback;
        }

        @Override
        public void onValues(final byte b, final KeyExchangeResponse keyExchangeResponse) {
            if (keyExchangeResponse != null) {
                this.mCallback.onCallback(new Byte(b), keyExchangeResponse.dockId, keyExchangeResponse.dockPublicKey);
            }
            else {
                this.mCallback.onCallback(new Byte(b), (byte)(-1), null);
            }
        }
    }
}
