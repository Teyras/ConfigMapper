package cz.cuni.mff.ConfigMapper;

/**
 * Created by teyras on 28.3.16.
 */
public @interface ConfigOption {
    String description() default "";

    String section() default "";

    String name() default "";

    int max() default Integer.MAX_VALUE;

    int min() default Integer.MIN_VALUE;
}
