package cz.cuni.mff.ConfigMapper;

/**
 * Created by teyras on 28.3.16.
 */
public class ConfigMapper<MappedObject> {

    private Class<MappedObject> cls;

    public ConfigMapper(Class<MappedObject> cls) {
        this.cls = cls;
    }

    public<MappedObject> MappedObject load(ConfigNode config, LoadingMode mode) {
        try {
            return (MappedObject) cls.newInstance();
        } catch (java.lang.InstantiationException e) {

        } catch (IllegalAccessException e) {

        }

        return null;
    }

    public void save(MappedObject object, ConfigNode config) {

    }
}
