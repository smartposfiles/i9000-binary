package rs.fncore.data;

import android.os.Parcel;

import java.util.ArrayList;

/**
 * Передаваемый через Parcel список
 * @author nick
 *
 * @param <T>
 */
public class ParcelableList<T extends IReableFromParcel> extends ArrayList<T> implements IReableFromParcel {

	private static final long serialVersionUID = 4586905436984182905L;
	private Class<T> _clazz;
	public ParcelableList(Class<T> clazz) {
		_clazz = clazz;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(size());
		for(T elem: this)
			elem.writeToParcel(p,flags);
		
	}

	@Override
	public void readFromParcel(Parcel p) {
		clear();
		int count = p.readInt();
		while(count--> 0) {
			try {
				T elem = _clazz.newInstance();
				elem.readFromParcel(p);
				add(elem);
			} catch(Exception e) {
				return;
			}
		}
		
	}

}
