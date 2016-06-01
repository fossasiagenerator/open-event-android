package org.fossasia.openevent.api.processor;

import android.util.Log;

import org.fossasia.openevent.OpenEventApp;
import org.fossasia.openevent.api.protocol.SessionResponseList;
import org.fossasia.openevent.data.Session;
import org.fossasia.openevent.dbutils.DbContract;
import org.fossasia.openevent.dbutils.DbSingleton;
import org.fossasia.openevent.events.SessionDownloadEvent;
import org.fossasia.openevent.utils.CommonTaskLoop;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * User: MananWason
 * Date: 27-05-2015
 */
public class SessionListResponseProcessor implements Callback<SessionResponseList> {
    private static final String TAG = "Session";

    @Override
    public void onResponse(Call<SessionResponseList> call, final Response<SessionResponseList> response) {
        Log.d(TAG,"run  ");

        if (response.isSuccessful()) {
            Log.d(TAG,"run  ");

            CommonTaskLoop.getInstance().post(new Runnable() {

                @Override
                public void run() {
                    Log.d(TAG,"run " + response.body().sessions.size());

                    ArrayList<String> queries = new ArrayList<String>();
                    if(response.body().sessions == null){
                        Log.d(TAG,"Null");

                    }
                    int count = 0;
                    for (Session session : response.body().sessions) {
                        session.setId(count);
                        String query = session.generateSql();
                        queries.add(query);
                        count++;
                        Log.d(TAG, count + "  " + query);
                    }

                    DbSingleton dbSingleton = DbSingleton.getInstance();
                    dbSingleton.clearDatabase(DbContract.Sessions.TABLE_NAME);
                    dbSingleton.insertQueries(queries);
                    OpenEventApp.postEventOnUIThread(new SessionDownloadEvent(true));
                }


            });
        } else {
            Log.d(TAG,"run not ");

            OpenEventApp.getEventBus().post(new SessionDownloadEvent(false));
        }
    }

    @Override
    public void onFailure(Call<SessionResponseList> call, Throwable t) {
        Log.d(TAG,t.getMessage());

        OpenEventApp.getEventBus().post(new SessionDownloadEvent(false));
    }
}