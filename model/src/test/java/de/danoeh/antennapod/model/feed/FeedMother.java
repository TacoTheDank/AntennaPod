package de.danoeh.antennapod.model.feed;

public class FeedMother {
    public static final String IMAGE_URL = "http://example.com/image";

    public static Feed anyFeed() {
        return new Feed(0, null, "title", "http://example.com", "This is the description",
                "http://example.com/payment", "Daniel", "en", null, "http://example.com/feed", IMAGE_URL,
                null, "http://example.com/feed", System.currentTimeMillis());
    }

}
