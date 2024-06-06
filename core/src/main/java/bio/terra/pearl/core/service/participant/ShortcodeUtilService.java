package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Function;

@Service
public class ShortcodeUtilService {
    public static final String SHORTCODE_ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final int SHORTCODE_LENGTH = 6;

    private final RandomUtilService randomUtilService;

    public ShortcodeUtilService(RandomUtilService randomUtilService) {
        this.randomUtilService = randomUtilService;
    }

    /**
     * Generate a unique shortcode. The shortcode is generated by concatenating the prefix with a random string.
     * It's possible there are snazzier ways to get postgres to generate this for us,
     * but for now, just keep trying strings until we get a unique one.
     * returns null if we couldn't generate one.
     */
    @Transactional
    public <T> String generateShortcode(String prefix, Function<String, Optional<T>> findOneByShortcode) {
        int MAX_TRIES = 10;
        String shortcode = null;
        for (int tryNum = 0; tryNum < MAX_TRIES; tryNum++) {
            String possibleShortcode = randomUtilService.generateSecureRandomString(SHORTCODE_LENGTH, SHORTCODE_ALLOWED_CHARS);
            if (prefix != null && !prefix.isEmpty()) {
                possibleShortcode = prefix + "_" + possibleShortcode;
            }
            if (findOneByShortcode.apply(possibleShortcode).isEmpty()) {
                shortcode = possibleShortcode;
                break;
            }
        }
        if (shortcode == null) {
            throw new InternalServerException("Unable to generate unique shortcode");
        }
        return shortcode;
    }
}

