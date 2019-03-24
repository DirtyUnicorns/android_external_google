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
        mHandler = new Handler(Looper.getMainLooper());
        mRunnable = new Runnable() {
            @Override
            public void run() {
                isDockPresentInternal(WirelessChargerImpl.this);
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
        if (mWirelessCharger == null) {
            try {
                (mWirelessCharger = IWirelessCharger.getService()).linkToDeath((
                        DeathRecipient)this, 0L);
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
                mWirelessCharger = null;
            }
        }
    }

    private void isDockPresentInternal(final isDockPresentCallback isDockPresentCallback) {
        initHALInterface();
        if (mWirelessCharger != null) {
            try {
                mWirelessCharger.isDockPresent(isDockPresentCallback);
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
        initHALInterface();
        if (mWirelessCharger != null) {
            mPollingStartedTimeNs = System.nanoTime();
            mCallback = new IsDockPresentCallbackWrapper(isDockPresentCallback);
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 100L);
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    @Override
    public void challenge(final byte b, final byte[] array,
                          final ChallengeCallback challengeCallback) {
        initHALInterface();
        if (mWirelessCharger != null) {
            try {
                mWirelessCharger.challenge(b, convertPrimitiveArrayToArrayList(array),
                        (IWirelessCharger.challengeCallback) new ChallengeCallbackWrapper(
                                challengeCallback));
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
        initHALInterface();
        if (mWirelessCharger != null) {
            try {
                mWirelessCharger.getInformation((IWirelessCharger.getInformationCallback)
                        new GetInformationCallbackWrapper(getInformationCallback));
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
        initHALInterface();
        if (mWirelessCharger != null) {
            try {
                mWirelessCharger.keyExchange(convertPrimitiveArrayToArrayList(array),
                        (IWirelessCharger.keyExchangeCallback) new KeyExchangeCallbackWrapper(
                                keyExchangeCallback));
            }
            catch (Exception ex) {
                final StringBuilder sb = new StringBuilder();
                sb.append("keyExchange fail: ");
                sb.append(ex.getMessage());
                Log.i("Dreamliner-WLC_HAL", sb.toString());
            }
        }
    }

    public void onValues(final boolean b, final byte b2, final byte b3, final boolean b4,
                         final int n) {
        if (System.nanoTime() < mPollingStartedTimeNs +
                MAX_POLLING_TIMEOUT_NS && n == 0) {
            mHandler.postDelayed(mRunnable, 100L);
            return;
        }
        if (mCallback == null) {
            return;
        }
        mCallback.onValues(b, b2, b3, b4, n);
        mCallback = null;
    }

    public void serviceDied(final long n) {
        Log.i("Dreamliner-WLC_HAL", "serviceDied");
        mWirelessCharger = null;
    }

    final class ChallengeCallbackWrapper implements challengeCallback {
        private ChallengeCallback mCallback;

        public ChallengeCallbackWrapper(final ChallengeCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onValues(final byte b, final ArrayList<Byte> list) {
            mCallback.onCallback(new Byte(b), list);
        }
    }

    final class GetInformationCallbackWrapper implements getInformationCallback {
        private GetInformationCallback mCallback;

        public GetInformationCallbackWrapper(final GetInformationCallback callback) {
            mCallback = callback;
        }

        private com.google.android.systemui.dreamliner.DockInfo convertDockInfo(
                final DockInfo dockInfo) {
            return new com.google.android.systemui.dreamliner.DockInfo(dockInfo.manufacturer,
                    dockInfo.model, dockInfo.serial, new Byte(dockInfo.type));
        }

        @Override
        public void onValues(final byte b, final DockInfo dockInfo) {
            mCallback.onCallback(new Byte(b), convertDockInfo(dockInfo));
        }
    }

    final class IsDockPresentCallbackWrapper implements isDockPresentCallback {
        private IsDockPresentCallback mCallback;

        public IsDockPresentCallbackWrapper(final IsDockPresentCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onValues(final boolean b, final byte b2, final byte b3, final boolean b4, 
                final int n) {
            mCallback.onCallback(b, b2, b3, b4, n);
        }
    }

    final class KeyExchangeCallbackWrapper implements keyExchangeCallback {
        private KeyExchangeCallback mCallback;

        public KeyExchangeCallbackWrapper(final KeyExchangeCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onValues(final byte b, final KeyExchangeResponse keyExchangeResponse) {
            if (keyExchangeResponse != null) {
                mCallback.onCallback(new Byte(b), keyExchangeResponse.dockId,
                        keyExchangeResponse.dockPublicKey);
            }
            else {
                mCallback.onCallback(new Byte(b), (byte)(-1), null);
            }
        }
    }
}
