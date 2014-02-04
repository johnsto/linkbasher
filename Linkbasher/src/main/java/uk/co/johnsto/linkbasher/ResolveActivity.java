package uk.co.johnsto.linkbasher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ResolveActivity extends Activity implements Consts {
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
        Intent serviceIntent = new Intent(this, ResolveService.class);
        serviceIntent.setData(intent.getData());
        serviceIntent.putExtras(intent.getExtras());
        startService(serviceIntent);
        finish();
    }
}