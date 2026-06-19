package callaou.userregistration.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Loggable Annotation
 *           Marks methods for AOP logging with entry, exit, execution time, and
 *           error tracking
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {

    /**
     * Log method arguments
     */
    boolean logArgs()

    default false;

    /**
     * Log method return value
     */
    boolean logResult()

    default false;

    /**
     * Log method execution time
     */
    boolean logTime() default false;
}
