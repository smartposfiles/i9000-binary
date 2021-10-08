package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

import rs.fncore.Const;
import rs.utils.Utils;

/**
 * Адрес и
 *
 * @author nick
 */
public class Location implements Parcelable {

    private static final String ADDRESS_TAG = "a";
    private static final String PLACE_TAG = "p";
    private String mAddress = Const.EMPTY_STRING;
    private String mPlace = Const.EMPTY_STRING;

    public Location(JSONObject json) throws JSONException {
        mAddress = json.getString(ADDRESS_TAG);
        mPlace = json.getString(PLACE_TAG);
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;
        Location newValue = (Location) o;
        // field comparison
        return mAddress.equals(newValue.mAddress)
                && mPlace.equals(newValue.mPlace);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();

        result.put(ADDRESS_TAG, mAddress);
        result.put(PLACE_TAG, mPlace);

        return result;
    }

    public Location() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {
        p.writeString(mAddress);
        p.writeString(mPlace);

    }

    public void readFromParcel(Parcel p) {
        mAddress = p.readString();
        mPlace = p.readString();
    }

    /**
     * Получить адрес расчетов
     *
     * @return
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Установить адрес расчетов
     *
     * @param s
     */
    public void setAddress(String s) {
        if (s == null) s = Const.EMPTY_STRING;
        mAddress = s;
    }

    /**
     * Получить место расчетов
     *
     * @return
     */
    public String getPlace() {
        return mPlace;
    }

    /**
     * Установить место расчетов
     *
     * @param s
     */
    public void setPlace(String s) {
        if (s == null) s = Const.EMPTY_STRING;
        mPlace = s;
    }

    /**
     * Скопировать в другой объект
     *
     * @param dest
     */
    public void cloneTo(Location dest) {
        Parcel p = Utils.writeToParcel(this);
        dest.readFromParcel(p);
        p.recycle();

    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {

        @Override
        public Location createFromParcel(Parcel p) {
            Location result = new Location();
            result.readFromParcel(p);
            return result;
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }

    };
}
