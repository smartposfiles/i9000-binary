package com.example.fn.ui;

import java.util.Random;

import com.example.fn.Core;
import com.example.fncoresample.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import rs.fncore.Const.Errors;
import rs.fncore.FiscalStorage;
import rs.fncore.data.Correction;
import rs.fncore.data.Correction.CorrectionType;
import rs.fncore.data.FiscalReport;
import rs.fncore.data.KKMInfo;
import rs.fncore.data.OU;
import rs.fncore.data.Payment;
import rs.fncore.data.Payment.PaymentType;
import rs.fncore.data.SellItem;
import rs.fncore.data.SellItem.ItemPaymentType;
import rs.fncore.data.SellItem.SellItemType;
import rs.fncore.data.SellItem.VAT;
import rs.fncore.data.SellOrder;
import rs.fncore.data.Shift;
import rs.fncore.data.TaxMode;
import rs.fncore.data.XReport;
import rs.fncore.data.SellOrder.OrderType;
import rs.utils.app.AppCore;
import rs.utils.app.AppCore.FNOperaionTask;
import rs.utils.app.MessageQueue.MessageHandler;

public class Main extends Activity  implements MessageHandler, View.OnClickListener {

	/**
	 * Информация о ККМ
	 */
	private KKMInfo _kkmInfo = new KKMInfo();
	private TextView _kkm_serial, _fn_serial,_kkm_number,_shift_state;
	private View _lock;
	private OU casier = new OU("Иванов И.Н."); // Кассир по умолчанию
	private class KKMInfoReader implements AppCore.ProcessTask, AppCore.ResultTask {

		@Override
		public void onResult(int result) {
			if(result == Errors.NO_ERROR) {
				_kkm_serial.setText(_kkmInfo.getKKMSerial());
				_shift_state.setText("не доступно");
				if(_kkmInfo.isFNPresent()) 
					_fn_serial.setText(_kkmInfo.getFNNumber());
				else  {
					_fn_serial.setText("не установлен");
				}
				if(_kkmInfo.isFNActive() || _kkmInfo.isFNArchived()) {
					_kkm_number.setText(_kkmInfo.getKKMNumber());
					if(_kkmInfo.getShift().isOpen())
						_shift_state.setText("открыта, № "+_kkmInfo.getShift().getNumber());
					else 
						_shift_state.setText("закрыта");
				} else 
					_kkm_number.setText("не фискализирован");
				_lock.setVisibility(View.GONE);
			} else {
				_kkm_serial.setText("не доступно");
				_fn_serial.setText("не доступно");
				_kkm_number.setText("не доступно");
				_shift_state.setText("не доступно");
				Core.getInstance().addLogRecord(String.format("Ошибка чтения информации %02X",result));
				_lock.setVisibility(View.VISIBLE);
			}
		}

		// Чтение информации о ККМ
		@Override
		public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
			try {
				return storage.readKKMInfo(_kkmInfo);
			} catch(RemoteException re) {
				return Errors.SYSTEM_ERROR;
			}
		}
		
	}
	
	public Main() {
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		_kkm_number = findViewById(R.id.kkm_number);
		_kkm_serial = findViewById(R.id.kkm_serial_number);
		_fn_serial = findViewById(R.id.fn_serial_number);
		_shift_state = findViewById(R.id.shift_state);
		_lock = findViewById(R.id.locker);
		_lock.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				return true;
			}
		});
		findViewById(R.id.fiscal_action).setOnClickListener(this);
		findViewById(R.id.shift_action).setOnClickListener(this);
		findViewById(R.id.sell_action).setOnClickListener(this);
		findViewById(R.id.correct_action).setOnClickListener(this);
		findViewById(R.id.rep_action).setOnClickListener(this);
		findViewById(R.id.xrep_action).setOnClickListener(this);
		
		
		((ListView)findViewById(R.id.log_view)).setAdapter(Core.getInstance().getLogger());
		// Регистрируем обработчик локальных сообщений
		Core.getInstance().registerHandler(this);
		Core.getInstance().addLogRecord("Инициализация ядра..");
		// Подключаемся к фискальному ядру, это имеет смысл делать по окончании инициализации
		switch(Core.getInstance().initialize()) {
		case 1:
			break;
		case 2:
			Core.getInstance().addLogRecord(String.format("Инициализация завершена"));
			updateKKMInfo();
			break;
		}
	}
	@Override
	protected void onDestroy() {
		Core.getInstance().removeHandler(this);
		// Отключаемся от Фискального ядра
		Core.getInstance().deinitialize();
		super.onDestroy();
	}
	@Override
	public boolean onMessage(Message msg) {
		if(msg.what == AppCore.MSG_FISCAL_STORAGE_READY) {
			int result = ((Number)msg.obj).intValue();
			Core.getInstance().addLogRecord(String.format("Инициализация завершена, код %02x",result));
			if(result == Errors.NO_ERROR)
				updateKKMInfo();
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
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.shift_action:
			if(!_kkmInfo.isFNActive())
				Toast.makeText(this, "В данном режиме ФН операция невозможна", Toast.LENGTH_LONG).show();
			else {
				Core.getInstance().newTask(this, new AppCore.ProcessTask() {
					@Override
					public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
						task.showProgress("Операция выполняется...");
						try {
							int result = storage.toggleShift(casier, new Shift(), null);
							if(result == Errors.NO_ERROR) // Обновляем информацию о ККМ
								storage.readKKMInfo(_kkmInfo);
							return result;
						} catch(RemoteException re) {
							return Errors.SYSTEM_ERROR;
						}
					}
				}, new AppCore.ResultTask() {
					@Override
					public void onResult(int result) {
						if(result == Errors.NO_ERROR) {
							if(_kkmInfo.getShift().isOpen())
								Toast.makeText(Main.this, String.format("Смена %d успешно открыта", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
							else 
								Toast.makeText(Main.this, String.format("Смена %d успешно закрыта", _kkmInfo.getShift().getNumber()), Toast.LENGTH_SHORT).show();
							new KKMInfoReader().onResult(result); // Отображаем новую информацию о смене
						} else 
							Toast.makeText(Main.this, String.format("Операция выполнена с ошибкой %02X",result), Toast.LENGTH_LONG).show();
					}
				}).execute();
			}
			break;
		case R.id.rep_action:
			if(!_kkmInfo.isFNActive())
				Toast.makeText(this, "В данном режиме ФН операция невозможна", Toast.LENGTH_LONG).show();
			else {
				Core.getInstance().newTask(this, new AppCore.ProcessTask() {
					@Override
					public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
						task.showProgress("Операция выполняется...");
						try {
							return storage.requestFiscalReport(casier, new FiscalReport(), "Hello!\n@include base\nHello2!");
						} catch(RemoteException re) {
							return Errors.SYSTEM_ERROR;
						}
					}
				}, new AppCore.ResultTask() {
					@Override
					public void onResult(int result) {
						if(result == Errors.NO_ERROR)
							Toast.makeText(Main.this, "Операция выполнена успешно", Toast.LENGTH_SHORT).show();
						else 
							Toast.makeText(Main.this, String.format("Операция выполнена с ошибкой %02X",result), Toast.LENGTH_LONG).show();
					}
				}).execute();
			}
			break;
		case R.id.sell_action:
			if(!_kkmInfo.isFNActive()) {
				Toast.makeText(this, "В данном режиме ФН операция невозможна", Toast.LENGTH_LONG).show();
				break;
			}
			if(!_kkmInfo.getShift().isOpen()) {
				Toast.makeText(this, "Не открыта рабочая смена", Toast.LENGTH_LONG).show();
				break;
			}
			SellOrder order = new SellOrder(OrderType.Income, TaxMode.Common); // Тип чека приход, СНО - Основная
			double s = 0;
			Random r = new Random(System.currentTimeMillis());
			for(int i=0;i<5;i++) {
				double p = 200.16;
				s += p;
				SellItem item = new SellItem(SellItemType.Good,ItemPaymentType.Full,"Товар по свободной цене",1f,"шт.",p,i % 2 == 0 ?VAT.vat_10_110 :VAT.vat_20); // Товар с агентскими данными
				order.addItem(item);
			}
				
			order.setRecipientAddress("nik@home");
			
			order.addPayment(new Payment(PaymentType.Cash,s)); // Добавление наличного платежа
			
			Core.getInstance().newTask(this, new AppCore.ProcessTask() {
				@Override
				public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
					task.showProgress("Проведение чека...");
					SellOrder order = (SellOrder)args[0];
					try {
						return storage.doSellOrder(order,casier,order,true,
								null,
								"{\\tr\\{\\td width:100%;style:bold;\\$item.name$}}" +
							            "{\\tr\\" +
							            " {\\td width:80%;\\$item.qtty$ $item.measure$ x $item.price$}" +
							            " {\\td width:*;align:right;\\=  $item.sum$}" +
							            "}" +
							            "{\\tr\\" +
							            " {\\td width:60%;\\!!!$item.Vat.Name$!!!}" +
							            " {\\td width:*;align:right;if:$item.Vat.Value$!=$item.sum$;\\=  $item.Vat.Value$}" +
							            "}",null,null);
					} catch(RemoteException re) {
						return Errors.SYSTEM_ERROR;
					}
				}
			},new AppCore.ResultTask() {
				@Override
				public void onResult(int result) {
					if(result == Errors.NO_ERROR)
						Toast.makeText(Main.this, "Операция выполнена успешно", Toast.LENGTH_SHORT).show();
					else 
						Toast.makeText(Main.this, String.format("Операция выполнена с ошибкой %02X",result), Toast.LENGTH_LONG).show();
				}
			}).execute(order);
			break;
		case R.id.correct_action:
			if(!_kkmInfo.isFNActive()) {
				Toast.makeText(this, "В данном режиме ФН операция невозможна", Toast.LENGTH_LONG).show();
				break;
			}
			if(!_kkmInfo.getShift().isOpen()) {
				Toast.makeText(this, "Не открыта рабочая смена", Toast.LENGTH_LONG).show();
				break;
			}
			Correction cor = new Correction(CorrectionType.byArbitarity, OrderType.Outcome, 4000, VAT.vat_20, TaxMode.Common);
			cor.setBaseDocumentDate(System.currentTimeMillis());
			cor.setBaseDocumentNumber("Возврат чека 11");
			cor.addPayment(new Payment(4000));
			Core.getInstance().newTask(this, new AppCore.ProcessTask() {
				@Override
				public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
					Correction cor = (Correction)args[0];
					task.showProgress("Операция выполняется...");
					try {
						return storage.doCorrection(cor, casier, cor, null); 
					} catch(RemoteException re) {
						return Errors.SYSTEM_ERROR;
					}
				}
			}, new AppCore.ResultTask() {
				@Override
				public void onResult(int result) {
					if(result == Errors.NO_ERROR)
						Toast.makeText(Main.this, "Операция выполнена успешно", Toast.LENGTH_SHORT).show();
					else 
						Toast.makeText(Main.this, String.format("Операция выполнена с ошибкой %02X",result), Toast.LENGTH_LONG).show();
				}
			}).execute(cor);
			
			break;
		case R.id.xrep_action:
			if(!_kkmInfo.isFNActive())
				Toast.makeText(this, "В данном режиме ФН операция невозможна", Toast.LENGTH_LONG).show();
			else {
				Core.getInstance().newTask(this, new AppCore.ProcessTask() {
					@Override
					public int execute(FiscalStorage storage, FNOperaionTask task,Object...args) {
						task.showProgress("Операция выполняется...");
						try {
							return storage.doXReport(new XReport(), true, null); 
						} catch(RemoteException re) {
							return Errors.SYSTEM_ERROR;
						}
					}
				}, new AppCore.ResultTask() {
					@Override
					public void onResult(int result) {
						if(result == Errors.NO_ERROR)
							Toast.makeText(Main.this, "Операция выполнена успешно", Toast.LENGTH_SHORT).show();
						else 
							Toast.makeText(Main.this, String.format("Операция выполнена с ошибкой %02X",result), Toast.LENGTH_LONG).show();
					}
				}).execute();
			}
			break;
		}
		
	}

}
