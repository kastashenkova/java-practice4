package org.example.validator;

import org.example.annotation.ValidateParams;

import java.lang.reflect.Method;

// using reflection
public class AnnotationValidator {

    public static void validate(Class<?> clazz, String[] params) {

        for (Method method : clazz.getDeclaredMethods()) {

            if (method.isAnnotationPresent(ValidateParams.class)) {

                ValidateParams annotation =
                        method.getAnnotation(ValidateParams.class);

                if (params.length < annotation.minParams()) {
                    throw new IllegalArgumentException(
                            "Not enough parameters");
                }
            }

            System.out.println("Validation passed for " + method.getName());
        }
    }
}
