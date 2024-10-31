package gisbis.org.geotoolseditfeaturetest;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.UUID;

import static java.util.Objects.nonNull;

public class UUIDUtil {

     /** sequence UUID v 7 */
    public static UUID sqUUID ( ) {
        long nano = System.nanoTime();
        nano = nano - (nano / 1000) * 1000;

        long mostSigBits = (Clock.systemUTC().millis() << 16) | (0x7000 + Long.parseLong(""+nano, 16));

        long leastSigBits = getSrvSign();
        byte[] randomBytes = new byte[6];
        getSecureRandom().nextBytes(randomBytes);
        for ( byte b : randomBytes )
            leastSigBits = (leastSigBits << 8) | (b & 0xff);

        return new UUID(mostSigBits, leastSigBits);
    }

    private static SecureRandom secureRandom;

    private static SecureRandom getSecureRandom ( ) {
        if ( secureRandom == null )
            secureRandom = new SecureRandom();

        return secureRandom;
    }

    private static final String SERVER_ID_SETTING_NAME = "SERVER_ID";
    private static final long DEFAULT_SERVER_ID = 0x6bd5;

    private static Long serverSign = null;

    private static long getSrvSign ( ) {
        if ( serverSign == null ) {
            Long serverId = null;
            String serverIdStr = StringUtils.trimToNull(System.getenv(SERVER_ID_SETTING_NAME));
            if ( StringUtils.length(serverIdStr) == 4 )
                try {
                    serverId = Long.parseLong(serverIdStr, 16);
                } catch ( Exception e ) {
                    e.printStackTrace();
                }

            if ( serverId == null )
                serverId = DEFAULT_SERVER_ID;

            serverSign = serverId;
        }

        return serverSign;
    }
}
