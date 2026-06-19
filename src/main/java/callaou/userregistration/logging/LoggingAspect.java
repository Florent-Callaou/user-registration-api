package callaou.userregistration.logging;

import java.time.Instant;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
}