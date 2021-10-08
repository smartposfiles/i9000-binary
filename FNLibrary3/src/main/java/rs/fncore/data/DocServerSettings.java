package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import rs.fncore.Const;

/**
 * Настройки сервера ОФД или ОИСМ или ОКП
 *
 * @author amv
 */
public class DocServerSettings implements Parcelable {

    private String mServerAddress = Const.EMPTY_STRING;
    private int mServerPort = 7777;
    private int mServerTimeoutS = 60;
    private volatile boolean mSendImmediately;

    public DocServerSettings() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(mServerAddress);
        p.writeInt(mServerPort);
        p.writeInt(mServerTimeoutS);
        p.writeInt(mSendImmediately ? 1 : 0);
    }

    public void readFromParcel(Parcel p) {
        mServerAddress = p.readString();
        mServerPort = p.readInt();
        mServerTimeoutS = p.readInt();
        mSendImmediately = p.readInt() != 0;
    }

    /**
     * Получить адрес сервера
     *
     * @return
     */
    public String getServerAddress() {
        return mServerAddress;
    }

    /**
     * Установить адрес сервера
     *
     * @param val
     */
    public void setServerAddress(String val) {
        if (val == null) val = Const.EMPTY_STRING;
        mServerAddress = val;
    }

    /**
     * Получить порт сервера
     *
     * @return
     */
    public int getServerPort() {
        return mServerPort;
    }

    /**
     * Установить порт сервера
     *
     * @param port
     */
    public void setServerPort(int port) {
        if (port > 65535) port = 65535;
        mServerPort = port;
    }

    /**
     * Получить время ожидания ответа от сервера
     *
     * @return
     */
    public int getServerTimeout() {
        return mServerTimeoutS;
    }

    /**
     * Установить время ожидания ответа от сервера
     *
     * @param value
     */
    public void setServerTimeout(int value) {
        if (value <= 0) value = 60;
        mServerTimeoutS = value;
    }

    /**
     * Режим "отправка немедленно"
     *
     * @return
     */
    public boolean getImmediatelyMode() {
        return mSendImmediately;
    }

    /**
     * Установить режим "отправка немедленно"
     *
     * @param val
     */
    public void setImmediatelyMode(boolean val) {
        mSendImmediately = val;
    }

    public static final Parcelable.Creator<DocServerSettings> CREATOR = new Parcelable.Creator<DocServerSettings>() {

        @Override
        public DocServerSettings createFromParcel(Parcel p) {
            DocServerSettings result = new DocServerSettings();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public DocServerSettings[] newArray(int size) {
            return new DocServerSettings[size];
        }

    };
}
