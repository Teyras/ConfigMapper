package cz.cuni.mff.ConfigMapper;

/**
 * Created by teyras on 28.3.16.
 */
public @interface ConfigOption {
    String description();

    String section();

    String name();

    int max();

    int min();
}
