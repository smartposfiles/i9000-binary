package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import rs.fncore.Const;

/**
 * Передаваемый через границы процесса список строк  (List<String>)
 *
 * @author nick
 */
public class ParcelableStrings extends SerializableList<String> {

    private static final long serialVersionUID = -8330054965761393712L;

    public ParcelableStrings() {
    }

    @Override
    public String get(int index) {
        if (index >= size())
            return Const.EMPTY_STRING;
        return super.get(index);
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
