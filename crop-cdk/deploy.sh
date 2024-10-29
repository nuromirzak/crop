cd ../crop-lambda || exit 1
mvn clean package

cd ../crop-cdk || exit 1
cdk deploy
