package rs.fncore.data;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import rs.fncore.Const;
/**
 * Фискальный документ
 * @author nick
 *
 */
public abstract class Document extends TLV implements IReableFromParcel {

	private static final String CLASS_NAME_TAG = "Class";
	private static final String LOCATION_TAG = "Location";
	protected static final int DDL_VERSION = 100;
	private final String TAG=this.getClass().getName();

	/**
	 * Несовпадение версий документа
	 * @author nick
	 *
	 */
	public class DDLException extends RuntimeException {

		private static final long serialVersionUID = 5030735810889733060L;
		public DDLException(int v) {
			super("Document version mismatch. Got "+v+" await "+DDL_VERSION);
		}
	}
	
	protected int _DDL = DDL_VERSION;
	protected Signature _signature = new Signature(this);
	protected Location _location = new Location();
	 
	public Document(JSONObject json) {
		super(json);
		try {
			if(json.has(LOCATION_TAG))
				_location = new Location(json.getJSONObject(LOCATION_TAG));
		} catch(JSONException jse) {
			Log.e(TAG,"exception",jse);
		}
		
	}
	public Document() {
		add(1009,Const.EMPTY_STRING);
		add(1187,Const.EMPTY_STRING);
	}

	/**
	 * Получить теги документа в виде байт для записи с помощью команды 07
	 * Длина каждого блока не более 1024 байт
	 * @return
	 */
	public  byte [][] pack() {
		add(1009,_location.getAddress());
		add(1187,_location.getPlace());
		List<byte []> result = new ArrayList<>();
		ByteBuffer buffer = ByteBuffer.allocate(Const.MAX_TAG_SIZE);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		for(int i=0;i<size();i++) {
			Tag tag = valueAt(i);
			if(buffer.position() + tag.size() >= buffer.capacity() ) {
				byte [] b = new byte[buffer.position()];
				System.arraycopy(buffer.array(), 0, b, 0, b.length);
				result.add(b);
				buffer.clear();
			}
			buffer.putShort((short)(keyAt(i) & 0xFFFF));
			buffer.put(tag.pack());
		}
		if(buffer.position() > 0) {
			byte [] b = new byte[buffer.position()];
			System.arraycopy(buffer.array(), 0, b, 0, b.length);
			result.add(b);
		}
		return result.toArray(new byte [result.size()][]);
	}
	
	

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(_DDL);
		p.writeInt(size());
		for(int i=0;i<size();i++) {
			p.writeInt(keyAt(i));
			valueAt(i).writeToParcel(p);
		}
		_location.writeToParcel(p, flags);
		if(_signature != null) {
			p.writeInt(1);
			_signature.writeToParcel(p, flags);
		} else
			p.writeInt(0);
		
	}
	public void readFromParcel(Parcel p) {
		_DDL = p.readInt(); 
		int size = p.readInt();
		clear();
		while(size-- > 0) 
			put(p.readInt(),new Tag(p));
		_location.readFromParcel(p);
		if(p.readInt() != 0) {
			_signature = new Signature(this);
			_signature.readFromParcel(p);
		}
	}
	
	/**
	 * Получить адрес расчетов
	 * @return
	 */
	public Location getLocation() { return _location; }
	/**
	 * Документ сохранен в ФН?
	 * @return
	 */
	public boolean isSigned() { return _signature._fpd != 0; }
	/**
	 * Получить фискальную подпись документа
	 * @return
	 */
	public Signature signature() { return _signature; }
	
	protected void sign(ByteBuffer bb, Signer signer, OU operator, long signTime) {
		_signature = new Signature(this,signer,signTime);
		operator.cloneTo(_signature.operator());
		_signature._number = bb.getInt();
		_signature._fpd = (bb.getInt() & 0xffffffffl);
	}
	/**
	 * Получить имя класса документа
	 * @return
	 */
	public abstract String getClassName();

	@Override
	public JSONObject toJSON() {
		JSONObject result = super.toJSON();
		try {
			result.put(CLASS_NAME_TAG, getClassName());
			result.put(LOCATION_TAG, _location.toJSON());
		} catch(JSONException jse) {
			Log.e(TAG,"exception",jse);
		}
		return result;
	}
	
}
