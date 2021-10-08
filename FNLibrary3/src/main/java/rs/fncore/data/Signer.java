package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Оборудование выполнившее подпись
 *
 * @author nick
 */
public class Signer implements Parcelable {
    protected String mKkmNumber;
    protected String mFnNumber;
    protected String mDeviceSerial;
    protected OU mOwner = new OU();
    protected Location mLocation = new Location();

    public String DeviceSerial() {
        return mDeviceSerial;
    }

    /**
     * Заводской номер ККТ
     *
     * @return
     */
    public String KKMNumber() {
        return mKkmNumber;
    }

    /**
     * Серийный номер фискального накопителя
     *
     * @return
     */
    public String FNNumber() {
        return mFnNumber;
    }

    /**
     * Владелец ККТ
     *
     * @return
     */
    public OU owner() {
        return mOwner;
    }

    /**
     * Адрес и место расчетов
     *
     * @return
     */
    public Location getLocation() {
        return mLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeString(mDeviceSerial);
        p.writeString(mFnNumber);
        p.writeString(mKkmNumber);
        mOwner.writeToParcel(p, flags);
    }

    public void readFromParcel(Parcel p) {
        mDeviceSerial = p.readString();
        mFnNumber = p.readString();
        mKkmNumber = p.readString();
        mOwner.readFromParcel(p);
    }
}
