package vendor.google.wireless_charger.V1_0;

import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HidlSupport;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public interface IWirelessCharger extends IBase {

    @FunctionalInterface
    public interface challengeCallback {
        void onValues(byte b, ArrayList<Byte> arrayList);
    }

    @FunctionalInterface
    public interface getInformationCallback {
        void onValues(byte b, DockInfo dockInfo);
    }

    @FunctionalInterface
    public interface isDockPresentCallback {
        void onValues(boolean z, byte b, byte b2, boolean z2, int i);
    }

    @FunctionalInterface
    public interface keyExchangeCallback {
        void onValues(byte b, KeyExchangeResponse keyExchangeResponse);
    }

    public static final class Proxy implements IWirelessCharger {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(interfaceDescriptor());
                stringBuilder.append("@Proxy");
                return stringBuilder.toString();
            } catch (RemoteException e) {
                return "[class or subclass of vendor.google.wireless_charger@1.0::IWirelessCharger]@Proxy";
            }
        }

        public final boolean equals(Object other) {
            return HidlSupport.interfacesEqual(this, other);
        }

        public final int hashCode() {
            return asBinder().hashCode();
        }

        public void isDockPresent(isDockPresentCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("vendor.google.wireless_charger@1.0::IWirelessCharger");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readBool(), _hidl_reply.readInt8(), _hidl_reply.readInt8(), _hidl_reply.readBool(), _hidl_reply.readInt32());
            } finally {
                _hidl_reply.release();
            }
        }

        public void getInformation(getInformationCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("vendor.google.wireless_charger@1.0::IWirelessCharger");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte _hidl_out_result = _hidl_reply.readInt8();
                DockInfo _hidl_out_info = new DockInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_result, _hidl_out_info);
            } finally {
                _hidl_reply.release();
            }
        }

        public void keyExchange(ArrayList<Byte> myPublicKey, keyExchangeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("vendor.google.wireless_charger@1.0::IWirelessCharger");
            _hidl_request.writeInt8Vector(myPublicKey);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                byte _hidl_out_result = _hidl_reply.readInt8();
                KeyExchangeResponse _hidl_out_response = new KeyExchangeResponse();
                _hidl_out_response.readFromParcel(_hidl_reply);
                _hidl_cb.onValues(_hidl_out_result, _hidl_out_response);
            } finally {
                _hidl_reply.release();
            }
        }

        public void challenge(byte dockId, ArrayList<Byte> challenge, challengeCallback _hidl_cb) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("vendor.google.wireless_charger@1.0::IWirelessCharger");
            _hidl_request.writeInt8(dockId);
            _hidl_request.writeInt8Vector(challenge);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                _hidl_cb.onValues(_hidl_reply.readInt8(), _hidl_reply.readInt8Vector());
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                int _hidl_index_0 = 0;
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                while (true) {
                    int _hidl_index_02 = _hidl_index_0;
                    if (_hidl_index_02 >= _hidl_vec_size) {
                        break;
                    }
                    byte[] _hidl_vec_element = new byte[32];
                    childBlob.copyToInt8Array((long) (_hidl_index_02 * 32), _hidl_vec_element, 32);
                    _hidl_out_hashchain.add(_hidl_vec_element);
                    _hidl_index_0 = _hidl_index_02 + 1;
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken("android.hidl.base@1.0::IBase");
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    void challenge(byte b, ArrayList<Byte> arrayList, challengeCallback challengecallback) throws RemoteException;

    void getInformation(getInformationCallback getinformationcallback) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    void isDockPresent(isDockPresentCallback isdockpresentcallback) throws RemoteException;

    void keyExchange(ArrayList<Byte> arrayList, keyExchangeCallback keyexchangecallback) throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    static IWirelessCharger asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface("vendor.google.wireless_charger@1.0::IWirelessCharger");
        if (iface != null && (iface instanceof IWirelessCharger)) {
            return (IWirelessCharger) iface;
        }
        IWirelessCharger proxy = new Proxy(binder);
        try {
            Iterator it = proxy.interfaceChain().iterator();
            while (it.hasNext()) {
                if (((String) it.next()).equals("vendor.google.wireless_charger@1.0::IWirelessCharger")) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IWirelessCharger getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService("vendor.google.wireless_charger@1.0::IWirelessCharger", serviceName));
    }

    static IWirelessCharger getService() throws RemoteException {
        return getService("default");
    }
}