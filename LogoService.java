package it.cnlogo.app;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/** Logo CN sopra tutto, non tocca i tocchi. Stessa posizione in verticale e in orizzontale. */
public class LogoService extends Service {
    // valori della schermata "Personalizza logo"
    static final float POS_X = 0.93f;   // posizione orizzontale
    static final float POS_Y = 0.13f;   // posizione verticale
    static final float SIZE  = 0.07f;   // dimensione (rispetto al lato lungo dello schermo)
    static final float ALPHA = 0.80f;   // visibilita'

    static boolean running = false;
    private WindowManager wm;
    private ImageView view;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent i, int f, int id) {
        if (i != null && "STOP".equals(i.getAction())) { stopSelf(); return START_NOT_STICKY; }
        startForeground(1, notification());
        if (view == null) {
            wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            view = new ImageView(this);
            view.setImageResource(R.drawable.cn_logo);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setAlpha(ALPHA);
            try {
                wm.addView(view, params());
                running = true;
            } catch (Exception e) { stopSelf(); }
        }
        return START_STICKY;
    }

    private WindowManager.LayoutParams params() {
        DisplayMetrics m = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(m);
        int W = m.widthPixels, H = m.heightPixels;
        int w = Math.round(SIZE * Math.max(W, H));
        int h = Math.round(w * 307f / 512f);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(w, h,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = Math.round(POS_X * (W - w));
        p.y = Math.round(POS_Y * (H - h));
        return p;
    }

    /** ruota lo schermo: ricalcola posizione e dimensione */
    @Override public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        if (view != null) {
            view.post(() -> { try { wm.updateViewLayout(view, params()); } catch (Exception ignored) { } });
        }
    }

    private Notification notification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("cn", "CN Logo", NotificationManager.IMPORTANCE_MIN));
        Intent stop = new Intent(this, LogoService.class).setAction("STOP");
        PendingIntent pi = PendingIntent.getService(this, 0, stop, PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, "cn")
                .setSmallIcon(android.R.drawable.ic_menu_gallery)
                .setContentTitle("CN Logo attivo")
                .setContentText("Tocca per spegnere")
                .setContentIntent(pi).build();
    }

    @Override public void onDestroy() {
        if (view != null) { try { wm.removeView(view); } catch (Exception ignored) { } view = null; }
        running = false;
        super.onDestroy();
    }
}
