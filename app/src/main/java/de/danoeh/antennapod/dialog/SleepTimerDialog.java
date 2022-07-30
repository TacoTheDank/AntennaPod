package de.danoeh.antennapod.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.snackbar.Snackbar;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.databinding.TimeDialogBinding;
import de.danoeh.antennapod.event.playback.SleepTimerUpdatedEvent;
import de.danoeh.antennapod.core.preferences.SleepTimerPreferences;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.util.Converter;
import de.danoeh.antennapod.core.util.playback.PlaybackController;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SleepTimerDialog extends DialogFragment {
    private PlaybackController controller;
    private TimeDialogBinding binding;

    public SleepTimerDialog() {

    }

    @Override
    public void onStart() {
        super.onStart();
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
            }
        };
        controller.init();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (controller != null) {
            controller.release();
        }
        EventBus.getDefault().unregister(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = TimeDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.sleep_timer_label);
        builder.setView(binding.getRoot());
        builder.setPositiveButton(R.string.close_label, null);

        binding.timeDisplay.setVisibility(View.GONE);
        binding.extendSleepFiveMinutesButton.setText(getString(R.string.extend_sleep_timer_label, 5));
        binding.extendSleepTenMinutesButton.setText(getString(R.string.extend_sleep_timer_label, 10));
        binding.extendSleepTwentyMinutesButton.setText(getString(R.string.extend_sleep_timer_label, 20));
        binding.extendSleepFiveMinutesButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.extendSleepTimer(5 * 1000 * 60);
            }
        });
        binding.extendSleepTenMinutesButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.extendSleepTimer(10 * 1000 * 60);
            }
        });
        binding.extendSleepTwentyMinutesButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.extendSleepTimer(20 * 1000 * 60);
            }
        });

        binding.etxtTime.setText(SleepTimerPreferences.lastTimerValue());
        binding.etxtTime.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(binding.etxtTime, InputMethodManager.SHOW_IMPLICIT);
        }, 100);

        String[] spinnerContent = new String[] {
                getString(R.string.time_seconds),
                getString(R.string.time_minutes),
                getString(R.string.time_hours) };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, spinnerContent);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTimeUnit.setAdapter(spinnerAdapter);
        binding.spTimeUnit.setSelection(SleepTimerPreferences.lastTimerTimeUnit());

        binding.cbShakeToReset.setChecked(SleepTimerPreferences.shakeToReset());
        binding.cbVibrate.setChecked(SleepTimerPreferences.vibrate());
        binding.chAutoEnable.setChecked(SleepTimerPreferences.autoEnable());

        binding.cbShakeToReset.setOnCheckedChangeListener((buttonView, isChecked)
                -> SleepTimerPreferences.setShakeToReset(isChecked));
        binding.cbVibrate.setOnCheckedChangeListener((buttonView, isChecked)
                -> SleepTimerPreferences.setVibrate(isChecked));
        binding.chAutoEnable.setOnCheckedChangeListener((compoundButton, isChecked)
                -> SleepTimerPreferences.setAutoEnable(isChecked));

        binding.disableSleeptimerButton.setOnClickListener(v -> {
            if (controller != null) {
                controller.disableSleepTimer();
            }
        });
        binding.setSleeptimerButton.setOnClickListener(v -> {
            if (!PlaybackService.isRunning) {
                Snackbar.make(binding.getRoot(), R.string.no_media_playing_label, Snackbar.LENGTH_LONG).show();
                return;
            }
            try {
                long time = Long.parseLong(binding.etxtTime.getText().toString());
                if (time == 0) {
                    throw new NumberFormatException("Timer must not be zero");
                }
                SleepTimerPreferences.setLastTimer(binding.etxtTime.getText().toString(),
                        binding.spTimeUnit.getSelectedItemPosition());
                if (controller != null) {
                    controller.setSleepTimer(SleepTimerPreferences.timerMillis());
                }
                closeKeyboard(binding.getRoot());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Snackbar.make(binding.getRoot(),
                        R.string.time_dialog_invalid_input, Snackbar.LENGTH_LONG).show();
            }
        });
        return builder.create();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void timerUpdated(SleepTimerUpdatedEvent event) {
        binding.timeDisplay.setVisibility(event.isOver() || event.isCancelled() ? View.GONE : View.VISIBLE);
        binding.timeSetup.setVisibility(event.isOver() || event.isCancelled() ? View.VISIBLE : View.GONE);
        binding.time.setText(Converter.getDurationStringLong((int) event.getTimeLeft()));
    }

    private void closeKeyboard(View content) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(content.getWindowToken(), 0);
    }
}
