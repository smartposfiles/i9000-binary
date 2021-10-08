package rs.fncore.data;

import android.os.Parcel;
import android.util.Log;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Передаваемый через Parcel список
 *
 * @param <T>
 * @author nick
 */
public class ParcelableList<T extends IReableFromParcel> extends ArrayList<T> implements IReableFromParcel {

    final Logger mLogger = Logger.getLogger("ParcelableList");
    private static final long serialVersionUID = 4586905436984182905L;
    private Class<T> mClazz;

    public ParcelableList(Class<T> clazz) {
        mClazz = clazz;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(size());
        for (T elem : this)
            elem.writeToParcel(p, flags);

    }

    @Override
    public void readFromParcel(Parcel p) {
        clear();
        int count = p.readInt();
        while (count-- > 0) {
            try {
                T elem = mClazz.newInstance();
                elem.readFromParcel(p);
                add(elem);
            } catch (Exception e) {
                mLogger.log(Level.SEVERE, "exception", e);
                return;
            }
        }

    }

}
