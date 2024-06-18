package com.mautilus.emcare;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.mautilus.emcare.entity.Action;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

public class PushData {

    public static final String URL_PUSH_SERVER = "http://82.223.70.174:80/esclerosis/api/v1/push";

    private SQLiteDatabase db;

    public JobService job = null;
    public JobParameters jobParameters = null;

    ParametersHelper parameters = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void endJobService() {
        if (job != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                job.jobFinished(jobParameters, false);
            }
        }
    }


    private final class BasicAuthenticator extends Authenticator {
        private String user = "android";
        private String password = "MttamtNMmBtf4RcXjN7LxQmP8jZntmaEn3h2zEaHR92Judxrk3pKyre2gWv35ge2vi2yVRXBq9adj5gBA5NWMEf5kTFjq5XUezxL";

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password.toCharArray());
        }
    }

    private class PushTask extends AsyncTask<String, String, String> {

        Context context;
        JSONArray json;
        String key;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        protected String doInBackground(String... urls) {
            // We make the connection
            try {
                if (key == null) {
                    key = RetrieveKeyFromContentProvider();
                    ParametersHelper params = new ParametersHelper(context);
                    params.set("KEY", key);
                }


                Log.i("PUSH", "A");
                // Creamos la conexión
                URL url = new URL(URL_PUSH_SERVER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // Definimos la autenticación básica
                Authenticator.setDefault(new PushData.BasicAuthenticator());

                // Generamos el cuerpo del post
                Map<String, Object> params = new LinkedHashMap<>();
                // Metemos los datos codificados en JSON en "data"
                params.put("data", json.toString());
                // Usamos el teléfono de clave individual
                params.put("key", key);
                Log.i("PUSH", "B");

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                Log.i("PUSH", "C");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Log.i("PUSH", "D");
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = "";
                Log.i("PUSH", "E");

                for (int c; (c = in.read()) >= 0; )
                    response += (char) c;


            } catch (IOException e) {
                Log.i("PUSH", "Connection exception");
                e.printStackTrace();
            }

            db.execSQL("DELETE FROM " + Action.TABLE_NAME);
            endJobService();
            return null;
        }

        protected String RetrieveKeyFromContentProvider()
        {
            // Definir la URI del Content Provider de la aplicación A
            Uri contentUri = Uri.parse("content://com.mautilus.emcare.key.provider");

            // Obtener el Content Resolver
            ContentResolver resolver = context.getContentResolver();

            // Realizar una consulta al Content Provider de la aplicación A para obtener la key
            Cursor cursor = resolver.query(contentUri, null, null, null, null);

            // Verificar si se obtuvo un resultado
            if (cursor != null && cursor.moveToFirst()) {
                // Obtener el valor de la key desde el cursor
                String key = cursor.getString(cursor.getColumnIndexOrThrow("Key"));
                Log.d("AppB", "Key obtenida desde App A: " + key);

                // Cerrar el cursor
                cursor.close();

                ParametersHelper params = new ParametersHelper(context);
                params.set("KEY", key);

                return  key;
            } else {
                return null;
            }
        }
    }


    public JSONArray cursorToJSONArray(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        String str = cursor.getString(i);
                        if (str.length() > 0) {
                            rowObject.put(cursor.getColumnName(i), str);
                        }
                    } catch (Exception e) {
//                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        return resultSet;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean push(Context appContext, String key) {

        String[] columns = {Action.FIELD_TYPE_NAME, Action.FIELD_TIME_NAME, Action.FIELD_TIME_START_NAME, Action.FIELD_ADDED_NAME, Action.FIELD_REMOVED_NAME};
        JSONArray json;

        db = DatabaseConnection.getDatabase(appContext);

        Cursor cursor = db.query(Action.TABLE_NAME, columns, null, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            endJobService();
            return true;
        }

        json = cursorToJSONArray(cursor);

        PushTask task = new PushTask();
        task.json = json;
        task.key = key;
        task.context = appContext;
        task.execute();

        // Limpiamos la DDBB
//        db.execSQL("DELETE FROM " + Action.TABLE_NAME);

        return true;
    }


//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void update(Context context) {
//        parameters = new ParametersHelper(context);
//
//        String key = parameters.get("KEY");
//
//        push(context, key);
//
//
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void update(Context context) {
        parameters = new ParametersHelper(context);
        String nextSyncStr = parameters.get("TEXT_SYNC");

        if (nextSyncStr == null) {
            Calendar nowInstance = Calendar.getInstance();
            nowInstance.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(10, 16));
            parameters.set("TEXT_SYNC", String.valueOf(nowInstance.getTime().getTime()));
            return;
        }

        Calendar nowInstance = Calendar.getInstance();
        Date currentTimeMin = nowInstance.getTime();
        Date nextSync = new Date(Long.valueOf(nextSyncStr));

        if (currentTimeMin.after(nextSync)) {
            String key = parameters.get("KEY");

            push(context, key);

            nowInstance.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(10, 16));
            parameters.set("TEXT_SYNC", String.valueOf(nowInstance.getTime().getTime()));

            // Iniciamos el servicio de recogida de pasos
            context.startService(new Intent(context, StepCounterListener.class));

        }

    }

}