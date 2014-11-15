package com.example.jwhitmarsh.myapplication;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Array;


public class Scoreboard extends Activity {
    private static final String DEBUG_TAG = "HttpExample";
    final Handler h = new Handler();
    Runnable getScoreInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);


        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // new DownloadWebpageTask().execute("bas");
        } else {
            // display error
        }

        //set up Runnable which gets our score
        getScoreInterval = new Runnable() {
            private long time = 0;

            @Override
            public void run() {
                // can call h again after work!
                new DownloadWebpageTask().execute("bas");

                time += 1000;
                Log.d("TimerExample", "Going for... " + time);
                h.postDelayed(this, 300000); // refresh every 5 mins
            }
        };

        //trigger the runnable
        h.postDelayed(getScoreInterval, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scoreboard_actions, menu);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh: // manual refresh click
                new DownloadWebpageTask().execute("bas");
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onStop() {
        h.removeCallbacks(getScoreInterval);
        super.onStop();
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "Fucked it";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            buildScoreboard(result);
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        // TODO this might need to change if commentary is long
        int len = 5000;
        JSONObject reader = new JSONObject();

        Log.d(DEBUG_TAG, "Got here!");
        try {
            URL url = new URL("http://bic6588:9899/");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            //conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();

            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public void buildScoreboard(String data) {
        TextView battingTeam = (TextView) findViewById(R.id.batting_team);
        TextView battingScore = (TextView) findViewById(R.id.batting_score);
        TextView battingWickets = (TextView) findViewById(R.id.batting_wickets);

        TextView overNumber = (TextView) findViewById(R.id.over_number);
        TextView overRuns = (TextView) findViewById(R.id.over_runs);
        TextView overRunRate = (TextView) findViewById(R.id.over_rr);

        TextView sBatterName = (TextView) findViewById(R.id.sbatter_name);
        TextView sBatterRuns = (TextView) findViewById(R.id.sbatter_runs);
        TextView sBatterBalls = (TextView) findViewById(R.id.sbatter_balls);
        TextView nsBatterName = (TextView) findViewById(R.id.nsbatter_name);
        TextView nsBatterRuns = (TextView) findViewById(R.id.nsbatter_runs);
        TextView nsBatterBalls = (TextView) findViewById(R.id.nsbatter_balls);

        TextView bowlerName = (TextView) findViewById(R.id.bowler_name);
        TextView bowlerType = (TextView) findViewById(R.id.bowler_type);
        TextView bowlerOvers = (TextView) findViewById(R.id.bowler_overs);
        TextView bowlerMaidens = (TextView) findViewById(R.id.bowler_maidens);
        TextView bowlerRuns = (TextView) findViewById(R.id.bowler_runs);
        TextView bowlerWickets = (TextView) findViewById(R.id.bowler_wickets);

        LinearLayout commentaryLayout = (LinearLayout) findViewById(R.id.commentary);
        commentaryLayout.removeAllViews();

        try {
            JSONObject reader = new JSONObject(data);

            setTitle(reader.getString("shortTitle"));

            //overall
            battingScore.setText(reader.getString("runs"));
            battingWickets.setText(reader.getString("wickets"));
            battingTeam.setText(reader.getString("battingTeam"));

            //over info
            overNumber.setText(reader.getString("lastOver"));
            overRuns.setText("(" + reader.getString("lastOverRuns") + " runs)");
            overRunRate.setText(reader.getString("runRate"));

            //on strike batter
            JSONObject sBatterObj = reader.getJSONObject("sBatter");
            sBatterName.setText(sBatterObj.getString("name"));
            sBatterRuns.setText(sBatterObj.getString("runs"));
            sBatterBalls.setText("(" + sBatterObj.getString("balls") + ")");

            //non strike batter
            JSONObject nsBatterObj = reader.getJSONObject("nsBatter");
            nsBatterName.setText(nsBatterObj.getString("name"));
            nsBatterRuns.setText(nsBatterObj.getString("runs"));
            nsBatterBalls.setText("(" + nsBatterObj.getString("balls") + ")");

            //bowler
            JSONObject bowlerObj = reader.getJSONObject("bowler");
            bowlerName.setText(bowlerObj.getString("name"));
            bowlerType.setText("(" + bowlerObj.getString("type") + ")");
            bowlerOvers.setText(bowlerObj.getString("overs"));
            bowlerMaidens.setText(bowlerObj.getString("maidens"));
            bowlerRuns.setText(bowlerObj.getString("runs"));
            bowlerWickets.setText(bowlerObj.getString("wickets"));

            //commentary
            JSONArray commentaryData = reader.getJSONArray("commentary");
            for (int x = 0; x < commentaryData.length(); x++) {
                JSONObject commentaryObj = commentaryData.getJSONObject(x);
                TextView tv = new TextView(this);
                tv.setText(commentaryObj.getString("text"));
                commentaryLayout.addView(tv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
