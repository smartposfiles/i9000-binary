package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Подпись фискального документа
 * @author nick
 *
 */
public class Signature implements Parcelable {
	protected int _number;
	protected long _fpd;
	protected long _sign_date;
	protected Signer _signer;
	protected OU _operator = new OU();
	private Document _owner;
	
	public Signature(Document owner) { 
		_owner = owner;
	}
	public Signature(Document owner, Signer signer, long signTime) {
		_owner = owner;
		_signer = signer;
		_sign_date = signTime;
	}
	
	/**
	 * Фискальный номер документа
	 * @return
	 */
	public int number() { return _number; }
	/**
	 * Фискальная подпись документа
	 * @return
	 */
	public long fpd() { return _fpd; }
	
	/**
	 * Дата/время подписи
	 * @return
	 */
	public long signDate() { return _sign_date; }
	/**
	 * Информация об оборудовании выполнившем фискальную операцию
	 * @return
	 */
	public Signer signer() { return _signer; }
	/**
	 * Оператор (кассир), выполнивший фискализацию
	 * @return
	 */
	public OU operator() { return _operator; }
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(_number);
		p.writeLong(_fpd);
		p.writeLong(_sign_date);
		if(_signer != null) {
			p.writeInt(1);
			_signer.writeToParcel(p,flags); 
		} else
			p.writeInt(0);
		_operator.writeToParcel(p, flags);
	}
	public void readFromParcel(Parcel p) {
		_number = p.readInt();
		_fpd = p.readLong() & 0xFFFFFFFFl;
		_sign_date = p.readLong();
		if(p.readInt() != 0) {
			_signer = new Signer();
			_signer.readFromParcel(p);
		}
		if(_owner._DDL > 100)
			_operator.readFromParcel(p);
	}
}
