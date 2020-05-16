package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import rs.fncore.Const;
import rs.utils.Utils;

/**
 * Адрес и 
 * @author nick
 *
 */
public class Location implements Parcelable {

	private static final String ADDRESS_TAG = "a";
	private static final String PLACE_TAG = "p";
	private String _address = Const.EMPTY_STRING;
	private String _place = Const.EMPTY_STRING;
	public Location(JSONObject json) throws JSONException {
		_address = json.getString(ADDRESS_TAG);
		_place = json.getString(PLACE_TAG);
	}
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		try {
			result.put(ADDRESS_TAG, _address);
			result.put(PLACE_TAG, _place);
		} catch(JSONException jse) {}  
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
		p.writeString(_address);
		p.writeString(_place);
		
	}
	public void readFromParcel(Parcel p) {
		_address = p.readString();
		_place = p.readString();
	}
	/**
	 * Получить адрес расчетов
	 * @return
	 */
	public String getAddress() { return _address; }
	/**
	 * Установить адрес расчетов
	 * @param s
	 */
	public void setAddress(String s) {
		if(s == null) s = Const.EMPTY_STRING;
		_address = s;
	}
	/**
	 * Получить место расчетов
	 * @return
	 */
	public String getPlace() { return _place; }
	/**
	 * Установить место расчетов
	 * @param s
	 */
	public void setPlace(String s) {
		if(s == null) s = Const.EMPTY_STRING;
		_place = s;
	}
	/**
	 * Скопировать в другой объект
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
