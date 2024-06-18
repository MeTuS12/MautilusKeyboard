package com.mautilus.emcare;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;


import java.util.Calendar;
import java.util.Date;

import androidx.annotation.RequiresApi;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PushActivityJobService extends JobService {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        String key = "";
        PushData push = new PushData();

        // Obtenemos la clave
        ParametersHelper parameters = new ParametersHelper(getApplicationContext());
        key = parameters.get("KEY");

        if (key != null) {
            push.job = this;
            push.jobParameters = jobParameters;
            push.push(getApplicationContext(), key);
        } else {
            return false;
        }

        parameters.set("PROCESS", "true");
        Date currentTime = Calendar.getInstance().getTime();
        parameters.set("PROCESS_LAST_EXEC", currentTime.toString());


//        jobFinished(jobParameters, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}