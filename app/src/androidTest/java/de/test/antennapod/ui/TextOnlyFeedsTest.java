package de.test.antennapod.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.model.feed.Feed;
import de.test.antennapod.EspressoTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static de.test.antennapod.EspressoTestUtils.onDrawerItem;
import static de.test.antennapod.EspressoTestUtils.openNavDrawer;
import static de.test.antennapod.EspressoTestUtils.waitForView;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

/**
 * Test UI for feeds that do not have media files
 */
@RunWith(AndroidJUnit4.class)
public class TextOnlyFeedsTest {

    private UITestUtils uiTestUtils;

    @Before
    public void setUp() throws IOException {
        EspressoTestUtils.clearPreferences();
        EspressoTestUtils.clearDatabase();

        uiTestUtils = new UITestUtils(InstrumentationRegistry.getInstrumentation().getTargetContext());
        uiTestUtils.setHostTextOnlyFeeds(true);
        uiTestUtils.setup();

        ActivityScenario.launch(MainActivity.class);
        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        uiTestUtils.tearDown();
        Intents.release();
    }

    @Test
    public void testMarkAsPlayedList() throws Exception {
        uiTestUtils.addLocalFeedData(false);
        final Feed feed = uiTestUtils.hostedFeeds.get(0);
        openNavDrawer();
        onView(withId(R.id.nav_list)).perform(swipeUp());
        onDrawerItem(withText(feed.getTitle())).perform(click());
        onView(withText(feed.getItemAtIndex(0).getTitle())).perform(click());
        onView(isRoot()).perform(waitForView(withText(R.string.mark_read_no_media_label), 3000));
        onView(withText(R.string.mark_read_no_media_label)).perform(click());
        onView(isRoot()).perform(waitForView(allOf(withText(R.string.mark_read_no_media_label), not(isDisplayed())), 3000));
    }

}
