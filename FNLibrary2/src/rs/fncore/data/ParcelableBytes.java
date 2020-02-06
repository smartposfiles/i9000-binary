package rs.fncore.data;

import android.os.Parcel;
/**
 * Передаваемый через границу процесса массив байт
 * @author nick
 *
 */
public class ParcelableBytes implements IReableFromParcel {

	protected byte [] _raw;
	public ParcelableBytes() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		if(_raw != null) {
			p.writeInt(_raw.length);
			p.writeByteArray(_raw);
		} else
			p.writeInt(0);
		
	}
	/**
	 * Получить массив байт
	 * @return
	 */
	public byte [] getRawBytes() { return _raw; }
	/**
	 * Установить массив байт
	 * @param raw
	 */
	public void setRawBytes(byte [] raw) {
		_raw = raw;
	}
	
	@Override
	public void readFromParcel(Parcel p) {
		int size = p.readInt();
		if(size > 0) {
			_raw = new byte[size];
			p.readByteArray(_raw);
		}
	}

}
