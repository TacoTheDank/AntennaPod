package de.danoeh.antennapod.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.util.playback.PlaybackController;
import de.danoeh.antennapod.databinding.AudioControlsBinding;
import java.util.List;

public class PlaybackControlsDialog extends DialogFragment {
    private PlaybackController controller;
    private AlertDialog dialog;
    private AudioControlsBinding binding;

    public static PlaybackControlsDialog newInstance() {
        Bundle arguments = new Bundle();
        PlaybackControlsDialog dialog = new PlaybackControlsDialog();
        dialog.setArguments(arguments);
        return dialog;
    }

    public PlaybackControlsDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onStart() {
        super.onStart();
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
                setupUi();
                setupAudioTracks();
            }
        };
        controller.init();
        setupUi();
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.release();
        controller = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = AudioControlsBinding.inflate(getLayoutInflater());
        dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.audio_controls)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.close_label, null).create();
        return dialog;
    }

    private void setupUi() {
        binding.stereoToMono.setChecked(UserPreferences.stereoToMono());
        if (controller != null && !controller.canDownmix()) {
            binding.stereoToMono.setEnabled(false);
            String sonicOnly = getString(R.string.sonic_only);
            binding.stereoToMono.setText(getString(R.string.stereo_to_mono) + " [" + sonicOnly + "]");
        }

        binding.skipSilence.setChecked(UserPreferences.isSkipSilence());
        if (!UserPreferences.useExoplayer()) {
            binding.skipSilence.setEnabled(false);
            String exoplayerOnly = getString(R.string.exoplayer_only);
            binding.skipSilence.setText(
                    getString(R.string.pref_skip_silence_title) + " [" + exoplayerOnly + "]");
        }
        binding.skipSilence.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPreferences.setSkipSilence(isChecked);
            controller.setSkipSilence(isChecked);
        });
        binding.stereoToMono.setOnCheckedChangeListener((buttonView, isChecked) -> {
            UserPreferences.stereoToMono(isChecked);
            if (controller != null) {
                controller.setDownmix(isChecked);
            }
        });
    }

    private void setupAudioTracks() {
        List<String> audioTracks = controller.getAudioTracks();
        int selectedAudioTrack = controller.getSelectedAudioTrack();
        if (audioTracks.size() < 2 || selectedAudioTrack < 0) {
            binding.audioTracks.setVisibility(View.GONE);
            return;
        }

        binding.audioTracks.setVisibility(View.VISIBLE);
        binding.audioTracks.setText(audioTracks.get(selectedAudioTrack));
        binding.audioTracks.setOnClickListener(v -> {
            controller.setAudioTrack((selectedAudioTrack + 1) % audioTracks.size());
            new Handler(Looper.getMainLooper()).postDelayed(this::setupAudioTracks, 500);
        });
    }
}
