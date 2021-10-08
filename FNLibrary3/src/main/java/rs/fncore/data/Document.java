package rs.fncore.data;

import android.os.Parcel;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import rs.fncore.Const;
import rs.utils.Utils;

/**
 * Фискальный документ
 *
 * @author nick
 */
public abstract class Document extends TLV implements IReableFromParcel {

    private static final String CLASS_NAME_TAG = "Class";
    private static final String LOCATION_TAG = "Location";
    protected static final int DDL_VERSION = 300;

    /**
     * Несовпадение версий документа
     *
     * @author nick
     */
    public class DDLException extends RuntimeException {

        private static final long serialVersionUID = 5030735810889733060L;

        public DDLException(int v) {
            super("Document version mismatch. Got " + v + " await " + DDL_VERSION);
        }
    }

    protected int mDDL = DDL_VERSION;
    protected Signature mSignature = new Signature(this);
    protected Location mLocation = new Location();

    public Document(JSONObject json) throws JSONException {
        super(json);

        if (json.has(LOCATION_TAG)) {
            mLocation = new Location(json.getJSONObject(LOCATION_TAG));
        }
    }

    public Document() {
    }

    /**
     * Получить теги документа в виде байт для записи с помощью команды 07
     * Длина каждого блока не более 1024 байт
     *
     * @return tags
     */
    public byte[][] pack() {
        List<byte[]> result = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.allocate(Const.MAX_TAG_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < size(); i++) {
            Tag tag = valueAt(i);
            if (buffer.position() + tag.size() >= buffer.capacity()) {
                byte[] b = new byte[buffer.position()];
                System.arraycopy(buffer.array(), 0, b, 0, b.length);
                result.add(b);
                buffer.clear();
            }
            buffer.putShort((short) (keyAt(i) & 0xFFFF));
            buffer.put(tag.pack());
        }
        if (buffer.position() > 0) {
            byte[] b = new byte[buffer.position()];
            System.arraycopy(buffer.array(), 0, b, 0, b.length);
            result.add(b);
        }
        return result.toArray(new byte[result.size()][]);
    }

    public StringBuilder printTags(){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            int key= keyAt(i);
            Tag tag = valueAt(i);
            res.append("key: ").append(key).append(",data: ").append(tag.toString()).append(System.getProperty("line.separator"));
        }
        return res;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(mDDL);
        p.writeInt(size());
        for (int i = 0; i < size(); i++) {
            p.writeInt(keyAt(i));
            valueAt(i).writeToParcel(p);
        }
        mLocation.writeToParcel(p, flags);
        if (mSignature != null) {
            p.writeInt(1);
            mSignature.writeToParcel(p, flags);
        } else
            p.writeInt(0);

    }

    public void readFromParcel(Parcel p) {
        mDDL = p.readInt();
        int size = p.readInt();
        clear();
        while (size-- > 0)
            put(p.readInt(), new Tag(p));
        mLocation.readFromParcel(p);
        if (p.readInt() != 0) {
            mSignature = new Signature(this);
            mSignature.readFromParcel(p);
        }
    }

    /**
     * Получить адрес расчетов
     *
     * @return location
     */
    public Location getLocation() {
        return mLocation;
    }

    /**
     * Документ сохранен в ФН?
     *
     * @return check signature
     */
    public boolean isSigned() {
        return mSignature.mFPD != 0;
    }

    /**
     * Получить фискальную подпись документа
     *
     * @return signature
     */
    public Signature signature() {
        return mSignature;
    }

    protected void sign(ByteBuffer bb, Signer signer, OU operator, long signTime) {
        mSignature = new Signature(this, signer, signTime);
        operator.cloneTo(mSignature.operator());
        mSignature.mNumber = bb.getInt();
        mSignature.mFPD = Utils.readUint32LE(bb);
    }

    /**
     * Получить имя класса документа
     *
     * @return
     */
    public abstract String getClassName();

    /**
     * Получить UUID класса документа
     *
     * @return
     */
    public abstract String getClassUUID();

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject result = super.toJSON();

        result.put(CLASS_NAME_TAG, getClassName());
        result.put(LOCATION_TAG, mLocation.toJSON());

        return result;
    }
}
