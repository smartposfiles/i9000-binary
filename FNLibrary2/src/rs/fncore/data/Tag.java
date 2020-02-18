package rs.fncore.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.util.Pair;
import android.util.SparseArray;
import rs.fncore.Const;
import rs.utils.Utils;

/**
 * LV тег
 * 
 * @author nick
 *
 */
public class Tag {

	private static final String TYPE_TAG = "t";
	private static final String SIZE_TAG = "s";
	private static final String DATA_TAG = "d";
	private static final int SHORT_SIZE = 2;

	private ByteBuffer _data = ByteBuffer.allocate(Const.MAX_TAG_SIZE);
	private DataType _type;

	private enum DataType {
		b, i1, i2, i4, tlv, d, s, f, f2, r
	}

	public byte[] pack() {
		byte[] result = new byte[_data.position() + SHORT_SIZE];
		result[0] = (byte) (_data.position() & 0xFF);
		result[1] = (byte) ((_data.position() >> 8) & 0xFF);
		System.arraycopy(_data.array(), 0, result, 2, _data.position());
		return result;
	}

	private static double round2(double number, int scale) {
		int pow = 10;
		for (int i = 1; i < scale; i++)
			pow *= 10;
		double tmp = number * pow;
		return (((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp)) / (double) pow;
	}

	private Tag() {
		_data.order(ByteOrder.LITTLE_ENDIAN);
	}

	public Tag(Parcel p) {
		this();
		readFromParcel(p);
	}

	public Tag(byte value) {
		this();
		_type = DataType.i1;
		_data.put(value);
	}

	public Tag(boolean value) {
		this();
		_type = DataType.b;
		_data.put((byte) (value ? 1 : 0));
	}

	public Tag(short value) {
		this();
		_type = DataType.i2;
		_data.putShort(value);
	}

	public Tag(int value) {
		this();
		_type = DataType.i4;
		_data.putInt(value);
	}

	public Tag(String s) {
		this();
		_type = DataType.s;
		_data.put(s.getBytes(Const.ENCODING));
	}

	public Tag(double value, int digits) {
		this();
		if (digits == 2)
			_type = DataType.f2;
		else
			_type = DataType.f;
		long v = (int) (round2(value, digits) * Math.pow(10, digits));
		if (digits > 2)
			_data.put((byte) (digits & 0xFF));
		if (v < 256)
			_data.put((byte) (v & 0xFF));
		else if (v < 65536)
			_data.putShort((short) (v & 0xFFFF));
		else
			_data.putInt((int) v);
	}

	public Tag(Pair<Integer, Tag>[] tags) {
		this();
		_type = DataType.tlv;
		for (int i = 0; i < tags.length; i++) {
			_data.putShort((short) (tags[i].first.intValue() & 0xFFFF));
			_data.put(tags[i].second.pack());
		}
	}

	public Tag(Date date) {
		this();
		_type = DataType.d;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		_data.put((byte) (cal.get(Calendar.YEAR) - 2000));
		_data.put((byte) cal.get(Calendar.MONTH));
		_data.put((byte) cal.get(Calendar.DAY_OF_MONTH));
		_data.put((byte) cal.get(Calendar.HOUR_OF_DAY));
		_data.put((byte) cal.get(Calendar.MINUTE));
	}

	public Tag(SparseArray<Tag> tags) {
		this();
		_type = DataType.tlv;
		for (int i = 0; i < tags.size(); i++) {
			if(tags.keyAt(i) > Short.MAX_VALUE) 
				continue;
			_data.putShort((short) (tags.keyAt(i) & 0xFFFF));
			_data.put(tags.valueAt(i).pack());
		}
	}

	public Tag(JSONObject o) throws JSONException {
		this();
		_type = DataType.valueOf(o.getString(TYPE_TAG));
		_data.put(Utils.hex2bytes(o.getString(DATA_TAG)));
	}
	public Tag(byte[] raw) {
		_type = DataType.r;
		_data.put(raw);
	}

	public Tag(ByteBuffer bb)  {
		_type = DataType.r;
		byte [] raw = new byte[bb.getShort()];
		bb.get(raw);
		_data.put(raw);
	}
	public Tag(Tag source) {
		_type = source._type;
		_data.put(source._data.array(), 0, source._data.position());
	}

	public Tag get(int id) {
		if (_type != DataType.tlv && _type != DataType.r)
			return null;
		if (_data.position() < 5)
			return null;
		int p = _data.position();
		try {
			_data.position(0);
			while (_data.position() < p) {
				int tagId = (_data.getShort() & 0xFFFF);
				int size = (_data.getShort() & 0xFF);
				if (p - _data.position() < size)
					return null;
				byte[] raw = new byte[size];
				_data.get(raw);
				if (tagId == id)
					return new Tag(raw);
			}
			return null;
		} finally {
			_data.position(p);
		}

	}

	public byte asByte() {
		return _data.array()[0];
	}

	public double asDouble() {
		double v = 100.0;
		int p = _data.position();
		_data.position(0);
		try {
			if (_type == DataType.f)
				v = Math.pow(10, _data.get());
			if (_data.remaining() == 1)
				return (_data.get() & 0xFF) / v;
			if (_data.position() == 2)
				return (_data.getShort() & 0xFFFF) / v;
			return (_data.getInt() & 0xFFFFFFFFL) / v;
		} finally {
			_data.position(p);
		}
	}

	public short asShort() {
		return (short) (((_data.array()[0] << 8) | _data.array()[0]) & 0xFFFF);
	}

	public int asInt() {
		return (_data.array()[3] << 24) | (_data.array()[2] << 16) | (_data.array()[1] << 8) | _data.array()[0];
	}

	public String asString() {
		return new String(_data.array(), 0, _data.position(), Const.ENCODING);
	}

	public int size() {
		return _data.position() + SHORT_SIZE;
	}

	public long asTimeStamp() {
		int p = _data.position();
		_data.position(0);
		try {
			return Utils.readDate5(_data);
		} finally {
			_data.position(p);
		}
	}

	public void writeToParcel(Parcel p) {
		p.writeInt(_type.ordinal());
		byte[] self = pack();
		p.writeInt(_data.position());
		p.writeByteArray(self);
	}

	public void readFromParcel(Parcel p) {
		_type = DataType.values()[p.readInt()];
		byte[] data = new byte[p.readInt() + SHORT_SIZE];
		p.readByteArray(data);
		_data.clear();
		_data.put(data, SHORT_SIZE, data.length - SHORT_SIZE);
	}

	public boolean asBoolean() {
		return _data.array()[0] != 0;
	}

	@SuppressLint("DefaultLocale")
	@Override
	public String toString() {
		switch (_type) {
		case b:
			return asBoolean() ? "Да" : "Нет";
		case i1:
			return String.valueOf(asByte());
		case i2:
			return String.valueOf(asShort());
		case i4:
			return String.valueOf(asInt());
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
			return Utils.dump(_data.array(), 0, _data.position());
		}
		return super.toString();
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		try {
			result.put(TYPE_TAG, _type.name());
			result.put(SIZE_TAG,_data.position());
			result.put(DATA_TAG,Utils.dump(_data.array(), 0, _data.position()));
		} catch (JSONException jse) {

		}
		return result;
	}
}
