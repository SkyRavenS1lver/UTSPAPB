package com.example.gmaps;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextViewText(R.id.appwidget_text2, String.valueOf(MapsActivity.currentTemp+"°C"));
        views.setTextViewText(R.id.appwidget_text, String.valueOf(MapsActivity.currentHumidity)+"%");
//        Intent configIntent = new Intent(context, WidgetSensor.class);
//        configIntent.addCategory("android.intent.category.LAUNCHER");
//        configIntent.setAction("android.intent.action.MAIN");
//        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
//        views.setOnClickPendingIntent(R.id.button_update, configPendingIntent);
//        PendingIntent.getActivity(context, 0,configIntent,0);
//        appWidgetManager.updateAppWidget(appWidgetId, views);
//        Intent intentUpdate = new Intent(context, WeatherWidget
//                .class);
//        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//        int[] idArray = new int[]{appWidgetId};
//        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,appWidgetId,intentUpdate,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
//        views.setOnClickPendingIntent(R.id.button_update, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}