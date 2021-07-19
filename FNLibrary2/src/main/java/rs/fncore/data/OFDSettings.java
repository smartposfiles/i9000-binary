package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import rs.fncore.Const;

/**
 * Настройки сервера ОФД
 *
 * @author nick
 */
public class OFDSettings implements Parcelable {

    private String _ofdServerAddress = Const.EMPTY_STRING;
    private int _ofdServerPort = 7777;
    private int _ofdServerTimeout = 60;
    private volatile boolean _sendImmediately;

    public OFDSettings() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(_ofdServerAddress);
        p.writeInt(_ofdServerPort);
        p.writeInt(_ofdServerTimeout);
        p.writeInt(_sendImmediately ? 1 : 0);
    }

    public void readFromParcel(Parcel p) {
        _ofdServerAddress = p.readString();
        _ofdServerPort = p.readInt();
        _ofdServerTimeout = p.readInt();
        _sendImmediately = p.readInt() != 0;
    }

    /**
     * Получить адрес сервера
     *
     * @return
     */
    public String getServerAddress() {
        return _ofdServerAddress;
    }

    /**
     * Установить адрес сервера
     *
     * @param val
     */
    public void setServerAddress(String val) {
        if (val == null) val = Const.EMPTY_STRING;
        _ofdServerAddress = val;
    }

    /**
     * Получить порт сервера
     *
     * @return
     */
    public int getServerPort() {
        return _ofdServerPort;
    }

    /**
     * Установить порт сервера
     *
     * @param port
     */
    public void setServerPort(int port) {
        if (port > 65535) port = 65535;
        _ofdServerPort = port;
    }

    /**
     * Получить время ожидания ответа от сервера
     *
     * @return
     */
    public int getServerTimeout() {
        return _ofdServerTimeout;
    }

    /**
     * Установить время ожидания ответа от сервера
     *
     * @param value
     */
    public void setServerTimeout(int value) {
        if (value <= 0) value = 60;
        _ofdServerTimeout = value;
    }

    /**
     * Режим "отправка немедленно"
     *
     * @return
     */
    public boolean getImmediatelyMode() {
        return _sendImmediately;
    }

    /**
     * Установить режим "отправка немедленно"
     *
     * @param val
     */
    public void setImmediatelyMode(boolean val) {
        _sendImmediately = val;
    }

    public static final Parcelable.Creator<OFDSettings> CREATOR = new Parcelable.Creator<OFDSettings>() {

        @Override
        public OFDSettings createFromParcel(Parcel p) {
            OFDSettings result = new OFDSettings();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public OFDSettings[] newArray(int size) {
            return new OFDSettings[size];
        }

    };
}
