package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A holder for {@link ConstantAlias} annotations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstantAliases {
	ConstantAlias[] value();
}
