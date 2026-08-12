package com.example.tapcounter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FloatingService extends Service {

    private WindowManager windowManager;
    private View widgetView;
    private TextView counterText;
    private TextView resetBtn;
    private TextView dragHint;

    private int counter = 0;
    private WindowManager.LayoutParams widgetParams;

    private float initialX, initialY;
    private int initialTouchX, initialTouchY;
    private static final int DRAG_THRESHOLD = 15;
    private boolean isDragging = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        createNotificationChannel();
        startForeground(1, buildNotification());
        initWidget();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "counter_channel", "Counter Overlay", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "counter_channel");
        } else {
            builder = new Notification.Builder(this);
        }
        return builder
                .setContentTitle("Tap Counter")
                .setContentText("Overlay is active - tap widget to count")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void initWidget() {
        widgetView = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        counterText = widgetView.findViewById(R.id.counter_text);
        resetBtn = widgetView.findViewById(R.id.reset_btn);
        dragHint = widgetView.findViewById(R.id.drag_hint);

        counterText.setText("0");

        resetBtn.setOnClickListener(v -> {
            counter = 0;
            updateCounter();
            animateReset();
        });

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        widgetParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        widgetParams.gravity = Gravity.TOP | Gravity.START;
        widgetParams.x = 50;
        widgetParams.y = 150;

        windowManager.addView(widgetView, widgetParams);
        setupTouchHandling();
    }

    private void setupTouchHandling() {
        widgetView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = widgetParams.x;
                    initialY = widgetParams.y;
                    initialTouchX = (int) event.getRawX();
                    initialTouchY = (int) event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    int deltaX = (int) (event.getRawX() - initialTouchX);
                    int deltaY = (int) (event.getRawY() - initialTouchY);

                    if (Math.abs(deltaX) > DRAG_THRESHOLD || Math.abs(deltaY) > DRAG_THRESHOLD) {
                        isDragging = true;
                        widgetParams.x = (int) initialX + deltaX;
                        widgetParams.y = (int) initialY + deltaY;
                        windowManager.updateViewLayout(widgetView, widgetParams);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        counter++;
                        updateCounter();
                        animateCount();
                    }
                    return true;
            }
            return false;
        });
    }

    private void updateCounter() {
        counterText.setText(String.valueOf(counter));
    }

    private void animateCount() {
        counterText.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(80)
                .withEndAction(() -> {
                    counterText.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(80)
                            .start();
                })
                .start();
    }

    private void animateReset() {
        counterText.animate()
                .alpha(0.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    counterText.animate()
                            .alpha(1.0f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (widgetView != null && widgetView.getParent() != null) {
            windowManager.removeView(widgetView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
