package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Передаваемый через границы процесса список строк  (List<String>)
 * @author nick
 *
 */
public class ParcelableStrings  extends SerializableList<String>{

	private static final long serialVersionUID = -8330054965761393712L;
	public ParcelableStrings() {
		// TODO Auto-generated constructor stub
	}

	public static final Parcelable.Creator<ParcelableStrings> CREATOR = new Parcelable.Creator<ParcelableStrings>() {

		@Override
		public ParcelableStrings createFromParcel(Parcel p) {
			ParcelableStrings result = new ParcelableStrings();
			result.readFromParcel(p);
			return result;
		}

		@Override
		public ParcelableStrings[] newArray(int size) {
			return new ParcelableStrings[size];
		}
		
	};
}
