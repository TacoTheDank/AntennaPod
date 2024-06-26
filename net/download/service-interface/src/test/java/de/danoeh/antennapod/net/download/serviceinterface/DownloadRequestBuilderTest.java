package de.danoeh.antennapod.net.download.serviceinterface;

import android.os.Bundle;
import android.os.Parcel;

import de.danoeh.antennapod.model.download.DownloadRequest;
import de.danoeh.antennapod.model.feed.FeedMedia;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(RobolectricTestRunner.class)
public class DownloadRequestBuilderTest {

    @Test
    public void parcelInArrayListTest_WithAuth() {
        doTestParcelInArrayList("case has authentication",
                "usr1", "pass1", "usr2", "pass2");
    }

    @Test
    public void parcelInArrayListTest_NoAuth() {
        doTestParcelInArrayList("case no authentication",
                null, null, null, null);
    }

    @Test
    public void parcelInArrayListTest_MixAuth() {
        doTestParcelInArrayList("case mixed authentication",
                null, null, "usr2", "pass2");
    }

    @Test
    public void downloadRequestTestEquals() {
        String destStr = "file://location/media.mp3";
        String username = "testUser";
        String password = "testPassword";
        FeedMedia item = createFeedItem(1);
        DownloadRequest request1 = new DownloadRequestBuilder(destStr, item)
                .withAuthentication(username, password)
                .build();

        DownloadRequest request2 = new DownloadRequestBuilder(destStr, item)
                .withAuthentication(username, password)
                .build();

        DownloadRequest request3 = new DownloadRequestBuilder(destStr, item)
                .withAuthentication("diffUsername", "diffPassword")
                .build();

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
    }

    // Test to ensure parcel using put/getParcelableArrayList() API work
    // based on: https://stackoverflow.com/a/13507191
    private void doTestParcelInArrayList(String message,
                                         String username1, String password1,
                                         String username2, String password2) {
        ArrayList<DownloadRequest> toParcel;
        { // test DownloadRequests to parcel
            String destStr = "file://location/media.mp3";
            FeedMedia item1 = createFeedItem(1);
            DownloadRequest request1 = new DownloadRequestBuilder(destStr, item1)
                    .withAuthentication(username1, password1)
                    .build();

            FeedMedia item2 = createFeedItem(2);
            DownloadRequest request2 = new DownloadRequestBuilder(destStr, item2)
                    .withAuthentication(username2, password2)
                    .build();

            toParcel = new ArrayList<>();
            toParcel.add(request1);
            toParcel.add(request2);
        }

        // parcel the download requests
        Bundle bundleIn = new Bundle();
        bundleIn.putParcelableArrayList("r", toParcel);

        Parcel parcel = Parcel.obtain();
        bundleIn.writeToParcel(parcel, 0);

        Bundle bundleOut = new Bundle();
        bundleOut.setClassLoader(DownloadRequest.class.getClassLoader());
        parcel.setDataPosition(0); // to read the parcel from the beginning.
        bundleOut.readFromParcel(parcel);

        ArrayList<DownloadRequest> fromParcel = bundleOut.getParcelableArrayList("r");

        // spot-check contents to ensure they are the same
        // DownloadRequest.equals() implementation doesn't quite work
        // for DownloadRequest.argument (a Bundle)
        assertEquals(message + " - size", toParcel.size(), fromParcel.size());
        assertEquals(message + " - source", toParcel.get(1).getSource(), fromParcel.get(1).getSource());
        assertEquals(message + " - password", toParcel.get(0).getPassword(), fromParcel.get(0).getPassword());
        assertEquals(message + " - argument", toString(toParcel.get(0).getArguments()),
                toString(fromParcel.get(0).getArguments()));
    }

    private static String toString(Bundle b) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key: b.keySet()) {
            Object val = b.get(key);
            sb.append("(").append(key).append(":").append(val).append(") ");
        }
        sb.append("}");
        return sb.toString();
    }

    private FeedMedia createFeedItem(final int id) {
        // Use mockito would be less verbose, but it'll take extra 1 second for this tiny test
        return new FeedMedia(id, null, 0, 0, 0, "", "", "http://example.com/episode" + id, 0, null, 0, 0);
    }
}
