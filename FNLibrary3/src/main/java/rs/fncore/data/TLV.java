package rs.fncore.data;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import rs.utils.Utils;

/**
 * Коллекция TLV
 *
 * @author nick
 */
public class TLV extends SparseArray<Tag> {

    private static final String TAG_ID_TAG = "i";
    private static final String TAGS_TAG = "tags";

    public TLV() {
    }

    public void addAll(byte[] bb) {
        ByteBuffer buf = ByteBuffer.wrap(bb);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        addAll(buf);
    }

    public void addAll(ByteBuffer bb) {
        while (bb.position() < bb.limit() - 4) {
            int tagId = Utils.readUint16LE(bb);
            put(tagId, new Tag(bb));
        }
    }

    public TLV(JSONObject json) throws JSONException {
        if (json.has(TAGS_TAG)) {
            JSONArray a = json.getJSONArray(TAGS_TAG);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (o.has(TAG_ID_TAG))
                    put(o.getInt(TAG_ID_TAG), new Tag(o));
            }
        }
    }

    public TLV(int initialCapacity) {
        super(initialCapacity);
    }

    public TLV(TLV source) {
        if (source != null)
            for (int i = 0; i < source.size(); i++) {
                put(source.keyAt(i), new Tag(source.valueAt(i)));
            }
    }

    @Override
    public void put(int key, Tag value) {
        if (value != null)
            super.put(key, value);
    }

    /************ Добавление тегов ****************/
    public void add(int tag, byte value) {
        put(tag, new Tag(value));
    }

    public void add(int tag, boolean value) {
        put(tag, new Tag(value));
    }

    public void add(int tag, short value) {
        put(tag, new Tag(value));
    }

    public void add(int tag, int value) {
        put(tag, new Tag(value));
    }

    public void add(int tag, BigDecimal value) {
        put(tag, new Tag(value, 2));
    }

    public void add(int tag, BigDecimal value, int digits) {
        put(tag, new Tag(value, digits));
    }

    public void add(int tag, String s) {
        put(tag, new Tag(s));
    }

    public void add(int tag, byte[] b) {
        put(tag, new Tag(b));
    }

    public boolean hasTag(int tag) {
        return get(tag) != null;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray tags = new JSONArray();

        for (int i = 0; i < size(); i++) {
            JSONObject jTag = valueAt(i).toJSON();
            jTag.put(TAG_ID_TAG, keyAt(i));
            tags.put(jTag);
        }
        result.put(TAGS_TAG, tags);

        return result;
    }

    public byte[] packAll() {
        ByteBuffer bb = ByteBuffer.allocate(32768);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < size(); i++) {
            bb.putShort((short) (keyAt(i) & 0xFFFF));
            bb.put(valueAt(i).pack());
        }
        byte[] result = new byte[bb.position()];
        System.arraycopy(bb.array(), 0, result, 0, bb.position());
        return result;
    }
}
