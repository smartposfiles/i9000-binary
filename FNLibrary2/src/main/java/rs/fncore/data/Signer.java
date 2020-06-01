package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Оборудование выполнившее подпись
 *
 * @author nick
 */
public class Signer implements Parcelable {
    protected String _kkm_number;
    protected String _fn_number;
    protected String _device_serial;
    protected OU _owner = new OU();
    protected Location _location = new Location();

    public String DeviceSerial() {
        return _device_serial;
    }

    /**
     * Заводской номер ККМ
     *
     * @return
     */
    public String KKMNumber() {
        return _kkm_number;
    }

    /**
     * Серийный номер фискального накопителя
     *
     * @return
     */
    public String FNNumber() {
        return _fn_number;
    }

    /**
     * Владелец ККМ
     *
     * @return
     */
    public OU owner() {
        return _owner;
    }

    /**
     * Адрес и место расчетов
     *
     * @return
     */
    public Location getLocation() {
        return _location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(_device_serial);
        p.writeString(_fn_number);
        p.writeString(_kkm_number);
        _owner.writeToParcel(p, flags);
    }

    public void readFromParcel(Parcel p) {
        _device_serial = p.readString();
        _fn_number = p.readString();
        _kkm_number = p.readString();
        _owner.readFromParcel(p);
    }
}
