package callaou.userregistration.logging;

import java.time.Instant;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Logging Aspect
 * AOP logging implementation
 * Logs method entry, exit, execution time, and exceptions
 */
@Component
@Aspect
@Slf4j
public class LoggingAspect {

    /**
     * Around advice for @Loggable annotated methods
     * 
     * @param joinPoint the proceeding join point
     * @param loggable  the loggable
     * @return the result
     * @throws Throwable
     */
    @Around("@annotation(loggable)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Instant startTime = Instant.now();

        if (loggable.logArgs()) {
            Object[] args = joinPoint.getArgs();
            log.debug("{}.{} invoked with arguments: {}", className, methodName, Arrays.toString(args));
        } else {
            log.debug("{}.{} invoked", className, methodName);
        }

        try {
            Object result = joinPoint.proceed();

            long executionTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();

            if (loggable.logTime()) {
                if (loggable.logResult()) {
                    log.debug("{}.{} completed successfully in {}ms with result: {}",
                            className, methodName, executionTime, result);
                } else {
                    log.debug("{}.{} completed successfully in {}ms",
                            className, methodName, executionTime);
                }
            } else {
                if (loggable.logResult()) {
                    log.debug("{}.{} completed with result: {}", className, methodName, result);
                } else {
                    log.debug("{}.{} completed successfully", className, methodName);
                }
            }

            return result;

        } catch (Exception e) {
            long executionTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            log.error("{}.{} failed after {}ms with exception: {}",
                    className, methodName, executionTime, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Before advice for logging method entry
     * 
     * @param joinPoint the join point
     * @param loggable  the loggable
     */
    @Before("@annotation(loggable)")
    public void logMethodEntry(JoinPoint joinPoint, Loggable loggable) {
        if (!loggable.logArgs()) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            log.debug("{}.{} started", className, methodName);
        }
    }

    /**
     * After returning advice for successful method execution
     * 
     * @param joinPoint the join point
     * @param loggable  the loggable
     * @param result    the result
     */
    @AfterReturning(pointcut = "@annotation(loggable)", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Loggable loggable, Object result) {
        if (!loggable.logTime()) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            if (loggable.logResult()) {
                log.debug("{}.{} finished with result: {}", className, methodName, result);
            } else {
                log.debug("{}.{} finished", className, methodName);
            }
        }
    }

    /**
     * After throwing advice for logging exceptions
     * 
     * @param joinPoint the join point
     * @param loggable  the loggable
     * @param exception the exception thrown
     */
    @AfterThrowing(pointcut = "@annotation(loggable)", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Loggable loggable, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        log.error("{}.{} threw exception: {}", className, methodName, exception.getMessage(), exception);
    }
}
