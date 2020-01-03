package com.google.android.systemui.dreamliner;

import java.util.ArrayList;

public abstract class WirelessCharger {

    public interface ChallengeCallback {
        void onCallback(int i, ArrayList<Byte> arrayList);
    }

    public interface GetInformationCallback {
        void onCallback(int i, DockInfo dockInfo);
    }

    public interface IsDockPresentCallback {
        void onCallback(boolean z, byte b, byte b2, boolean z2, int i);
    }

    public interface KeyExchangeCallback {
        void onCallback(int i, byte b, ArrayList<Byte> arrayList);
    }

    public abstract void asyncIsDockPresent(IsDockPresentCallback isDockPresentCallback);

    public abstract void challenge(byte b, byte[] bArr, ChallengeCallback challengeCallback);

    public abstract void getInformation(GetInformationCallback getInformationCallback);

    public abstract void keyExchange(byte[] bArr, KeyExchangeCallback keyExchangeCallback);
}