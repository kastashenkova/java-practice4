package org.example;

import java.net.Socket;
import org.example.processing.SharedQueue;
import org.example.util.RetryInvoker;
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

        try {
            SharedQueue<byte[]> queue = new SharedQueue<>();
            Socket socket = new Socket("localhost", 8077);
            SocketWrapper wrapper = new SocketWrapper(socket, queue);

            byte[] data = "test-packet".getBytes();

            boolean success = (boolean) RetryInvoker.invoke(wrapper, "sendPackage", data);
            System.out.println("Result: " + success);

        } catch (Throwable t) {
            System.err.println("All retry attempts failed. Operation aborted: " + t.getMessage());
        }
    }
}
