package de.danoeh.antennapod.activity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.preferences.ThemeSwitcher;
import de.danoeh.antennapod.core.receiver.PlayerWidget;
import de.danoeh.antennapod.core.widget.WidgetUpdaterWorker;
import de.danoeh.antennapod.databinding.ActivityWidgetConfigBinding;

public class WidgetConfigActivity extends AppCompatActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ActivityWidgetConfigBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemeSwitcher.getTheme(this));
        super.onCreate(savedInstanceState);
        viewBinding = ActivityWidgetConfigBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        viewBinding.butConfirm.setOnClickListener(v -> confirmCreateWidget());
        viewBinding.widgetOpacitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                viewBinding.widgetOpacityTextView.setText(seekBar.getProgress() + "%");
                int color = getColorWithAlpha(
                        PlayerWidget.DEFAULT_COLOR, viewBinding.widgetOpacitySeekBar.getProgress());
                viewBinding.widgetConfigPreview.widgetLayout.setBackgroundColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        viewBinding.widgetConfigPreview.txtNoPlaying.setVisibility(View.GONE);
        viewBinding.widgetConfigPreview.txtvTitle.setVisibility(View.VISIBLE);
        viewBinding.widgetConfigPreview.txtvTitle.setText(R.string.app_name);
        viewBinding.widgetConfigPreview.txtvProgress.setVisibility(View.VISIBLE);
        viewBinding.widgetConfigPreview.txtvProgress.setText(R.string.position_default_label);

        viewBinding.ckPlaybackSpeed.setOnClickListener(v -> displayPreviewPanel());
        viewBinding.ckRewind.setOnClickListener(v -> displayPreviewPanel());
        viewBinding.ckFastForward.setOnClickListener(v -> displayPreviewPanel());
        viewBinding.ckSkip.setOnClickListener(v -> displayPreviewPanel());

        setInitialState();
    }

    private void setInitialState() {
        SharedPreferences prefs = getSharedPreferences(PlayerWidget.PREFS_NAME, MODE_PRIVATE);
        viewBinding.ckPlaybackSpeed.setChecked(
                prefs.getBoolean(PlayerWidget.KEY_WIDGET_PLAYBACK_SPEED + appWidgetId, false));
        viewBinding.ckRewind.setChecked(
                prefs.getBoolean(PlayerWidget.KEY_WIDGET_REWIND + appWidgetId, false));
        viewBinding.ckFastForward.setChecked(
                prefs.getBoolean(PlayerWidget.KEY_WIDGET_FAST_FORWARD + appWidgetId, false));
        viewBinding.ckSkip.setChecked(
                prefs.getBoolean(PlayerWidget.KEY_WIDGET_SKIP + appWidgetId, false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            int color = prefs.getInt(PlayerWidget.KEY_WIDGET_COLOR + appWidgetId, 0);
            int opacity = Color.alpha(color) * 100 / 0xFF;

            viewBinding.widgetOpacitySeekBar.setProgress(opacity, false);
        }
        displayPreviewPanel();
    }

    private void displayPreviewPanel() {
        boolean showExtendedPreview = viewBinding.ckPlaybackSpeed.isChecked() || viewBinding.ckRewind.isChecked()
                || viewBinding.ckFastForward.isChecked() || viewBinding.ckSkip.isChecked();
        viewBinding.widgetConfigPreview.extendedButtonsContainer
                .setVisibility(showExtendedPreview ? View.VISIBLE : View.GONE);
        viewBinding.widgetConfigPreview.butPlay.setVisibility(showExtendedPreview ? View.GONE : View.VISIBLE);
        viewBinding.widgetConfigPreview.butPlaybackSpeed
                .setVisibility(viewBinding.ckPlaybackSpeed.isChecked() ? View.VISIBLE : View.GONE);
        viewBinding.widgetConfigPreview.butFastForward
                .setVisibility(viewBinding.ckFastForward.isChecked() ? View.VISIBLE : View.GONE);
        viewBinding.widgetConfigPreview.butSkip.setVisibility(
                viewBinding.ckSkip.isChecked() ? View.VISIBLE : View.GONE);
        viewBinding.widgetConfigPreview.butRew.setVisibility(
                viewBinding.ckRewind.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void confirmCreateWidget() {
        int backgroundColor = getColorWithAlpha(
                PlayerWidget.DEFAULT_COLOR, viewBinding.widgetOpacitySeekBar.getProgress());

        SharedPreferences prefs = getSharedPreferences(PlayerWidget.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PlayerWidget.KEY_WIDGET_COLOR + appWidgetId, backgroundColor);
        editor.putBoolean(PlayerWidget.KEY_WIDGET_PLAYBACK_SPEED + appWidgetId,
                viewBinding.ckPlaybackSpeed.isChecked());
        editor.putBoolean(PlayerWidget.KEY_WIDGET_SKIP + appWidgetId, viewBinding.ckSkip.isChecked());
        editor.putBoolean(PlayerWidget.KEY_WIDGET_REWIND + appWidgetId, viewBinding.ckRewind.isChecked());
        editor.putBoolean(PlayerWidget.KEY_WIDGET_FAST_FORWARD + appWidgetId, viewBinding.ckFastForward.isChecked());
        editor.apply();

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
        WidgetUpdaterWorker.enqueueWork(this);
    }

    private int getColorWithAlpha(int color, int opacity) {
        return (int) Math.round(0xFF * (0.01 * opacity)) * 0x1000000 + color;
    }
}
