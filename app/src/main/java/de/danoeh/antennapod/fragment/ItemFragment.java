package de.danoeh.antennapod.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowOrientationRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.adapter.actionbutton.CancelDownloadActionButton;
import de.danoeh.antennapod.adapter.actionbutton.DeleteActionButton;
import de.danoeh.antennapod.adapter.actionbutton.DownloadActionButton;
import de.danoeh.antennapod.adapter.actionbutton.ItemActionButton;
import de.danoeh.antennapod.adapter.actionbutton.MarkAsPlayedActionButton;
import de.danoeh.antennapod.adapter.actionbutton.PauseActionButton;
import de.danoeh.antennapod.adapter.actionbutton.PlayActionButton;
import de.danoeh.antennapod.adapter.actionbutton.PlayLocalActionButton;
import de.danoeh.antennapod.adapter.actionbutton.StreamActionButton;
import de.danoeh.antennapod.adapter.actionbutton.VisitWebsiteActionButton;
import de.danoeh.antennapod.core.databinding.PopupBubbleViewBinding;
import de.danoeh.antennapod.databinding.FeeditemFragmentBinding;
import de.danoeh.antennapod.event.EpisodeDownloadEvent;
import de.danoeh.antennapod.core.util.PlaybackStatus;
import de.danoeh.antennapod.event.FeedItemEvent;
import de.danoeh.antennapod.event.PlayerStatusEvent;
import de.danoeh.antennapod.event.UnreadItemsUpdateEvent;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedMedia;
import de.danoeh.antennapod.core.feed.util.ImageResourceUtils;
import de.danoeh.antennapod.core.preferences.UsageStatistics;
import de.danoeh.antennapod.net.download.serviceinterface.DownloadServiceInterface;
import de.danoeh.antennapod.storage.preferences.UserPreferences;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.util.Converter;
import de.danoeh.antennapod.core.util.DateFormatter;
import de.danoeh.antennapod.ui.common.ThemeUtils;
import de.danoeh.antennapod.core.util.playback.PlaybackController;
import de.danoeh.antennapod.core.util.gui.ShownotesCleaner;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Objects;

/**
 * Displays information about a FeedItem and actions.
 */
public class ItemFragment extends Fragment {

    private static final String TAG = "ItemFragment";
    private static final String ARG_FEEDITEM = "feeditem";

    /**
     * Creates a new instance of an ItemFragment
     *
     * @param feeditem The ID of the FeedItem to show
     * @return The ItemFragment instance
     */
    public static ItemFragment newInstance(long feeditem) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FEEDITEM, feeditem);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean itemsLoaded = false;
    private long itemId;
    private FeedItem item;
    private String webviewData;

    private ItemActionButton actionButton1;
    private ItemActionButton actionButton2;

    private Disposable disposable;
    private PlaybackController controller;

    private FeeditemFragmentBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemId = getArguments().getLong(ARG_FEEDITEM);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = FeeditemFragmentBinding.inflate(inflater);

        binding.txtvPodcast.setOnClickListener(v -> openPodcast());
        if (Build.VERSION.SDK_INT >= 23) {
            binding.txtvTitle.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);
        }
        binding.txtvTitle.setEllipsize(TextUtils.TruncateAt.END);
        binding.webvDescription.setTimecodeSelectedListener(time -> {
            if (controller != null && item.getMedia() != null && controller.getMedia() != null
                    && Objects.equals(item.getMedia().getIdentifier(), controller.getMedia().getIdentifier())) {
                controller.seekTo(time);
            } else {
                ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.play_this_to_seek_position,
                        Snackbar.LENGTH_LONG);
            }
        });
        registerForContextMenu(binding.webvDescription);

        binding.imgvCover.setOnClickListener(v -> openPodcast());

        binding.butAction1.setOnClickListener(v -> {
            if (actionButton1 instanceof StreamActionButton && !UserPreferences.isStreamOverDownload()
                    && UsageStatistics.hasSignificantBiasTo(UsageStatistics.ACTION_STREAM)) {
                showOnDemandConfigBalloon(true);
                return;
            } else if (actionButton1 == null) {
                return; // Not loaded yet
            }
            actionButton1.onClick(getContext());
        });
        binding.butAction2.setOnClickListener(v -> {
            if (actionButton2 instanceof DownloadActionButton && UserPreferences.isStreamOverDownload()
                    && UsageStatistics.hasSignificantBiasTo(UsageStatistics.ACTION_DOWNLOAD)) {
                showOnDemandConfigBalloon(false);
                return;
            } else if (actionButton2 == null) {
                return; // Not loaded yet
            }
            actionButton2.onClick(getContext());
        });
        return binding.getRoot();
    }

    private void showOnDemandConfigBalloon(boolean offerStreaming) {
        final boolean isLocaleRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())
                == View.LAYOUT_DIRECTION_RTL;
        final PopupBubbleViewBinding balloonBinding = PopupBubbleViewBinding.inflate(getLayoutInflater());
        final Balloon balloon = new Balloon.Builder(getContext())
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
                .setArrowPosition(0.25f + ((isLocaleRtl ^ offerStreaming) ? 0f : 0.5f))
                .setWidthRatio(1.0f)
                .setMarginLeft(8)
                .setMarginRight(8)
                .setBackgroundColor(ThemeUtils.getColorFromAttr(getContext(), R.attr.colorSecondary))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setLayout(R.layout.popup_bubble_view)
                .setDismissWhenTouchOutside(true)
                .setLifecycleOwner(this)
                .build();
        balloonBinding.balloonMessage.setText(offerStreaming
                ? R.string.on_demand_config_stream_text : R.string.on_demand_config_download_text);
        balloonBinding.balloonButtonPositive.setOnClickListener(v1 -> {
            UserPreferences.setStreamOverDownload(offerStreaming);
            // Update all visible lists to reflect new streaming action button
            EventBus.getDefault().post(new UnreadItemsUpdateEvent());
            ((MainActivity) getActivity()).showSnackbarAbovePlayer(
                    R.string.on_demand_config_setting_changed, Snackbar.LENGTH_SHORT);
            balloon.dismiss();
        });
        balloonBinding.balloonButtonNegative.setOnClickListener(v1 -> {
            UsageStatistics.doNotAskAgain(UsageStatistics.ACTION_STREAM); // Type does not matter. Both are silenced.
            balloon.dismiss();
        });
        balloon.showAlignBottom(binding.butAction1, 0, (int) (-12 * getResources().getDisplayMetrics().density));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        controller = new PlaybackController(getActivity()) {
            @Override
            public void loadMediaInfo() {
                // Do nothing
            }
        };
        controller.init();
        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (itemsLoaded) {
            binding.progbarLoading.setVisibility(View.GONE);
            updateAppearance();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        controller.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (disposable != null) {
            disposable.dispose();
        }
        binding.contentRoot.removeView(binding.webvDescription);
        binding.webvDescription.destroy();
        binding = null;
    }

    private void onFragmentLoaded() {
        if (webviewData != null && !itemsLoaded) {
            binding.webvDescription.loadDataWithBaseURL("https://127.0.0.1",
                    webviewData, "text/html", "utf-8", "about:blank");
        }
        updateAppearance();
    }

    private void updateAppearance() {
        if (item == null) {
            Log.d(TAG, "updateAppearance item is null");
            return;
        }
        binding.txtvPodcast.setText(item.getFeed().getTitle());
        binding.txtvTitle.setText(item.getTitle());

        if (item.getPubDate() != null) {
            String pubDateStr = DateFormatter.formatAbbrev(getActivity(), item.getPubDate());
            binding.txtvPublished.setText(pubDateStr);
            binding.txtvPublished.setContentDescription(DateFormatter.formatForAccessibility(item.getPubDate()));
        }

        RequestOptions options = new RequestOptions()
                .error(R.color.light_gray)
                .transform(new FitCenter(),
                        new RoundedCorners((int) (8 * getResources().getDisplayMetrics().density)))
                .dontAnimate();

        Glide.with(this)
                .load(item.getImageLocation())
                .error(Glide.with(this)
                        .load(ImageResourceUtils.getFallbackImageLocation(item))
                        .apply(options))
                .apply(options)
                .into(binding.imgvCover);
        updateButtons();
    }

    private void updateButtons() {
        binding.circularProgressBar.setVisibility(View.GONE);
        if (item.hasMedia()) {
            if (DownloadServiceInterface.get().isDownloadingEpisode(item.getMedia().getDownload_url())) {
                binding.circularProgressBar.setVisibility(View.VISIBLE);
                binding.circularProgressBar.setPercentage(0.01f * Math.max(1,
                        DownloadServiceInterface.get().getProgress(item.getMedia().getDownload_url())), item);
                binding.circularProgressBar.setIndeterminate(
                        DownloadServiceInterface.get().isEpisodeQueued(item.getMedia().getDownload_url()));
            }
        }

        FeedMedia media = item.getMedia();
        if (media == null) {
            actionButton1 = new MarkAsPlayedActionButton(item);
            actionButton2 = new VisitWebsiteActionButton(item);
            binding.noMediaLabel.setVisibility(View.VISIBLE);
        } else {
            binding.noMediaLabel.setVisibility(View.GONE);
            if (media.getDuration() > 0) {
                binding.txtvDuration.setText(Converter.getDurationStringLong(media.getDuration()));
                binding.txtvDuration.setContentDescription(
                        Converter.getDurationStringLocalized(getContext(), media.getDuration()));
            }
            if (PlaybackStatus.isCurrentlyPlaying(media)) {
                actionButton1 = new PauseActionButton(item);
            } else if (item.getFeed().isLocalFeed()) {
                actionButton1 = new PlayLocalActionButton(item);
            } else if (media.isDownloaded()) {
                actionButton1 = new PlayActionButton(item);
            } else {
                actionButton1 = new StreamActionButton(item);
            }
            if (DownloadServiceInterface.get().isDownloadingEpisode(media.getDownload_url())) {
                actionButton2 = new CancelDownloadActionButton(item);
            } else if (!media.isDownloaded()) {
                actionButton2 = new DownloadActionButton(item);
            } else {
                actionButton2 = new DeleteActionButton(item);
            }
        }

        binding.butAction1Text.setText(actionButton1.getLabel());
        binding.butAction1Text.setTransformationMethod(null);
        binding.butAction1Icon.setImageResource(actionButton1.getDrawable());
        binding.butAction1.setVisibility(actionButton1.getVisibility());

        binding.butAction2Text.setText(actionButton2.getLabel());
        binding.butAction2Text.setTransformationMethod(null);
        binding.butAction2Icon.setImageResource(actionButton2.getDrawable());
        binding.butAction2.setVisibility(actionButton2.getVisibility());
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return binding.webvDescription.onContextItemSelected(item);
    }

    private void openPodcast() {
        if (item == null) {
            return;
        }
        Fragment fragment = FeedItemlistFragment.newInstance(item.getFeedId());
        ((MainActivity) getActivity()).loadChildFragment(fragment);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FeedItemEvent event) {
        Log.d(TAG, "onEventMainThread() called with: " + "event = [" + event + "]");
        for (FeedItem item : event.items) {
            if (this.item.getId() == item.getId()) {
                load();
                return;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EpisodeDownloadEvent event) {
        if (item == null || item.getMedia() == null) {
            return;
        }
        if (!event.getUrls().contains(item.getMedia().getDownload_url())) {
            return;
        }
        if (itemsLoaded && getActivity() != null) {
            updateButtons();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerStatusChanged(PlayerStatusEvent event) {
        updateButtons();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadItemsChanged(UnreadItemsUpdateEvent event) {
        load();
    }

    private void load() {
        if (disposable != null) {
            disposable.dispose();
        }
        if (!itemsLoaded) {
            binding.progbarLoading.setVisibility(View.VISIBLE);
        }
        disposable = Observable.fromCallable(this::loadInBackground)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(result -> {
                binding.progbarLoading.setVisibility(View.GONE);
                item = result;
                onFragmentLoaded();
                itemsLoaded = true;
            }, error -> Log.e(TAG, Log.getStackTraceString(error)));
    }

    @Nullable
    private FeedItem loadInBackground() {
        FeedItem feedItem = DBReader.getFeedItem(itemId);
        Context context = getContext();
        if (feedItem != null && context != null) {
            int duration = feedItem.getMedia() != null ? feedItem.getMedia().getDuration() : Integer.MAX_VALUE;
            DBReader.loadDescriptionOfFeedItem(feedItem);
            ShownotesCleaner t = new ShownotesCleaner(context, feedItem.getDescription(), duration);
            webviewData = t.processShownotes();
        }
        return feedItem;
    }

}
