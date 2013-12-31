package uk.co.johnsto.linkbasher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class ResolveActivity extends Activity implements Consts {
    public static final String EXTRA_ITERATION = "uk.co.johnsto.linkbasher.ITERATION";
    private SharedPreferences mPrefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handle(intent);
    }

    private class ResolveTask extends AsyncTask<Intent, Void, Intent> {
        int toastString;
        Toast progressToast;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressToast = Toast.makeText(ResolveActivity.this, R.string.toast_resolving, Toast.LENGTH_LONG);
            progressToast.show();
        }

        @Override
        protected Intent doInBackground(Intent... intents) {
            final Intent launch = intents[0];
            final Uri location = launch.getData();
            final int iteration = launch.getIntExtra(EXTRA_ITERATION, 0);

            Log.v(TAG, launch.toString());

            Uri originatingUri = launch.getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
            if(originatingUri == null) {
                originatingUri = location;
            }

            if(location == null) {
                return null;
            }

            if(DEBUG) {
                Log.d(TAG, "Incoming URL: " + location.toString() + " [" + String.valueOf(iteration) + "]");
            }

            // Don't resolve indefinitely
            if(iteration > MAX_ITERATIONS) {
                // return immediately after a number of redirections
                Log.w(TAG, "Exceeded redirection limit [" + String.valueOf(MAX_ITERATIONS) + "]");
                toastString = R.string.toast_max_redirects;
                return null;
            }

            // Check for potential redirect loop
            if(originatingUri.equals(location) && iteration > 0) {
                Log.w(TAG, "Possible redirect loop for " + location);
                toastString = R.string.toast_redirect_loop;
                return null;
            }


            try {
                // Fetch HEAD
                final HttpURLConnection conn = (HttpURLConnection) new URL(location.toString()).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("HEAD");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                // work around https://code.google.com/p/android/issues/detail?id=24672
                conn.setRequestProperty( "Accept-Encoding", "" );

                final int statusCode = conn.getResponseCode();
                final int statusDigit = statusCode / 100;

                if(DEBUG) {
                    Log.d(TAG, "Resolved " + String.valueOf(statusCode) + " for " + location);
                }

                if(statusDigit == 3) {
                    // ANY 3xx status is considered successful
                    Uri newLocation = Uri.parse(conn.getHeaderField("location"));
                    Log.d(TAG, "Resolved " + location + " to " + newLocation);
                    Intent intent = new Intent(Intent.ACTION_VIEW, newLocation);
                    intent.putExtra(Intent.EXTRA_REFERRER, location);
                    intent.putExtra(Intent.EXTRA_ORIGINATING_URI, originatingUri);
                    intent.putExtra(EXTRA_ITERATION, iteration + 1);
                    return intent;
                } else if(statusCode == 404) {
                    toastString = R.string.toast_got_404;
                } else {
                    toastString = R.string.toast_got_non_30x;
                }
            } catch (IOException e) {
                Log.e(TAG, "Couldn't resolve URL", e);
                toastString = R.string.toast_misc_error;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            super.onPostExecute(intent);
            progressToast.cancel();
            if(toastString != 0) {
                Toast.makeText(ResolveActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && data != null) {
            startActivityForResult(data, 0);
        } else {
            finish();
        }
    }

    private void handle(Intent launch) {
        try {
            Intent newIntent = new ResolveTask().execute(launch).get();
            if(newIntent != null) {
                startActivityForResult(newIntent, 0);
            } else {
                newIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                newIntent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, launch.getData() ));
                startActivityForResult(newIntent, 1);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Couldn't start activity", e);
            startActivity(Intent.createChooser(launch, null));
        } catch (ExecutionException e) {
            Log.e(TAG, "Couldn't start activity", e);
            startActivity(Intent.createChooser(launch, null));
        } finally {
            // increment counter
            int count = mPrefs.getInt(PREF_COUNT, 0);
            mPrefs.edit().putInt(PREF_COUNT, count + 1).commit();
            //finish();
        }
    }
}
