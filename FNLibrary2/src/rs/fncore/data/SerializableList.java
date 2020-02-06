package rs.fncore.data;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;

public class SerializableList<T extends Serializable> extends ArrayList<T> implements IReableFromParcel {

	private static final long serialVersionUID = 7875774978352541994L;

	public SerializableList() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		p.writeInt(size());
		for(T elem: this)
			p.writeSerializable(elem);
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readFromParcel(Parcel p) {
		int cnt = p.readInt();
		while(cnt-- > 0) try {
			add((T)p.readSerializable());
		} catch(Exception e) { }
	}

}
