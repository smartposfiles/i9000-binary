package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Настройки параметров печати
 *
 * @author nick
 */
public class PrintSettings implements Parcelable {

    private int[] mMargins = {0, 0};
    public static final String DEFAULT_FONT = "monospace";
    private String mDefaultFontName = DEFAULT_FONT;
    private int mDefaultFontSize = 20;

    public PrintSettings() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {
        p.writeInt(mMargins[0]);
        p.writeInt(mMargins[1]);
        p.writeInt(mDefaultFontSize);
        p.writeString(mDefaultFontName);

    }

    public void readFromParcel(Parcel p) {
        mMargins[0] = p.readInt();
        mMargins[1] = p.readInt();
        mDefaultFontSize = p.readInt();
        mDefaultFontName = p.readString();
    }

    /**
     * Получить значения в точках левого и правого отступов
     *
     * @return
     */
    public int[] getMargins() {
        return mMargins;
    }

    /**
     * Установить значения отступов
     *
     * @param margins - отсупы (левый, правый)
     */
    public void setMargins(int... margins) {
        if (margins.length > 0 && margins[0] > -1) mMargins[0] = margins[0];
        if (margins.length > 1 && margins[1] > -1) mMargins[1] = margins[1];
    }

    /**
     * Размер шрифта по умолчанию
     *
     * @return
     */
    public int getDefaultFontSize() {
        return mDefaultFontSize;
    }

    /**
     * Установить размер шрифта по умолчанию
     *
     * @param v
     */
    public void setDefaultFontSize(int v) {
        if (v > 0) mDefaultFontSize = v;
    }

    /**
     * Наименование гарнитуры шрифта по умолчанию
     *
     * @return
     */
    public String getDefaultFontName() {
        return mDefaultFontName;
    }

    /**
     * Установить шрифт по умолчанию
     *
     * @param val
     */
    public void setDefaultFontName(String val) {
        if (val != null && !val.isEmpty()) mDefaultFontName = val;
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
