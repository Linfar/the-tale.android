package org.thetale;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Andrey.Titov on 9/19/13.
 */
public class ServerOperation implements RequestService.Operation {

    public static final String SERVER_URL_STRING = "http://the-tale.org/game/info";

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
        NetworkConnection nc = new NetworkConnection(context, SERVER_URL_STRING);
        HashMap<String, String> map = new HashMap<String, String>(1);
        map.put("account", String.valueOf(request.getString("account")));
        nc.setParameters(map);

        NetworkConnection.ConnectionResult cr = nc.execute();
        ContentValues heroData = null;
        ContentValues journalData[] = null;
        try {
            JSONObject object = new JSONObject(cr.body);
            if (!object.has("status")) {
                return null;
            }

            if (object.getString("status").equalsIgnoreCase("ok")) {
                heroData = new ContentValues();
                JSONObject data = object.getJSONObject("data");
                if (data.has("hero")) {
                    JSONObject hero = data.getJSONObject("hero");
                    if (hero.has("base")) {
                        JSONObject base = hero.getJSONObject("base");
                        collectHeroData(heroData, base, data);
                    }
                    if (hero.has("diary")) {
                        JSONArray diary = hero.getJSONArray("diary");
                        journalData = new ContentValues[diary.length()];
                        for (int i = 0; i < diary.length(); ++i) {
                            JSONArray o = diary.getJSONArray(i);
                            ContentValues value = new ContentValues();
                            value.put(ServerContract.Journal._COUNT, 1);
                            value.put(ServerContract.Journal.TIME, o.getString(0));
                            value.put(ServerContract.Journal.TIME_V, o.getString(1));
                            value.put(ServerContract.Journal.DESCRIPTION, o.getString(2));
                            value.put(ServerContract.Journal.DATE_V, o.getString(3));
                            journalData[i] = value;
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }

        if (heroData != null) {
            context.getContentResolver().delete(ServerContract.HeroData.CONTENT_URI, null, null);
            context.getContentResolver().insert(ServerContract.HeroData.CONTENT_URI, heroData);
        }
        if (journalData != null && journalData.length > 0) {
            context.getContentResolver().delete(ServerContract.Journal.CONTENT_URI, null, null); // ughhh how doe's it sucks. Journal should be incremental.

            context.getContentResolver().bulkInsert(ServerContract.Journal.CONTENT_URI, journalData);
        }
        return null;
    }

    private void collectHeroData(ContentValues heroData, JSONObject base, JSONObject data) throws JSONException {
        heroData.put(ServerContract.HeroData._COUNT, 1);
        heroData.put(ServerContract.HeroDataColumns.DESTINY_POINTS, base.getInt(ServerContract.HeroDataColumns.DESTINY_POINTS));
        heroData.put(ServerContract.HeroDataColumns.MAX_HEALTH, base.getInt(ServerContract.HeroDataColumns.MAX_HEALTH));
        heroData.put(ServerContract.HeroDataColumns.EXP_TO_NEXT_LEVEL, base.getInt(ServerContract.HeroDataColumns.EXP_TO_NEXT_LEVEL));
        heroData.put(ServerContract.HeroDataColumns.GENDER, base.getInt(ServerContract.HeroDataColumns.GENDER));
        heroData.put(ServerContract.HeroDataColumns.LEVEL, base.getInt(ServerContract.HeroDataColumns.LEVEL));
        heroData.put(ServerContract.HeroDataColumns.NAME, base.getString(ServerContract.HeroDataColumns.NAME));
        heroData.put(ServerContract.HeroDataColumns.HEALTH, base.getInt(ServerContract.HeroDataColumns.HEALTH));
        heroData.put(ServerContract.HeroDataColumns.EXPERIENCE, base.getInt(ServerContract.HeroDataColumns.EXPERIENCE));

        if (data.getJSONObject("hero").has("action")) {
            JSONObject action = data.getJSONObject("hero").getJSONObject("action");
            heroData.put(ServerContract.HeroDataColumns.CURRENT_ACTION_DESCRIPTION, action.getString("description"));
        } else {
            heroData.put(ServerContract.HeroDataColumns.CURRENT_ACTION_DESCRIPTION, "<not found>");
        }

        heroData.put(ServerContract.HeroDataColumns.RACE, base.getString(ServerContract.HeroDataColumns.RACE));
    }
}
