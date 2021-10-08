package rs.sample.fncore2.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import rs.fncore.Const;
import rs.fncore.Errors;
import rs.fncore.FiscalStorage;
import rs.fncore.data.Correction;
import rs.fncore.data.FiscalReport;
import rs.fncore.data.KKMInfo;
import rs.fncore.data.MarkingCode;
import rs.fncore.data.MeasureTypeE;
import rs.fncore.data.OU;
import rs.fncore.data.Payment;
import rs.fncore.data.Payment.PaymentTypeE;
import rs.fncore.data.SellItem;
import rs.fncore.data.SellOrder;
import rs.fncore.data.SellOrder.OrderTypeE;
import rs.fncore.data.Shift;
import rs.fncore.data.VatE;
import rs.fncore.data.TaxModeE;
import rs.fncore.data.XReport;
import rs.sample.fncore2.Core;
import rs.sample.fncore2.R;
import rs.utils.app.AppCore;
import rs.utils.app.AppCore.FNOperaionTask;
import rs.utils.app.MessageQueue.MessageHandler;

import static rs.fncore.Errors.MARKING_CHECK_FAILED;

public class Main extends Activity implements MessageHandler, View.OnClickListener {
    private final String TAG=this.getClass().getSimpleName();
    /**
     * Информация о ККМ
     */
    private final KKMInfo mKkmInfo = new KKMInfo();
    private TextView mKkmSerial, mFnSerial, mKkmNumber, mShiftState;
    private View mViewLock;
    private final OU mCasier = new OU("Петров И.Н."); // Кассир по умолчанию

    private final BroadcastReceiver FNChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateKKMInfo();
        }
    };

    public Main() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mKkmNumber = findViewById(R.id.kkm_number);
        mKkmSerial = findViewById(R.id.kkm_serial_number);
        mFnSerial = findViewById(R.id.fn_serial_number);
        mShiftState = findViewById(R.id.shift_state);
        mViewLock = findViewById(R.id.locker);
        mViewLock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });

        findViewById(R.id.shift_action).setOnClickListener(this);
        findViewById(R.id.sell_action).setOnClickListener(this);
        findViewById(R.id.sell_action_mark).setOnClickListener(this);
        findViewById(R.id.correct_action).setOnClickListener(this);
        findViewById(R.id.correct_action_mark).setOnClickListener(this);
        findViewById(R.id.rep_action).setOnClickListener(this);
        findViewById(R.id.xrep_action).setOnClickListener(this);
        findViewById(R.id.lbl_get_sno).setOnClickListener(this);
        findViewById(R.id.correct_action_mark).setOnClickListener(this);

        ((ListView) findViewById(R.id.log_view)).setAdapter(Core.getInstance().getLogger());

        registerReceiver(FNChangedReceiver, new IntentFilter(Const.FN_STATE_CHANGED_ACTION));

        // Регистрируем обработчик локальных сообщений
        Core.getInstance().registerHandler(this);
        Core.getInstance().addLogRecord("Инициализация ядра..");
        // Подключаемся к фискальному ядру, это имеет смысл делать по окончании инициализации
        if (Core.getInstance().initialize()) {
            Core.getInstance().addLogRecord("Инициализация, ожидание ответа от FNCore");
        }
        else {
            Core.getInstance().addLogRecord("Ошибка инициализации ядра");
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(FNChangedReceiver);
        Core.getInstance().removeHandler(this);
        // Отключаемся от Фискального ядра
        Core.getInstance().deinitialize();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateKKMInfo();
    }

    @Override
    public boolean onMessage(Message msg) {
        if (msg.what == AppCore.MSG_FISCAL_STORAGE_READY) {
            int result = ((Number) msg.obj).intValue();
            Core.getInstance().addLogRecord(String.format("Инициализация завершена, код %02x", result));
            if (result == Errors.NO_ERROR) {
                updateKKMInfo();
            }
            return true;
        }
        return false;
    }

    /**
     * Обновление информации о ККМ
     */
    private void updateKKMInfo() {
        KKMInfoReader reader = new KKMInfoReader();
        Core.getInstance().newTask(this, reader, reader).execute();
    }

    abstract static class RunnableResult<T> extends AppCore.AsyncResult<T> implements Runnable {
    }

    int checkMarkingItem(SellItem item) throws RemoteException, InterruptedException, ExecutionException {
        if (item.getMarkingCode().isEmpty()) return Errors.NO_ERROR;

        int res=Core.getInstance().newTask(this, new AppCore.ProcessTask() {
            @Override
            public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                task.showProgress("Операция выполняется...");
                try {
                    return storage.checkMarkingItem(item);
                } catch (RemoteException re) {
                    return Errors.SYSTEM_ERROR;
                }
            }
        }, result -> {
            if (result != Errors.NO_ERROR && result != MARKING_CHECK_FAILED) {
                showToast(String.format("Операция выполнена с ошибкой %02X", result));
            }
        }).execute().get();

        if (res == Errors.NO_ERROR) return res;
        if (res != MARKING_CHECK_FAILED) return res;

        RunnableResult<Boolean> run = new RunnableResult<Boolean>() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
                AlertDialog markingDialog = builder.setTitle("Внимание!")
                        .setMessage("Товар " + item.getName() + " не прошел проверку " +
                                "маркировки, включить его в чек ?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            setResult(true);
                            dialog.dismiss();
                        })
                        .setNegativeButton("Нет", (dialog, which) -> {
                            setResult(false);
                            dialog.dismiss();
                        }).create();
                markingDialog.show();
            }
        };
        runOnUiThread(run);

        boolean result = run.get();

        return Core.getInstance().confirmFailedMarkingItem(item, result);
    }

    void ProcessOnclick(int viewId) {
        try {
            switch (viewId) {
                case R.id.lbl_get_sno:
                    runOnUiThread(() -> {
                        ArrayAdapter<String> modes = new ArrayAdapter<>(Main.this, android.R.layout.simple_list_item_1);
                        for (TaxModeE m : mKkmInfo.getTaxModes())
                            modes.add(m.desc);
                        AlertDialog.Builder b = new AlertDialog.Builder(Main.this);
                        b.setAdapter(modes, null);
                        b.setNegativeButton(android.R.string.cancel, null);
                        b.show();
                    });
                    break;
                case R.id.shift_action:
                    if (!mKkmInfo.isFNActive())
                        showToast("В данном режиме ФН операция невозможна");
                    else {
                        Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                            @Override
                            public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                                task.showProgress("Операция выполняется...");
                                try {
                                    int result = storage.toggleShift(mCasier, new Shift(), null);
                                    if (result == Errors.NO_ERROR) // Обновляем информацию о ККМ
                                        storage.readKKMInfo(mKkmInfo);
                                    return result;
                                } catch (RemoteException re) {
                                    return Errors.SYSTEM_ERROR;
                                }
                            }
                        }, result -> {
                            if (result == Errors.NO_ERROR) {
                                if (mKkmInfo.getShift().isOpen())
                                    showToast(String.format("Смена %d успешно открыта", mKkmInfo.getShift().getNumber()));
                                else
                                    showToast(String.format("Смена %d успешно закрыта", mKkmInfo.getShift().getNumber()));
                                new KKMInfoReader().onResult(result); // Отображаем новую информацию о смене
                            } else {
                                showToast(String.format("Операция выполнена с ошибкой %02X", result));
                            }
                        }).execute();
                    }
                    break;
                case R.id.rep_action:
                    if (!mKkmInfo.isFNActive())
                        showToast("В данном режиме ФН операция невозможна");
                    else {
                        Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                            @Override
                            public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                                task.showProgress("Операция выполняется...");
                                try {
                                    return storage.requestFiscalReport(mCasier, new FiscalReport(), "Hello!\n@include base\nHello2!");
                                } catch (RemoteException re) {
                                    return Errors.SYSTEM_ERROR;
                                }
                            }
                        }, result -> {
                            if (result == Errors.NO_ERROR)
                                showToast("Операция выполнена успешно");
                            else
                                showToast(String.format("Операция выполнена с ошибкой %02X", result));
                        }).execute();
                    }
                    break;
                case R.id.sell_action: {
                    if (!mKkmInfo.isFNActive()) {
                        showToast("В данном режиме ФН операция невозможна");
                        break;
                    }
                    if (!mKkmInfo.getShift().isOpen()) {
                        showToast("Не открыта рабочая смена");
                        break;
                    }
                    SellOrder order = new SellOrder(OrderTypeE.Income, TaxModeE.Common); // Тип чека
                    // приход, СНО - Основная
                    order.getLocation().setPlace("Магазин");
                    order.getLocation().setAddress("г. Москва, Красная площадь, 1");
                    //order.addItem(new SellItem("Тест 1", 2.000, 938.66, VAT.vat_20));
                    //order.addItem(new SellItem("Тест 2", 2.000, 957.25, VAT.vat_20));

                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес 1кг", new BigDecimal("0.774"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        order.addItem(item);

                        item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес 1кг", new BigDecimal("1.03"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        order.addItem(item);
                    }
                    order.setRecipientAddress("test@rs.com");
                    order.addPayment(new Payment(PaymentTypeE.Credit, new BigDecimal("31.58")));

                    // Добавление наличного платежа
                    Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                        @Override
                        public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                            task.showProgress("Проведение чека...");
                            SellOrder order = (SellOrder) args[0];
                            try {
                                return storage.doSellOrder(order, mCasier, order, true,
                                        null,
                                        "{\\tr\\{\\td width:100%;style:bold;\\$item.name$}}" +
                                                "{\\tr\\" +
                                                " {\\td width:80%;\\$item.qtty$ $item.measure$ x $item.price$}" +
                                                " {\\td width:*;align:right;\\=  $item.sum$}" +
                                                "}" +
                                                "{\\tr\\" +
                                                " {\\td width:60%;\\!!!$item.Vat.Name$!!!}" +
                                                " {\\td width:*;align:right;if:$item.Vat.Value$!=$item.sum$;\\=  $item.Vat.Value$}" +
                                                "}", null, null);
                            } catch (RemoteException re) {
                                return Errors.SYSTEM_ERROR;
                            }
                        }
                    }, result -> {
                        if (result == Errors.NO_ERROR)
                            showToast("Операция выполнена успешно");
                        else
                            showToast(String.format("Операция выполнена с ошибкой %02X", result));
                    }).execute(order);
                }
                break;
                case R.id.sell_action_mark: {
                    if (!mKkmInfo.isFNActive() || !mKkmInfo.isMarkingGoods()) {
                        showToast("В данном режиме ФН операция невозможна");
                        break;
                    }
                    if (!mKkmInfo.getShift().isOpen()) {
                        showToast("Не открыта рабочая смена");
                        break;
                    }

                    BigDecimal paymentSum = BigDecimal.ZERO;
                    SellOrder order = new SellOrder(OrderTypeE.Income, TaxModeE.Common); // Тип чека
                    // приход, СНО - Основная
                    order.getLocation().setPlace("Магазин");
                    order.getLocation().setAddress("г. Москва, Красная площадь, 1");
                    order.addItem(new SellItem("Тест 1", new BigDecimal(2.000),
                            new BigDecimal("938.66"), VatE.vat_20));
                    paymentSum=paymentSum.add(new BigDecimal("1877.32"));
                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.MarkGoodsMark,
                                SellItem.ItemPaymentTypeE.Full,
                                "Табак весовой 1кг", new BigDecimal("0.774"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_20);
                        final String div = new String(new byte[]{(byte) 0x1d});
                        item.setClientINN("760762606926");
                        String codeStr = "010464007801637221AgqLybqxM9MbR" + div + "91FFD0" + div + "92dGVzdL31KAYL0YT6592MjmW7a2HkF3IY+muf2pVSKdQ=";
                        item.setMarkingCode(codeStr, order.getType());

                        int res = checkMarkingItem(item);
                        if (res == Errors.NO_ERROR) {
                            order.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                    }

                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.MarkGoodsMark,
                                SellItem.ItemPaymentTypeE.Full,
                                "Кроссовки Nike", BigDecimal.ONE, MeasureTypeE.Piece,
                                new BigDecimal("27.50"), VatE.vat_20);
                        String div = new String(new byte[]{(byte) 0x1d});
                        item.setClientINN("760762606926");
                        String codeStr = "010464007801637221F8YTliqZrE*N?" + div + "91FFD0" + div + "92dGVzdF06ABaSTwxlENovW1AVeduzPT8M0wMAdmbpue0=";
                        item.setMarkingCode(codeStr, order.getType());
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            order.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                    }

                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес 1кг", new BigDecimal("1.03"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            order.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                    }
                    order.setRecipientAddress("test@rs.com");
                    order.addPayment(new Payment(PaymentTypeE.Card, paymentSum));

                    // Добавление наличного платежа
                    Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                        @Override
                        public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                            task.showProgress("Проведение чека...");
                            SellOrder order = (SellOrder) args[0];
                            try {
                                return storage.doSellOrder(order, mCasier, order, true,
                                        null,
                                        "{\\tr\\{\\td width:100%;style:bold;\\$item.MarkCode$ " +
                                                "$item.name$}}" +
                                                "{\\tr\\" +
                                                " {\\td width:80%;\\$item.qtty$ $item.measure$ x $item.price$}" +
                                                " {\\td width:*;align:right;\\=  $item.sum$}" +
                                                "}" +
                                                "{\\tr\\" +
                                                " {\\td width:60%;\\!!!$item.Vat.Name$!!!}" +
                                                " {\\td width:*;align:right;if:$item.Vat.Value$!=$item.sum$;\\=  $item.Vat.Value$}" +
                                                "}", null, null);
                            } catch (RemoteException re) {
                                return Errors.SYSTEM_ERROR;
                            }
                        }
                    }, result -> {
                        if (result == Errors.NO_ERROR)
                            showToast("Операция выполнена успешно");
                        else
                            showToast(String.format("Операция выполнена с ошибкой %02X", result));
                    }).execute(order);
                }
                break;
                case R.id.correct_action: {
                    if (!mKkmInfo.isFNActive()) {
                        showToast("В данном режиме ФН операция невозможна");
                        break;
                    }
                    if (!mKkmInfo.getShift().isOpen()) {
                        showToast("Не открыта рабочая смена");
                        break;
                    }
                    Correction cor = new Correction(Correction.CorrectionTypeE.byArbitarity, OrderTypeE.Outcome,
                            TaxModeE.Common);

                    cor.setBaseDocumentDate(System.currentTimeMillis());
                    cor.setBaseDocumentNumber("Возврат чека 11");
                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес", new BigDecimal("0.5"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            cor.addItem(item);
                        }
                        item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес", new BigDecimal("1.00"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            cor.addItem(item);
                        }
                    }
                    //cor.addPayment(new Payment(new BigDecimal("26.25")));
                    cor.addPayment(new Payment(PaymentTypeE.Ahead, new BigDecimal("26.25")));
                    Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                        @Override
                        public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                            Correction cor = (Correction) args[0];
                            task.showProgress("Операция выполняется...");
                            try {
                                return storage.doCorrection2(cor, mCasier, cor, null,null,null,null);
                            } catch (RemoteException re) {
                                return Errors.SYSTEM_ERROR;
                            }
                        }
                    }, result -> {
                        if (result == Errors.NO_ERROR)
                            showToast("Операция выполнена успешно");
                        else
                            showToast(String.format("Операция выполнена с ошибкой %02X", result));
                    }).execute(cor);
                }
                break;
                case R.id.correct_action_mark: {
                    if (!mKkmInfo.isFNActive() || !mKkmInfo.isMarkingGoods()) {
                        showToast("В данном режиме ФН операция невозможна");
                        break;
                    }
                    if (!mKkmInfo.getShift().isOpen()) {
                        showToast("Не открыта рабочая смена");
                        break;
                    }
                    BigDecimal paymentSum = BigDecimal.ZERO;
                    Correction cor = new Correction(Correction.CorrectionTypeE.byArbitarity, OrderTypeE.Outcome,
                            TaxModeE.Common);
                    cor.setBaseDocumentDate(System.currentTimeMillis());
                    cor.setBaseDocumentNumber("Возврат чека 11");
                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.MarkGoodsMark,
                                SellItem.ItemPaymentTypeE.Full,
                                "Табак весовой 1кг", new BigDecimal("0.774"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_20);
                        String div = new String(new byte[]{(byte) 0x1d});
                        item.setClientINN("760762606926");
                        String codeStr = "010464007801637221AgqLybqxM9MbR" + div + "91FFD0" + div + "92dGVzdL31KAYL0YT6592MjmW7a2HkF3IY+muf2pVSKdQ=";
                        MarkingCode code = new MarkingCode(codeStr, MarkingCode.PlannedItemStatusE.MeasuredItemSell);
                        item.setMarkingCode(code);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            cor.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                    }
                    {
                        SellItem item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес", new BigDecimal("0.5"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            cor.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                        item = new SellItem(SellItem.SellItemTypeE.Good,
                                SellItem.ItemPaymentTypeE.Full,
                                "Картофель красный вес", new BigDecimal("1.00"), MeasureTypeE.Kilogram,
                                new BigDecimal("17.50"), VatE.vat_10);
                        if (checkMarkingItem(item) == Errors.NO_ERROR) {
                            cor.addItem(item);
                            paymentSum=paymentSum.add(item.getSum());
                        }
                    }
                    cor.addPayment(new Payment(paymentSum));

                    Core.getInstance().newTask(this, new AppCore.ProcessTask() {
                        @Override
                        public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
                            Correction cor = (Correction) args[0];
                            task.showProgress("Операция выполняется...");
                            try {
                                return storage.doCorrection2(cor, mCasier, cor, null,null,null,null);
                            } catch (RemoteException re) {
                                return Errors.SYSTEM_ERROR;
                            }
                        }
                    }, result -> {
                        if (result == Errors.NO_ERROR)
                            showToast("Операция выполнена успешно");
                        else
                            showToast(String.format("Операция выполнена с ошибкой %02X", result));
                    }).execute(cor);
                }
                break;
                case R.id.xrep_action:
                    if (!mKkmInfo.isFNActive())
                        showToast("В данном режиме ФН операция невозможна");
                    else {
                        Core.getInstance().newTask(this, (storage, task, args) -> {
                            task.showProgress("Операция выполняется...");
                            try {
                                return storage.doXReport(new XReport(), true, null);
                            } catch (RemoteException re) {
                                return Errors.SYSTEM_ERROR;
                            }
                        }, result -> {
                            if (result == Errors.NO_ERROR)
                                showToast("Операция выполнена успешно");
                            else
                                showToast(String.format("Операция выполнена с ошибкой %02X", result));
                        }).execute();
                    }
                    break;
                default:
                    showToast("Не поддерживается, смотри МКАССА");
                    break;
            }
        } catch (Exception e) {
            showToast("Ошибка " + e);
            Log.e(TAG, "exception: ", e);
        }
    }

    private void showToast(String data){
        runOnUiThread(() -> {
            Toast.makeText(this, data, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onClick(View v) {
        Thread clickThread = new Thread(() -> ProcessOnclick(v.getId()));
        clickThread.start();
    }

    private class KKMInfoReader implements AppCore.ProcessTask, AppCore.ResultTask {

        @Override
        public void onResult(int result) {
            if (result == Errors.NO_ERROR) {
                mKkmSerial.setText(mKkmInfo.getKKMSerial());
                mShiftState.setText("не доступно");
                if (mKkmInfo.isFNPresent())
                    mFnSerial.setText(mKkmInfo.getFNNumber());
                else {
                    mFnSerial.setText("не установлен");
                }
                if (mKkmInfo.isFNActive() || mKkmInfo.isFNArchived()) {
                    mKkmNumber.setText(mKkmInfo.getKKMNumber());
                    if (mKkmInfo.getShift().isOpen())
                        mShiftState.setText("открыта, № " + mKkmInfo.getShift().getNumber());
                    else
                        mShiftState.setText("закрыта");
                } else
                    mKkmNumber.setText("не фискализирован");
                mViewLock.setVisibility(View.GONE);
            } else {
                mKkmSerial.setText("не доступно");
                mFnSerial.setText("не доступно");
                mKkmNumber.setText("не доступно");
                mShiftState.setText("не доступно");
                Core.getInstance().addLogRecord(String.format("Ошибка чтения информации %02X", result));
                mViewLock.setVisibility(View.VISIBLE);
            }
        }

        // Чтение информации о ККМ
        @Override
        public int execute(FiscalStorage storage, FNOperaionTask task, Object... args) {
            try {
                return storage.readKKMInfo(mKkmInfo);
            } catch (RemoteException re) {
                return Errors.SYSTEM_ERROR;
            }
        }

    }

}
