package uk.co.johnsto.linkbasher;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResolveService extends IntentService implements Consts {
    public static final String EXTRA_ITERATION = "uk.co.johnsto.linkbasher.ITERATION";
    private Handler handler;
    private SharedPreferences prefs;
    private Toast resolvingToast;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public ResolveService() {
        super("Linkbasher");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        // Show icon in notification area
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notif = new Notification.Builder(getApplicationContext())
                //.setTicker("Resolving " + intent.getData() + "...")
                .getNotification();
        startForeground(1, notif);

        handler.post(new Runnable() {
            @Override
            public void run() {
                resolvingToast = Toast.makeText(
                        ResolveService.this,
                        "Resolving " + intent.getData() + "...",
                        Toast.LENGTH_LONG
                );
                resolvingToast.show();
            }
        });

        try {
            // Resolve incoming intent
            Intent newIntent = resolve(intent);
            if(newIntent != null) {
                // Resolved successfully => launch as normal
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
                int count = prefs.getInt(PREF_COUNT, 0);
                prefs.edit().putInt(PREF_COUNT, count + 1).commit();
            } else {
                // Failed to resolve => pass original intent through to system picker instead
                newIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                newIntent.putExtra(
                        Intent.EXTRA_INTENT,
                        new Intent(Intent.ACTION_VIEW, intent.getData())
                );
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }
        } finally {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if(resolvingToast != null) {
                        resolvingToast.cancel();
                    }
                }
            });

            // Notification complete
            stopForeground(true);
        }
    }

    protected Intent resolve(Intent launch) {
        final Uri location = launch.getData();
        final int iteration = launch.getIntExtra(EXTRA_ITERATION, 0);

        // Maintain originating URL for children
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

        try {
            Uri target = new ResolveTask()
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, location)
                    .get(10, TimeUnit.SECONDS);

            if(target != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, target);
                intent.putExtra(Intent.EXTRA_REFERRER, location);
                intent.putExtra(Intent.EXTRA_ORIGINATING_URI, originatingUri);
                intent.putExtra(EXTRA_ITERATION, iteration + 1);
                return intent;
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Couldn't resolve URL", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Couldn't resolve URL", e);
        } catch (TimeoutException e) {
            Log.e(TAG, "Couldn't resolve URL", e);
        } finally {
            stopForeground(true);
        }
        return null;
    }

    public static class ResolveTask extends AsyncTask<Uri, Void, Uri> {
        @Override
        protected Uri doInBackground(Uri... uris) {
            final Uri origin = uris[0];
            Uri location = origin;
            int redirects = 0;
            while(++redirects < 5) {
                try {
                    // Fetch HEAD
                    final HttpURLConnection conn = (HttpURLConnection) new URL(location.toString()).openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);

                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestMethod("HEAD");

                    // work around https://code.google.com/p/android/issues/detail?id=24672
                    conn.setRequestProperty( "Accept-Encoding", "" );

                    final int statusCode = conn.getResponseCode();
                    final int statusDigit = statusCode / 100;

                    if(DEBUG) {
                        Log.d(TAG, "Resolved " + String.valueOf(statusCode) + " for " + location);
                    }

                    if(statusDigit == 2) {
                        // 2xx - probably not a redirect, so let user pick another activity
                        return location;
                    } else if(statusDigit == 3) {
                        // 3xx - fetch Location header and use that
                        location = Uri.parse(conn.getHeaderField("location"));
                        if(DEBUG) {
                            Log.d(TAG, "Resolved " + origin + " to " + location);
                        }
                        continue;
                    } else if(statusCode == 404) {
                        return null;
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Couldn't resolve URL", e);
                    return null;
                }
            }
            return origin;
        }
    }
}
