package com.mautilus.emcare;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class PushData {

    public static final String URL_PUSH_SERVER = "https://dataendpoint.mautilus.org/esclerosis/api/v1/push";

    private SQLiteDatabase db;
    public JobService job = null;
    public JobParameters jobParameters = null;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private void endJobService() {
        if (job != null) {
            job.jobFinished(jobParameters, false);
        }
    }

    private static final class BasicAuthenticator extends Authenticator {

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            String user = "android";
            String password = "MttamtNMmBtf4RcXjN7LxQmP8jZntmaEn3h2zEaHR92Judxrk3pKyre2gWv35ge2vi2yVRXBq9adj5gBA5NWMEf5kTFjq5XUezxL";
            return new PasswordAuthentication(user, password.toCharArray());
        }
    }

    private class PushTask implements Runnable {
        private final Context context;
        private final JSONArray json;
        private String key;

        public PushTask(Context context, JSONArray json, String key) {
            this.context = context;
            this.json = json;
            this.key = key;
        }

        @Override
        public void run() {
            try {
                if (key == null) {
                    key = RetrieveKeyFromContentProvider();
                    ParametersHelper params = new ParametersHelper(context);
                    params.set("KEY", key);
                }

                Log.i("PUSH", "A");
                URL url = new URL(URL_PUSH_SERVER);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Authenticator.setDefault(new BasicAuthenticator());

                Map<String, Object> params = new LinkedHashMap<>();
                params.put("data", json.toString());
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
                StringBuilder response = new StringBuilder();
                for (int c; (c = in.read()) >= 0;) {
                    response.append((char) c);
                }
                Log.i("PUSH_RESPONSE", response.toString());

            } catch (IOException e) {
                Log.i("PUSH", "Connection exception");
                e.printStackTrace();
            }

            db.execSQL("DELETE FROM " + Action.TABLE_NAME);
            endJobService();
        }

        private String RetrieveKeyFromContentProvider() {
            Uri contentUri = Uri.parse("content://com.mautilus.emcare.key.provider");
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(contentUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String key = cursor.getString(cursor.getColumnIndexOrThrow("Key"));
                Log.d("AppB", "Key retrieved from App A: " + key);
                cursor.close();

                ParametersHelper params = new ParametersHelper(context);
                params.set("KEY", key);
                return key;
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
                        if (!str.isEmpty()) {
                            rowObject.put(cursor.getColumnName(i), str);
                        }
                    } catch (Exception e) {
                        // Log if needed
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    public void push(Context appContext, String key) {
        String[] columns = {
                Action.FIELD_TYPE_NAME,
                Action.FIELD_TIME_NAME,
                Action.FIELD_TIME_START_NAME,
                Action.FIELD_ADDED_NAME,
                Action.FIELD_REMOVED_NAME
        };
        db = DatabaseConnection.getDatabase(appContext);
        Cursor cursor = db.query(Action.TABLE_NAME, columns, null, null, null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            endJobService();
            return;
        }

        JSONArray json = cursorToJSONArray(cursor);
        PushTask task = new PushTask(appContext, json, key);
        executor.execute(task);
    }

    public void update(Context context) {
        ParametersHelper parameters = new ParametersHelper(context);
        String nextSyncStr = parameters.get("TEXT_SYNC");

        if (nextSyncStr == null) {
            Calendar nowInstance = Calendar.getInstance();
            nowInstance.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(10, 16));
            parameters.set("TEXT_SYNC", String.valueOf(nowInstance.getTime().getTime()));
            return;
        }

        Calendar nowInstance = Calendar.getInstance();
        Date currentTimeMin = nowInstance.getTime();
        Date nextSync = new Date(Long.parseLong(nextSyncStr));

        if (currentTimeMin.after(nextSync)) {
            String key = parameters.get("KEY");
            push(context, key);

            nowInstance.add(Calendar.MINUTE, ThreadLocalRandom.current().nextInt(10, 16));
            parameters.set("TEXT_SYNC", String.valueOf(nowInstance.getTime().getTime()));

            context.startService(new Intent(context, StepCounterListener.class));
        }
    }
}