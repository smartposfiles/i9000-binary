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
    private boolean _inProgress;
    private int _documentCount;
    private int _firstUnsentNumber;
    private long _firstUnsentDate;

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
        bb.get();
        _inProgress = (bb.get() & 0xFF) != 0;
        _documentCount = (bb.getShort() & 0xFFFF);
        _firstUnsentNumber = bb.getInt();
        _firstUnsentDate = Utils.readDate5(bb);
    }

    /**
     * Признак установленного транспортного режима
     *
     * @return
     */
    public boolean isInProgress() {
        return _inProgress;
    }

    /**
     * Имеются ли неотправленные документы
     *
     * @return
     */
    public boolean haveUnsentDocuments() {
        return _documentCount > 0;
    }

    /**
     * Количество неотправленных документов
     *
     * @return
     */
    public int getDocumentCount() {
        return _documentCount;
    }

    /**
     * Номер первого неотправленного документа
     *
     * @return
     */
    public int getFirstUnsentNumber() {
        return _firstUnsentNumber;
    }

    /**
     * Дата первого неотправленного документа
     *
     * @return
     */
    public long getFirstUnsentDate() {
        return _firstUnsentDate;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(_inProgress ? 1 : 0);
        p.writeInt(_documentCount);
        p.writeInt(_firstUnsentNumber);
        p.writeLong(_firstUnsentDate);
    }

    public void readFromParcel(Parcel p) {
        _inProgress = p.readInt() != 0;
        _documentCount = p.readInt();
        _firstUnsentNumber = p.readInt();
        _firstUnsentDate = p.readLong();
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
