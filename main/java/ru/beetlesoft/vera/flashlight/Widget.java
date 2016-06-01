package ru.beetlesoft.vera.flashlight;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Arrays;

public class Widget extends AppWidgetProvider {
    final String TAG = "Widget";
    private static String CLICK_ACTION = "click";
    private Camera camera;
    private boolean isFlashOn = false;
    Camera.Parameters params;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        if (CLICK_ACTION.equals(action)) {
            Log.d(TAG, "clicked, isFlashOn " + String.valueOf(isFlashOn));
            updateData(context);

        }

    }

    @Override
    public void onEnabled(final Context context) {
        super.onEnabled(context);
        Log.d(TAG, "onEnabled");
        hasFlash(context);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.d(TAG, "onUpdate " + Arrays.toString(appWidgetIds));
        getCamera();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        ComponentName watchWidget = new ComponentName(context, Widget.class);

        PendingIntent pendingIntent = getPendingSelfIntent(context, CLICK_ACTION);
        remoteViews.setOnClickPendingIntent(R.id.btn_flashlight, pendingIntent);

        updateData(context);

        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Widget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "onDisabled");
    }

    // getting camera parameters
    private void getCamera() {
        if (camera == null) {
            try {
                releaseCameraAndPreview();
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Camera Error: ", e.getMessage());
            }
        }
    }

    private void releaseCameraAndPreview() {
        Log.d(TAG, "releaseCameraAndPreview");
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void hasFlash(Context context) {
        boolean hasFlash = context.getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .create();
            dialog.setTitle("Error");
            dialog.setMessage("Sorry, your device doesn't support flash light!");
            dialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
//                    finish();
                }
            });
            dialog.show();
            return;
        }
    }

    private void updateData(Context context) {
        Log.d(TAG, "updateData");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        ComponentName watchWidget = new ComponentName(context, Widget.class);

        if (isFlashOn) {
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();

            isFlashOn = false;

            remoteViews.setTextViewText(R.id.btn_flashlight, "ON");
        } else {

            if (camera == null ) {
                Log.d(TAG, "null camera");
                getCamera();
            }


            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();

            isFlashOn = true;

            remoteViews.setTextViewText(R.id.btn_flashlight, "OFF");
        }
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);
    }
}
