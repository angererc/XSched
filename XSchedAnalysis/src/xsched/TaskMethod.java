package xsched;

import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Inherited
@Target({ElementType.METHOD})
public @interface TaskMethod {

}
