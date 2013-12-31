package uk.co.johnsto.linkbasher;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements Consts {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }

    public static class MainFragment extends Fragment {
        private SharedPreferences mPrefs;
        private TextView mtvCount;

        public MainFragment() {
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mtvCount = (TextView) rootView.findViewById(R.id.count);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            countUp();
        }

        /**
         * Counts up towards the current number of bashed links, as if this process were far more
         * dramatic and exciting than it actually is. It's not.
         */
        private void countUp() {
            final int finalCount = mPrefs.getInt(PREF_COUNT, 0);
            final Activity activity = getActivity();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;

                    while(count < finalCount && !activity.isFinishing()) {
                        final int thisCount = count;

                        // update TextView
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mtvCount.setText(String.format("%,d", thisCount));
                            }
                        });

                        if(count >= finalCount) {
                            count = finalCount;
                            break;
                        }

                        // increment fast at first, then slow down as we reach the total
                        count += Math.max(1, (finalCount - count) / 10);

                        // Wait a bit before updating again
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }).start();
        }
    }

}
