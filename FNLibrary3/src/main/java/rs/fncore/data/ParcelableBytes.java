package rs.fncore.data;

import android.os.Parcel;

/**
 * Передаваемый через границу процесса массив байт
 *
 * @author nick
 */
public class ParcelableBytes implements IReableFromParcel {

    protected byte[] mRaw;

    public ParcelableBytes() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {
        if (mRaw != null) {
            p.writeInt(mRaw.length);
            p.writeByteArray(mRaw);
        } else
            p.writeInt(0);

    }

    /**
     * Получить массив байт
     *
     * @return
     */
    public byte[] getRawBytes() {
        return mRaw;
    }

    /**
     * Установить массив байт
     *
     * @param raw
     */
    public void setRawBytes(byte[] raw) {
        mRaw = raw;
    }

    @Override
    public void readFromParcel(Parcel p) {
        int size = p.readInt();
        if (size > 0) {
            mRaw = new byte[size];
            p.readByteArray(mRaw);
        }
    }

}
