package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Подпись фискального документа
 *
 * @author nick
 */
public class Signature implements Parcelable {
    protected int mNumber;
    protected long mFPD;
    protected long mSignDate;
    protected Signer mSigner;
    protected OU mOperator = new OU();
    private Document mOwner;

    public Signature(Document owner) {
        mOwner = owner;
    }

    public Signature(Document owner, Signer signer, long signTime) {
        mOwner = owner;
        mSigner = signer;
        mSignDate = signTime;
    }

    /**
     * Фискальный номер документа
     *
     * @return
     */
    public int number() {
        return mNumber;
    }

    /**
     * Фискальная подпись документа
     *
     * @return
     */
    public long fpd() {
        return mFPD;
    }

    /**
     * Дата/время подписи
     *
     * @return
     */
    public long signDate() {
        return mSignDate;
    }

    /**
     * Информация об оборудовании выполнившем фискальную операцию
     *
     * @return
     */
    public Signer signer() {
        return mSigner;
    }

    /**
     * Оператор (кассир), выполнивший фискализацию
     *
     * @return
     */
    public OU operator() {
        return mOperator;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(mNumber);
        p.writeLong(mFPD);
        p.writeLong(mSignDate);
        if (mSigner != null) {
            p.writeInt(1);
            mSigner.writeToParcel(p, flags);
        } else
            p.writeInt(0);
    }

    public void readFromParcel(Parcel p) {
        mNumber = p.readInt();
        mFPD = p.readLong();
        mSignDate = p.readLong();
        if (p.readInt() != 0) {
            mSigner = new Signer();
            mSigner.readFromParcel(p);
        }
    }
}
