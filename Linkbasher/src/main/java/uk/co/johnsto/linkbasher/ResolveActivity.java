package uk.co.johnsto.linkbasher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ResolveActivity extends Activity implements Consts {
    public final static int REQ_PICKER = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handle(intent);
    }

    private void handle(Intent intent) {
        final String action = intent.getAction();

        if(Intent.ACTION_VIEW.equals(action)) {
            // Bootstrap resolver
            Intent serviceIntent = new Intent(this, ResolveService.class);
            serviceIntent.setData(intent.getData());
            if(intent.getExtras() != null) {
                serviceIntent.putExtras(intent.getExtras());
            }
            startService(serviceIntent);
            finish();
        } else if(Intent.ACTION_PICK_ACTIVITY.equals(action)) {
            // Resolver failed, so prompt user to choose a different app..
            Intent pickIntent = new Intent(Intent.ACTION_CHOOSER);
            pickIntent.putExtra(
                    Intent.EXTRA_INTENT,
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
            );
            startActivityForResult(pickIntent, REQ_PICKER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_PICKER && data != null) {
            // Result from picker - launch app
            startActivityForResult(data, 0);
        } else {
            // Result from launched app, user probably hit BACK from browser, so just close.
            finish();
        }
    }
}