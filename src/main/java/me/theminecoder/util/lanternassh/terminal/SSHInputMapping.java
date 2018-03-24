package me.theminecoder.util.lanternassh.terminal;

import com.googlecode.lanterna.input.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author theminecoder
 */
class SSHInputMapping implements KeyDecodingProfile {

    public static final SSHInputMapping INSTANCE = new SSHInputMapping();

    private final List<CharacterPattern> PATTERNS = Arrays.asList(new BasicCharacterPattern(new KeyStroke(KeyType.Enter), '\r'));

    private SSHInputMapping() {
    }

    @Override
    public Collection<CharacterPattern> getPatterns() {
        return PATTERNS;
    }

}
