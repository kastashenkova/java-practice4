package org.example.validator;

import org.example.annotation.ConnectionRules;
import java.lang.reflect.Method;

// using reflection
public class ConnectionValidator {

    public static void validate(Object router) {
        Class<?> clazz = router.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ConnectionRules.class)) {
                ConnectionRules rules = method.getAnnotation(ConnectionRules.class);

                int timeout = rules.timeoutMs();

                if (timeout < 1000) {
                    throw new IllegalStateException("Timeout is too low for safe data transmission: " + timeout + "ms");
                }

                System.out.println("Validation passed for " + method.getName());
            }
        }
    }
}
