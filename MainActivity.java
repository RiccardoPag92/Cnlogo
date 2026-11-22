package it.cnlogo.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Button btn;
    private TextView info;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(48, 48, 48, 48);

        TextView t = new TextView(this);
        t.setText("CN LOGO");
        t.setTextColor(Color.WHITE);
        t.setTextSize(34);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setGravity(Gravity.CENTER);
        root.addView(t);

        info = new TextView(this);
        info.setTextColor(Color.rgb(255, 200, 0));
        info.setTextSize(15);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 24, 0, 48);
        root.addView(info);

        btn = new Button(this);
        btn.setTextSize(20);
        btn.setTypeface(Typeface.DEFAULT_BOLD);
        btn.setBackgroundColor(Color.rgb(255, 200, 0));
        btn.setTextColor(Color.BLACK);
        btn.setOnClickListener(v -> toggle());
        root.addView(btn, new LinearLayout.LayoutParams(-1, 160));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void toggle() {
        if (LogoService.running) {
            startService(new Intent(this, LogoService.class).setAction("STOP"));
            btn.postDelayed(this::refresh, 400);
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            return;
        }
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        startForegroundService(new Intent(this, LogoService.class));
        btn.postDelayed(this::refresh, 400);
    }

    private void refresh() {
        if (!Settings.canDrawOverlays(this)) {
            info.setText("Prima devi dare il permesso\n\"Mostra sopra altre app\"");
            btn.setText("DAI IL PERMESSO");
        } else if (LogoService.running) {
            info.setText("Logo acceso, in verticale e in orizzontale");
            btn.setText("SPEGNI");
        } else {
            info.setText("Logo spento");
            btn.setText("ACCENDI");
        }
    }
}
