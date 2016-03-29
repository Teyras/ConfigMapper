package cz.cuni.mff.ConfigMapper;

import java.io.*;

/**
 * Created by teyras on 28.3.16.
 */
public class ConfigMapper<MappedObject> {
    public enum LoadingMode {
        STRICT,
        RELAXED
    }

    private Class<MappedObject> cls;

    public ConfigMapper(Class<MappedObject> cls) {
        this.cls = cls;
    }

    public MappedObject load(InputStream stream, LoadingMode mode) {
        try {
            return cls.newInstance();
        } catch (java.lang.InstantiationException e) {

        } catch (IllegalAccessException e) {

        }

        return null;
    }

    public MappedObject load(InputStream input)
    {
        return load(input, LoadingMode.STRICT);
    }

    public MappedObject load(File file, LoadingMode mode) throws FileNotFoundException {
        return load(new FileInputStream(file), mode);
    }

    public MappedObject load(File file) throws FileNotFoundException {
        return load(file, LoadingMode.STRICT);
    }

    public void save(MappedObject object, OutputStream output) {

    }

    public void save(MappedObject object, File file) throws FileNotFoundException {
        save(object, new FileOutputStream(file));
    }

    public void saveDefaults(OutputStream output) {

    }

    public void saveDefaults(File file) throws FileNotFoundException {
        saveDefaults(new FileOutputStream(file));
    }
}
