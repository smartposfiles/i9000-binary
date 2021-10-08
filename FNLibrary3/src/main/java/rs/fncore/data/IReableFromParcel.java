package rs.fncore.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Интерфейс, описывающий класс который может быть записан/прочитан из Parcel
 *
 * @author nick
 */
public interface IReableFromParcel extends Parcelable {
    void readFromParcel(Parcel p);
}
