package org.example;

import org.example.validator.AnnotationValidator;
import org.example.validator.ConnectionValidator;

public class Main {

    public static void main(String[] args) {

        String[] params = {"rice", "buckwheat"};
        AnnotationValidator.validate(
                GeneratedWarehouseExecutor.class,
                params
        );

        GeneratedSocketRouter metricsRouter = new GeneratedSocketRouter();
        ConnectionValidator.validate(metricsRouter);
    }
}
