package com.mautilus.emcare;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mautilus.emcare.entity.Action;

import java.util.Calendar;
import java.util.Date;


/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class StepCounterListener extends Service implements SensorEventListener {

    private final static String TAG = "EMCARE STEP COUNTER";

    private final static int TYPE_STEPS = 3;
    private final static int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 12345;

    private static int steps;

    private LocalBinder accellBinder = new LocalBinder();

    public static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;

//    private final BroadcastReceiver shutdownReceiver = new ShutdownRecevier();

    /*
     * Sensor objects
     */
    private SensorManager mSensorManager;
    private Sensor mStepSensor;

    /*
     * Global data that is used to make sure the service is actually up and
     * running before using it.
     */
    private boolean started = false;
    private boolean registered = false;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("EMCARE STEP COUNTER", "On Create");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("EMCARE STEP COUNTER", "On Start");
        synchronized(this) {
            if(started) {
                return START_STICKY;
            }

            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            // Nos suscribimos al sensor
            registered = mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            started = true;
            Log.i("EMCARE STEP COUNTER", "Suscribed!");
            Log.i("EMCARE STEP COUNTER", String.valueOf(registered));
        }
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return accellBinder;
    }


    @Override
    public void onDestroy() {
        // Quitamos el sensor
        mSensorManager.unregisterListener(this, mStepSensor);
        super.onDestroy();
    }


    public class LocalBinder extends Binder {
        StepCounterListener getService() {
            return StepCounterListener.this;
        }
    }


    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // nobody knows what happens here: step value might magically decrease
        // when this method is called...
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        Log.i("EMCARE STEP COUNTER", "Sensor Changed!!");
        if (event.values[0] > Integer.MAX_VALUE) {

//            if (BuildConfig.DEBUG) Log.i(TAG, "probably not a real value: " + event.values[0]);

        } else {

            steps = (int) event.values[0];

            SQLiteDatabase db = DatabaseConnection.getDatabase(getApplicationContext());

            ContentValues values = new ContentValues();

            values.put(Action.FIELD_ADDED_NAME, steps);
            values.put(Action.FIELD_TYPE_NAME, TYPE_STEPS);

            // Ponemos el tiempo según corresponda.
            Calendar nowInstance = Calendar.getInstance();
            Date currentTimeMin = nowInstance.getTime();
            values.put(Action.FIELD_TIME_NAME, currentTimeMin.getTime());

            Long id = db.insert(Action.TABLE_NAME, Action.FIELD_ID_NAME, values);

        }
    }

}