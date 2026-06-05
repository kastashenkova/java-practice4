package org.example.util;

import org.example.annotation.simple.RetryOnFailure;
import java.lang.reflect.Method;

public class RetryInvoker {

    public static Object invoke(Object target, String methodName, Object... args) throws Throwable {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }

        Method method = target.getClass().getMethod(methodName, paramTypes);

        if (method.isAnnotationPresent(RetryOnFailure.class)) {
            RetryOnFailure policy = method.getAnnotation(RetryOnFailure.class);
            int attempts = policy.attempts();
            long delay = policy.delayMs();

            Throwable lastException = null;
            for (int i = 0; i < attempts; i++) {
                try {
                    return method.invoke(target, args);
                } catch (Exception e) {
                    lastException = e.getCause() != null ? e.getCause() : e;
                    System.err.println("Attempt " + (i + 1) + " failed: " + lastException.getMessage());
                    if (i < attempts - 1) {
                        Thread.sleep(delay);
                    }
                }
            }
            throw lastException;
        }

        return method.invoke(target, args);
    }
}
