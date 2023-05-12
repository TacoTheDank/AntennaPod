package de.danoeh.antennapod.core.sync;

import org.apache.commons.lang3.StringUtils;

public class GuidValidator {

    public static boolean isValidGuid(String guid) {
        return StringUtils.isNotBlank(guid)
                && !guid.equals("null");
    }
}

