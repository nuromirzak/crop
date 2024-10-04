package com.github.nuromirzak;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.EndpointType;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class CropCdkStack extends Stack {
    public CropCdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CropCdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = new Bucket(this, "MyFirstBucket", BucketProps.builder()
                .versioned(true)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());

        Function cropFunction = new Function(this, "CropFunction", FunctionProps.builder()
                .runtime(Runtime.JAVA_21)
                .handler("com.github.omirzak.CropLambda::handleRequest")
                .code(Code.fromAsset("../crop-lambda/target/crop-lambda.jar"))
                .timeout(Duration.seconds(30))
                .memorySize(1024)
                .environment(Map.of(
                        "BUCKET_NAME", bucket.getBucketName()
                ))
                .build());

        bucket.grantReadWrite(cropFunction);

        cropFunction.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("rekognition:DetectFaces"))
                .resources(List.of("*"))
                .build());

        RestApi api = new RestApi(this, "MyApi", RestApiProps.builder()
                .restApiName("My Service API")
                .description("This service handles multiple operations.")
                .deployOptions(StageOptions.builder()
                        .stageName("prod")
                        .build())
                .endpointTypes(List.of(EndpointType.REGIONAL))
                .build());

        LambdaIntegration cropIntegration = new LambdaIntegration(cropFunction);
        api.getRoot().addResource("crop").addMethod("POST", cropIntegration);

        new CfnOutput(this, "MyApiEndpoint", CfnOutputProps.builder()
                .value(api.getUrl())
                .build());
    }
}
