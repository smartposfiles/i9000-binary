package rs.fncore.data;


import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;

import rs.utils.Utils;

/**
 * Данные о документах к отправке в ОФД
 *
 * @author nick
 */
public class OFDStatistic implements IReableFromParcel {
    private boolean mInProgress;
    private int mExchangeStatus;
    private int mUnsentCount;
    private long mFirstUnsentNumber;
    private long mFirstUnsentDate;

    public OFDStatistic() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Внутренний метод
     *
     * @param bb
     */
    public void update(ByteBuffer bb) {
        mExchangeStatus = bb.get();
        mInProgress = (bb.get() & 0xFF) != 0;
        mUnsentCount = Utils.readUint16LE(bb);
        mFirstUnsentNumber = Utils.readUint32LE(bb);
        mFirstUnsentDate = Utils.readDate5(bb);
    }

    /**
     * Признак установленного транспортного режима
     *
     * @return
     */
    public boolean isInProgress() {
        return mInProgress;
    }

    /**
     * Имеются ли неотправленные документы
     *
     * @return
     */
    public boolean haveUnsentDocuments() {
        return mUnsentCount > 0;
    }

    /**
     * Количество неотправленных документов
     *
     * @return
     */
    public int getUnsentDocumentCount() {
        return mUnsentCount;
    }

    /**
     * Номер первого неотправленного документа
     *
     * @return
     */
    public long getFirstUnsentNumber() {
        return mFirstUnsentNumber;
    }

    /**
     * Дата первого неотправленного документа
     *
     * @return
     */
    public long getFirstUnsentDate() {
        return mFirstUnsentDate;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(mExchangeStatus);
        p.writeInt(mInProgress ? 1 : 0);
        p.writeInt(mUnsentCount);
        p.writeLong(mFirstUnsentNumber);
        p.writeLong(mFirstUnsentDate);
    }

    public void readFromParcel(Parcel p) {
        mExchangeStatus = p.readInt();
        mInProgress = p.readInt() != 0;
        mUnsentCount = p.readInt();
        mFirstUnsentNumber = p.readLong();
        mFirstUnsentDate = p.readLong();
    }

    public static final Parcelable.Creator<OFDStatistic> CREATOR = new Parcelable.Creator<OFDStatistic>() {

        @Override
        public OFDStatistic createFromParcel(Parcel p) {
            OFDStatistic result = new OFDStatistic();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public OFDStatistic[] newArray(int size) {
            return new OFDStatistic[size];
        }

    };

}
