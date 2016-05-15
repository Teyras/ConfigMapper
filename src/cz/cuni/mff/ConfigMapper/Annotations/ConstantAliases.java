package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A holder for {@link ConstantAlias} annotations
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConstantAliases {
	ConstantAlias[] value();
}
