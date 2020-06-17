package rs.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import rs.fncore.Const;
import rs.fncore.data.Document;
import rs.fncore.data.IReableFromParcel;

/**
 * Вспомогательные методы
 *
 * @author nick
 */
public class Utils {
    private static final String TAG = "rs.utils.Utils";
    @SuppressLint("SimpleDateFormat")
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final DateFormat DATE_FORMAT_S = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static String getStackTrace(){
        String res="";
        StackTraceElement elem[] = Thread.currentThread().getStackTrace();
        for (int i=3;i<elem.length;i++) {
            res+="\n"+elem[i].toString();
        }
        return res;
    }

    /**
     * Округление до указанного количества знаков после запятой
     *
     * @param number
     * @param scale
     * @return
     */
    public static double round2(double number, int scale) {
        double pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (((long) ((tmp - (long) tmp) >= 0.5f ? tmp + 1 : tmp))) / pow;
    }

    /**
     * Округление до указанного количества знаков после запятой
     *
     * @param number
     * @param scale
     * @return
     */
    public static BigDecimal round2(BigDecimal number, int scale) {
        return number.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Восстановить Parcelable из массива байт
     *
     * @param bb
     * @param creator
     * @return
     */
    public static <T extends Parcelable> T deserialize(byte[] bb, Parcelable.Creator<T> creator) {
        Parcel p = Parcel.obtain();
        p.unmarshall(bb, 0, bb.length);
        p.setDataPosition(0);
        try {
            return creator.createFromParcel(p);
        } finally {
            p.recycle();
        }
    }

    /**
     * Прочитать экземпляр  IReableFromParcel из массива байт
     *
     * @param data
     * @param docjse
     */
    public static void deserialize(byte[] data, IReableFromParcel doc) {
        Parcel p = Parcel.obtain();
        p.unmarshall(data, 0, data.length);
        p.setDataPosition(0);
        doc.readFromParcel(p);
        p.recycle();
    }

    /**
     * Сохранить Parcelable как массив байт
     *
     * @param doc
     * @return
     */
    public static byte[] serialize(Parcelable doc) {
        Parcel p = Parcel.obtain();
        doc.writeToParcel(p, 0);
        p.setDataPosition(0);
        byte[] result = p.marshall();
        p.recycle();
        return result;
    }

    /**
     * Сохранить Parcelable
     *
     * @param doc
     * @return
     */
    public static Parcel writeToParcel(Parcelable doc) {
        Parcel p = Parcel.obtain();
        doc.writeToParcel(p, 0);
        p.setDataPosition(0);
        return p;
    }

    /**
     * Прочитать IReableFromParcel из массива байт
     *
     * @param doc
     * @param p
     */
    public static void readFromParcel(IReableFromParcel doc, Parcel p) {
        p.setDataPosition(0);
        doc.readFromParcel(p);
        p.recycle();
    }

    /**
     * Прочитать дату в формате YY mm DD HH MM
     *
     * @param bb
     * @return
     */
    public static long readDate5(ByteBuffer bb) {
        return readDate5(bb, Calendar.getInstance());
    }

    public static long readDate5(ByteBuffer bb, Calendar cal) {
        cal.set(Calendar.YEAR, bb.get() + 2000);
        cal.set(Calendar.MONTH, bb.get() - 1);
        cal.set(Calendar.DAY_OF_MONTH, bb.get());
        cal.set(Calendar.HOUR_OF_DAY, bb.get());
        cal.set(Calendar.MINUTE, bb.get());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static byte[] encodeDate(Calendar cal) {
        byte[] result = new byte[5];
        result[0] = (byte) (cal.get(Calendar.YEAR) - 2000);
        result[1] = (byte) (cal.get(Calendar.MONTH) + 1);
        result[2] = (byte) (cal.get(Calendar.DAY_OF_MONTH));
        result[3] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
        result[4] = (byte) (cal.get(Calendar.MINUTE));
        return result;
    }

    /**
     * Значение CRC16
     *
     * @param data
     * @param offset
     * @param length
     * @param nPoly
     * @return
     */
    public static short CRC16(byte[] data, int offset, int length, short nPoly) {
        short crc = (short) 0xFFFF;
        for (int j = 0; j < length; j++) {
            byte b = data[offset + j];
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= nPoly;
            }
        }
        crc &= 0xffff;
        return crc;
    }

    public static String dump(byte[] b, int offset, int size) {
        String s = "";
        if (b == null)
            return s;
        for (int i = 0; i < size; i++) {
            if (offset + i >= b.length)
                return s;
            s += String.format("%02X", b[offset + i]);
        }
        return s;
    }

    /**
     * Поверить ИНН на валидность
     *
     * @param inn ИНН для проверки
     * @return
     */
    public static boolean checkINN(String inn) {
        if (inn.length() != 10 && inn.length() != 12) return false;
        int[] d = new int[inn.length()];
        try {
            for (int i = 0; i < inn.length(); i++)
                d[i] = Integer.parseInt("" + inn.charAt(i));
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "exception", nfe);
            return false;
        }

        if (d.length == 10) {
            int sum = d[0] * 2 + d[1] * 4 + d[2] * 10 + d[3] * 3 + d[4] * 5 + d[5] * 9 + d[6] * 4 + d[7] * 6 + d[8] * 8;
            sum = (sum % 11) % 10;
            return sum == d[9];
        } else {
            int sum = d[0] * 7 + d[1] * 2 + d[2] * 4 + d[3] * 10 + d[4] * 3 + d[5] * 5 + d[6] * 9 + d[7] * 4 + d[8] * 6 + d[9] * 8;
            sum = (sum % 11) % 10;
            if (sum != d[10]) return false;
            sum = d[0] * 3 + d[1] * 7 + d[2] * 2 + d[3] * 4 + d[4] * 10 + d[5] * 3 + d[6] * 5 + d[7] * 9 + d[8] * 4 + d[9] * 6 + d[10] * 8;
            sum = (sum % 11) % 10;
            return sum == d[11];
        }

    }

    /**
     * Проверить валидность регистрационного номера ККТ
     *
     * @param number номер
     * @param inn    ИНН пользователя
     * @param device серийный номер ККТ
     */
    public static boolean checkRegNo(String number, String inn, String device) {
        if (number == null || number.length() != 16) return false;
        while (inn.length() < 12) inn = "0" + inn;
        while (device.length() < 20) device = "0" + device;
        String num = number.substring(0, 10) + inn + device;
        String crc = number.substring(10);
        byte b[] = num.getBytes(Const.ENCODING);
        int sCRC = (CRC16(b, 0, b.length, (short) 0x1021) & 0xFFFF);
        try {
            if (Integer.parseInt(crc) == sCRC)
                return true;
            return false;
        } catch (NumberFormatException nfe) {
            Log.e(TAG, "exception", nfe);
            return false;
        }
    }

    /**
     * Сформатировать дату в виде DD/MM/YYYY, HH:mm
     *
     * @param d
     * @return
     */
    public static String formatDate(long d) {
        return DATE_FORMAT.format(new Date(d));
    }

    public static String formatDate(Date d) {
        return DATE_FORMAT.format(d);
    }

    public static String formatDate(Calendar cal) {
        return DATE_FORMAT.format(cal.getTime());
    }

    public static String formatDateS(long d) {
        return DATE_FORMAT_S.format(new Date(d));
    }

    /**
     * Созать баркод как картинку
     *
     * @param contents
     * @param img_width
     * @param img_height
     * @param format
     * @return
     */
    public static Bitmap encodeAsBitmap(String contents, int img_width, int img_height, BarcodeFormat format, int rotation) {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return Bitmap.createBitmap(img_width, img_height, Config.ARGB_8888);
        }
        Hashtable<EncodeHintType, Object> hints = null;
        hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        int w = img_width, h = img_height;
        if (rotation == 90 || rotation == 270 || rotation == -90 || rotation == -270) {
            w = img_height;
            h = img_width;
        }

        try {
            result = writer.encode(contentsToEncode, format, w, h, hints);
        } catch (Exception iae) {
            return Bitmap.createBitmap(img_width, img_height, Config.ARGB_8888);
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        int po = 0;
        switch (rotation) {
            case 0:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++)
                        pixels[po++] = result.get(x, y) ? Color.BLACK : Color.WHITE;
                }
                break;
            case 90:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        pixels[po++] = result.get(x, y) ? Color.BLACK : Color.WHITE;
                    }
                }
                break;
        }

        Bitmap bitmap = Bitmap.createBitmap(img_width, img_height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, img_width, 0, 0, img_width, img_height);
        return bitmap;
    }

    public static Document deserializeDocument(byte[] data) {
        Parcel p = Parcel.obtain();
        p.unmarshall(data, 0, data.length);
        p.setDataPosition(0);
        String className = p.readString();
        try {
            Class<?> c = Class.forName(className);
            if (Document.class.isAssignableFrom(c)) {
                Field cField = c.getDeclaredField("CREATOR");
                @SuppressWarnings("rawtypes")
                Parcelable.Creator creator = (Parcelable.Creator) cField.get(null);
                return (Document) creator.createFromParcel(p);
            }
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
        return null;
    }

    public static byte[] hex2bytes(String hex) {
        if (hex == null) return new byte[]{};
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            String s = hex.substring(i * 2, i * 2 + 2);
            result[i] = (byte) (Integer.parseInt(s, 16) & 0xFF);
        }
        return result;
    }

    public static boolean startService(Context context){
        ComponentName cn = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cn = context.startForegroundService(Const.FISCAL_STORAGE);
        } else {
            cn = context.startService(Const.FISCAL_STORAGE);
        }

        if (cn == null){
            Log.e(TAG, "can't start FNCore service");
            return false;
        }

        return true;
    }

}
