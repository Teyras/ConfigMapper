package cz.cuni.mff.ConfigMapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by teyras on 28.3.16.
 */
public interface ConfigAdapter {
    ConfigNode read(InputStream input);

    void write(ConfigNode config, OutputStream output);
}
