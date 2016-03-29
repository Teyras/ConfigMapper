package cz.cuni.mff.ConfigMapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by teyras on 28.3.16.
 */
public class IniAdapter implements ConfigAdapter {
    @Override
    public ConfigNode read(InputStream input) {
        return null;
    }

    @Override
    public void write(ConfigNode config, OutputStream output) {

    }
}
