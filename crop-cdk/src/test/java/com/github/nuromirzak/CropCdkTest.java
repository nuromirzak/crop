package com.github.nuromirzak;

import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.io.IOException;
import java.util.HashMap;

public class CropCdkTest {

    @Test
    public void testStack() throws IOException {
        App app = new App();
        CropCdkStack stack = new CropCdkStack(app, "test");

        Template template = Template.fromStack(stack);

        template.hasResourceProperties("AWS::Lambda::Function", new HashMap<String, Object>() {{
            put("Runtime", "java21");
            put("Handler", "com.github.omirzak.CropLambda::handleRequest");
            put("Timeout", 30);
            put("MemorySize", 1024);
        }});
    }
}
