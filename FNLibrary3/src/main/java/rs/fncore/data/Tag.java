package rs.fncore.data;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.fncore.Const;
import rs.utils.Utils;

/**
 * LV тег
 *
 * @author nick
 */
public class Tag {

    private static final String TYPE_TAG = "t";
    private static final String SIZE_TAG = "s";
    private static final String DATA_TAG = "d";
    private static final int SHORT_SIZE = 2;

    private ByteBuffer mData = ByteBuffer.allocate(Const.MAX_TAG_SIZE);
    private DataTypeE mType;

    private enum DataTypeE {
        b, i1, i2, i4, tlv, d, s, f, f2, r, u4, u2
    }

    public byte[] pack() {
        byte[] result = new byte[mData.position() + SHORT_SIZE];
        result[0] = (byte) (mData.position() & 0xFF);
        result[1] = (byte) ((mData.position() >> 8) & 0xFF);
        System.arraycopy(mData.array(), 0, result, 2, mData.position());
        return result;
    }

    private Tag() {
        mData.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Tag(Parcel p) {
        this();
        readFromParcel(p);
    }

    public Tag(byte value) {
        this();
        mType = DataTypeE.i1;
        mData.put(value);
    }

    public Tag(boolean value) {
        this();
        mType = DataTypeE.b;
        mData.put((byte) (value ? 1 : 0));
    }

    public Tag(short value) {
        this();
        mType = DataTypeE.i2;
        mData.putShort(value);
    }

    public Tag(int value) {
        this();
        mType = DataTypeE.i4;
        mData.putInt(value);
    }

    public Tag(long value) {
        this();
        mType = DataTypeE.u4;
        mData.putInt((int)value);
    }

    public Tag(String s) {
        this();
        mType = DataTypeE.s;
        mData.put(s.getBytes(Const.ENCODING));
    }

    public Tag(BigDecimal value, int digits) {
        this();
        if (digits == 2)
            mType = DataTypeE.f2;
        else
            mType = DataTypeE.f;
        long v = (Utils.round2(value, digits).multiply(new BigDecimal(Math.pow(10, digits)), MathContext.DECIMAL128)).intValue();
        if (digits > 2)
            mData.put((byte) (digits & 0xFF));
        if (v < 256)
            mData.put((byte) (v & 0xFF));
        else if (v < 65536)
            mData.putShort((short) (v & 0xFFFF));
        else
            mData.putInt((int) v);
    }

    public Tag(Pair<Integer, Tag>[] tags) {
        this();
        mType = DataTypeE.tlv;
        for (Pair<Integer, Tag> tag : tags) {
            mData.putShort((short) (tag.first.intValue() & 0xFFFF));
            mData.put(tag.second.pack());
        }
    }

    public Tag(List<Pair<Integer, Tag>> tags) {
        this();
        mType = DataTypeE.tlv;
        for (Pair<Integer, Tag> tag : tags) {
            mData.putShort((short) (tag.first.intValue() & 0xFFFF));
            mData.put(tag.second.pack());
        }
    }

    public Tag(Date date) {
        this();
        mType = DataTypeE.d;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        mData.put((byte) (cal.get(Calendar.YEAR) - 2000));
        mData.put((byte) cal.get(Calendar.MONTH));
        mData.put((byte) cal.get(Calendar.DAY_OF_MONTH));
        mData.put((byte) cal.get(Calendar.HOUR_OF_DAY));
        mData.put((byte) cal.get(Calendar.MINUTE));
    }

    public Tag(SparseArray<Tag> tags) {
        this();
        mType = DataTypeE.tlv;
        for (int i = 0; i < tags.size(); i++) {
            if (tags.keyAt(i) > Short.MAX_VALUE || tags.keyAt(i)==0)
                continue;
            mData.putShort((short) (tags.keyAt(i) & 0xFFFF));
            mData.put(tags.valueAt(i).pack());
        }
    }

    public Tag(JSONObject o) throws JSONException {
        this();
        mType = DataTypeE.valueOf(o.getString(TYPE_TAG));
        mData.put(Utils.hex2bytes(o.getString(DATA_TAG)));
    }

    public Tag(byte[] raw) {
        mType = DataTypeE.r;
        mData.put(raw);
    }

    public Tag(ByteBuffer bb) {
        mType = DataTypeE.r;
        int size=Utils.readUint16LE(bb);
        byte[] raw = new byte[size];
        bb.get(raw);
        mData.put(raw);
    }

    public Tag(Tag source) {
        mType = source.mType;
        mData.put(source.mData.array(), 0, source.mData.position());
    }

    public Tag get(int id) {
        if (mType != DataTypeE.tlv && mType != DataTypeE.r)
            return null;
        if (mData.position() < 5)
            return null;
        int p = mData.position();
        try {
            mData.position(0);
            while (mData.position() < p) {
                int tagId = Utils.readUint16LE(mData);
                int size = Utils.readUint16LE(mData);
                if (p - mData.position() < size)
                    return null;
                byte[] raw = new byte[size];
                mData.get(raw);
                if (tagId == id)
                    return new Tag(raw);
            }
            return null;
        } finally {
            mData.position(p);
        }

    }

    public byte asByte() {
        return mData.array()[0];
    }

    public double asDouble() {
        double v = 100.0;
        int p = mData.position();
        mData.position(0);
        try {
            if (mType == DataTypeE.f)
                v = Math.pow(10, mData.get());
            if (mData.remaining() == 1)
                return (mData.get() & 0xFF) / v;
            if (mData.position() == 2)
                return Utils.readUint16LE(mData) / v;
            return Utils.readUint32LE(mData) / v;
        } finally {
            mData.position(p);
        }
    }

    public short asShort() {
        return (short) (((mData.array()[0] << 8) | mData.array()[0]) & 0xFFFF);
    }

    public int asUShort() {
        return ((((int) mData.array()[0]& 0xff << 8) | mData.array()[0]& 0xff) & 0xFFFF);
    }

    public int asInt() {
        return (mData.array()[3] << 24) | (mData.array()[2] << 16) | (mData.array()[1] << 8) | mData.array()[0];
    }

    public long asUInt() {
        return (((long) mData.array()[3] & 0xff) << 24) | ((mData.array()[2]& 0xff) << 16) | ((mData.array()[1]& 0xff) << 8) | (mData.array()[0]& 0xff);
    }

    public String asString() {
        return new String(mData.array(), 0, mData.position(), Const.ENCODING);
    }

    public int size() {
        return mData.position() + SHORT_SIZE;
    }

    public long asTimeStamp() {
        int p = mData.position();
        mData.position(0);
        try {
            return Utils.readDate5(mData);
        } finally {
            mData.position(p);
        }
    }

    public void writeToParcel(Parcel p) {
        p.writeInt(mType.ordinal());
        byte[] self = pack();
        p.writeInt(mData.position());
        p.writeByteArray(self);
    }

    public void readFromParcel(Parcel p) {
        mType = DataTypeE.values()[p.readInt()];
        byte[] data = new byte[p.readInt() + SHORT_SIZE];
        p.readByteArray(data);
        mData.clear();
        mData.put(data, SHORT_SIZE, data.length - SHORT_SIZE);
    }

    public boolean asBoolean() {
        return mData.array()[0] != 0;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        switch (mType) {
            case b:
                return asBoolean() ? "Да" : "Нет";
            case i1:
                return String.valueOf(asByte());
            case i2:
                return String.valueOf(asShort());
            case i4:
                return String.valueOf(asInt());
            case u4:
                return String.valueOf(asUInt());
            case f:
                return String.format("%.3f", asDouble());
            case f2:
                return String.format("%.2f", asDouble());
            case s:
                return asString();
            case d:
                return Utils.formatDate(asTimeStamp());
            case tlv:
            case r:
                return Utils.dump(mData.array(), 0, mData.position());
        }
        return super.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put(TYPE_TAG, mType.name());
        result.put(SIZE_TAG, mData.position());
        result.put(DATA_TAG, Utils.dump(mData.array(), 0, mData.position()));

        return result;
    }
}
