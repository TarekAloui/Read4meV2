package com.aloui.tarek.read4me.Other;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.aloui.tarek.read4me.HomeFragment;

/**
 * Created by Tarek on 07/01/2018.
 */

public class WaitForPagesLoadThread extends AsyncTask<HomeFragment, Void, HomeFragment>{

    @Override
    protected HomeFragment doInBackground(HomeFragment... homeFragments) {
        while (homeFragments[0].getReadInd() >= homeFragments[0].getPages().size())
        {

        }
        return homeFragments[0];
    }

    @Override
    protected void onPostExecute(HomeFragment homeFragment) {
        super.onPostExecute(homeFragment);

    }
}
