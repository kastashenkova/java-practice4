package org.example;

import org.example.annotation.NetworkChannel;
import org.example.annotation.WarehouseAction;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes({
        "org.example.annotation.NetworkChannel",
        "org.example.annotation.WarehouseAction"
})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        boolean isProcessed = false;

        Set<? extends Element> networkElements = roundEnv.getElementsAnnotatedWith(NetworkChannel.class);
        if (!networkElements.isEmpty()) {
            generateSocketRouter(networkElements);
            isProcessed = true;
        }

        Set<? extends Element> warehouseElements = roundEnv.getElementsAnnotatedWith(WarehouseAction.class);
        if (!warehouseElements.isEmpty()) {
            generateWarehouseExecutor(warehouseElements);
            isProcessed = true;
        }

        return isProcessed;
    }

    private void generateSocketRouter(Set<? extends Element> elements) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("org.example.GeneratedSocketRouter");

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println("package org.example;");
                out.println("import org.example.annotation.ConnectionRules;");
                out.println("public class GeneratedSocketRouter {");

                for (Element node : elements) {
                    NetworkChannel annotation = node.getAnnotation(NetworkChannel.class);
                    String channelName = annotation.name();
                    String methodName = "routeTo" + channelName.substring(0, 1).toUpperCase() + channelName.substring(1).toLowerCase();

                    out.println("    @ConnectionRules(timeoutMs = 3000)");
                    out.println("    public void " + methodName + "(SocketWrapper wrapper) {");
                    out.println("        System.out.println(\"Establishing isolated connection to: " + channelName + "\");");
                    out.println("    }");
                }

                out.println("}");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Socket Router", ex);
        }
    }

    private void generateWarehouseExecutor(Set<? extends Element> elements) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile("org.example.GeneratedWarehouseExecutor");

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.println("package org.example;");
                out.println("import org.example.annotation.ValidateParams;");
                out.println("public class GeneratedWarehouseExecutor {");

                for (Element node : elements) {
                    if (node instanceof ExecutableElement methodNode) {
                        String actionName = methodNode.getSimpleName().toString();
                        int argsCount = methodNode.getParameters().size();

                        out.println("    @ValidateParams(minParams = " + argsCount + ")");
                        out.println("    public void execute" + actionName.substring(0, 1).toUpperCase() + actionName.substring(1) + "(String[] inputParams) {");
                        out.println("    }");
                    }
                }

                out.println("}");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate Warehouse Executor", ex);
        }
    }
}
