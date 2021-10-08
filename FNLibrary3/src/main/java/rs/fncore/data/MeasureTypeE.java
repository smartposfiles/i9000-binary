package rs.fncore.data;

import java.security.InvalidParameterException;

/**
 * Типы агентских услуг
 *
 * @author amv
 */
public enum MeasureTypeE {
    /**
     * шт. или ед.
     * Применяется для предметов расчета, которые могут быть реализованы поштучно или единицами
     */
    Piece(0,"шт."),
    /**
     * Грамм
     */
    Gram(10, "г"),
    /**
     * Килограмм
     */
    Kilogram(11,"кг"),
    /**
     * Тонна
     */
    Ton(12,"т"),
    /**
     * Сантиметр
     */
    Centimeter(20,"см"),
    /**
     * Дециметр
     */
    Decimeter(21,"дм"),
    /**
     * Метр
     */
    Meter(22,"м"),
    /**
     * Квадратный сантиметр
     */
    Square_centimeter(30,"кв. см"),
    /**
     * Квадратный дециметр
     */
    Square_decimeter(31,"кв. дм"),
    /**
     * Квадратный метр
     */
    Square_meter(32,"кв. м"),
    /**
     * Миллилитр
     */
    Milliliter(40,"мл"),
    /**
     * Литр
     */
    Liter(41,"л"),
    /**
     * Кубический метр
     */
    Cubic_meter(42,"куб. м"),
    /**
     * Киловатт час
     */
    Kilowatt_hour(50,"кВт ч"),
    /**
     * Гигакалория
     */
    Gigacalorie(51,"Гкал"),
    /**
     * Сутки (день)
     */
    Day(70,"сутки"),
    /**
     * Час
     */
    Hour(71,"час"),
    /**
     * Минута
     */
    Minute(72,"мин"),
    /**
     * Секунда
     */
    Second(73,"с"),
    /**
     * Килобайт
     */
    Kilobyte(80,"Кбайт"),
    /**
     * Мегабайт
     */
    Megabyte(81,"Мбайт"),
    /**
     * Гигабайт
     */
    Gigabyte(82,"Гбайт"),
    /**
     * Терабайт
     */
    Terabyte(83,"Тбайт"),
    /**
     * Применяется при использовании иных единиц измерения, не поименованных в п.п. 1 - 23
     */
    Other(255,"-")
    ;

    public final byte bVal;
    public final String pName;

    private MeasureTypeE(int value, String name) {
        this.bVal = (byte)value;
        this.pName = name;
    }

    public static MeasureTypeE fromByte(byte number){
        for (MeasureTypeE val:values()){
            if (val.bVal == number){
                return val;
            }
        }
        throw new InvalidParameterException("unknown value");
    }

    /**
     * Получить список наименований
     * @return
     */
    public static String [] getNames(){
        String [] res = new String[values().length];
        for (MeasureTypeE val:values()){
            res[val.ordinal()]=val.pName;
        }
        return res;
    }
}
