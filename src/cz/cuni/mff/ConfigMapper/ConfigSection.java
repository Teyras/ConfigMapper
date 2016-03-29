package cz.cuni.mff.ConfigMapper;

/**
 * Created by teyras on 28.3.16.
 */
public @interface ConfigSection {
    String description();

    String name();

    boolean optional();
}
