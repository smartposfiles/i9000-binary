package rs.fncore.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import rs.fncore.Const;

/**
 * Данные о организации/сотруднике
 * @author nick
 *
 */
public class OU implements IReableFromParcel {

	private static final String NAME_TAG = "Name";
	private static final String INN_TAG = "INN";
	public static final String EMPTY_INN = "0000000000";
	private String _name = Const.EMPTY_STRING;
	private String _inn = EMPTY_INN;

	public OU(JSONObject json) throws JSONException {
		_name = json.getString(NAME_TAG);
		_inn =json.getString(INN_TAG);
	}
	public OU() { }
	/**
	 * 
	 * @param name - наименование
	 */
	public OU(String name) {
		_name = name;
	}
	/**
	 * 
	 * @param name - наименование
	 * @param inn - ИНН
	 */
	public OU(String name, String inn) {
		_name = name;
		_inn = inn;
	}
	/**
	 * Получить наименование
	 * @return
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Установить наименование
	 * @param name
	 */
	public void setName(String name) {
		_name = name;
	}
	/**
	 * Получить ИНН без дополнения нулями
	 * @return
	 */
	public String getINN() {
		return getINN(false);
	}

	/**
	 * Получить ИНН с/без дополнения нулями слева до 12 символов
	 * @param padded
	 * @return
	 */
	public String getINN(boolean padded) {
		if (_inn == null || _inn.isEmpty())
			return EMPTY_INN;
		String s = _inn;
		if (padded)
			while (s.length() < 12)
				s = "0" + s;
		return s;
	}
	/**
	 * Получить ИНН с обрезанными лидирующими нулями
	 * @return
	 */
	public String getINNtrimZ() {
		String s = getINN();
		while(s.startsWith("0") && s.length() > 10 ) s = s.substring(1);
		if(EMPTY_INN.equals(s)) return Const.EMPTY_STRING;
		return s;
	}

	/**
	 * Установить ИНН
	 * @param s
	 */
	public void setINN(String s) {
		if (s != null && !s.trim().isEmpty())
			_inn = s.trim();
		else
			_inn = EMPTY_INN;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		p.writeString(_inn);
		p.writeString(_name);
	}

	public void readFromParcel(Parcel p) {
		_inn = p.readString();
		_name = p.readString();
	}

	public void cloneTo(OU ou) {
		ou._inn = _inn;
		ou._name = _name;
	}

	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		try {
			result.put(NAME_TAG, _name);
			result.put(INN_TAG, _inn);
		} catch(JSONException jse) { }
		return result;
	}
	public static final Parcelable.Creator<OU> CREATOR = new Parcelable.Creator<OU>() {

		@Override
		public OU createFromParcel(Parcel p) {
			OU result = new OU();
			result.readFromParcel(p);
			return result;
		}

		@Override
		public OU[] newArray(int size) {
			return new OU[size];
		}

	};
}
