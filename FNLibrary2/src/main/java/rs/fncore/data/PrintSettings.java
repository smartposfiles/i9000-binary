package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Настройки параметров печати
 * @author nick
 *
 */
public class PrintSettings implements Parcelable {

	private int []_margins = {0,0};
	public static final String DEFAULT_FONT = "Monospace"; 
	private String _default_font_name = DEFAULT_FONT;
	private int _default_font_size = 20;
	public PrintSettings() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int arg1) {
		p.writeInt(_margins[0]);
		p.writeInt(_margins[1]);
		p.writeInt(_default_font_size);
		p.writeString(_default_font_name);

	}
	public void readFromParcel(Parcel p) {
		_margins[0] = p.readInt();
		_margins[1] = p.readInt();
		_default_font_size = p.readInt();
		_default_font_name = p.readString();
	}
	/**
	 * Получить значения в точках левого и правого отступов
	 * @return
	 */
	public int [] getMargins() { return _margins;  }
	/**
	 * Установить значения отступов
	 * @param margins - отсупы (левый, правый)
	 */
	public void setMargins(int...margins) {
		if(margins.length > 0 && margins[0] > -1) _margins[0] = margins[0];
		if(margins.length > 1 && margins[1] > -1) _margins[1] = margins[1];
	}
	/**
	 * Размер шрифта по умолчанию
	 * @return
	 */
	public int getDefaultFontSize() { return _default_font_size; }
	/**
	 * Установить размер шрифта по умолчанию
	 * @param v
	 */
	public void setDefaultFontSize(int v) {
		if(v > 0) _default_font_size = v;  
	}
	/**
	 * Наименование гарнитуры шрифта по умолчанию
	 * @return
	 */
	public String getDefaultFontName() { return _default_font_name; }
	/**
	 * Установить шрифт по умолчанию
	 * @param val
	 */
	public void setDefaultFontName(String val) {
		if(val != null && !val.isEmpty()) _default_font_name = val;
	}
	
	
	public static final Parcelable.Creator<PrintSettings> CREATOR = new Creator<PrintSettings>() {
		
		@Override
		public PrintSettings[] newArray(int size) {
			return new PrintSettings[size];
		}
		
		@Override
		public PrintSettings createFromParcel(Parcel p) {
			PrintSettings result = new PrintSettings();
			result.readFromParcel(p);
			return result;
		}
	};

}
