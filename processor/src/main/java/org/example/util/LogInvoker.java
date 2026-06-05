package org.example.util;

import org.example.annotation.simple.LogExecutionTime;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LogInvoker {

    public static Object invoke(Object target, String methodName, Object... args) throws Throwable {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Class<?> type = args[i].getClass();

            if (type == Integer.class) {
                type = int.class;
            } else if (type == Double.class) {
                type = double.class;
            } else if (type == Boolean.class) {
                type = boolean.class;
            } else if (type == Long.class) {
                type = long.class;
            }

            paramTypes[i] = type;
        }

        Method method = target.getClass().getMethod(methodName, paramTypes);

        if (method.isAnnotationPresent(LogExecutionTime.class)) {
            LogExecutionTime logConfig = method.getAnnotation(LogExecutionTime.class);

            if (logConfig.logArguments()) {
                System.out.println("[LOG] Arguments passed to " + methodName + ": " + Arrays.toString(args));
            }

            long startTime = System.currentTimeMillis();
            Object result = method.invoke(target, args);
            long endTime = System.currentTimeMillis();

            System.out.println("[LOG] Method '" + methodName + "' execution time: " + (endTime - startTime) + " ms");
            return result;
        }

        return method.invoke(target, args);
    }
}
