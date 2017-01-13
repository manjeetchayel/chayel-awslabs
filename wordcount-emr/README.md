WordCount example for Amazon EMR using maven

- Build the maven jar using 
`maven clean install`

1. Upload the JAR to your S3 bucket
2. Using CUSTOM_JAR step on EMR cluster submit
http://docs.aws.amazon.com/emr/latest/ReleaseGuide/emr-launch-custom-jar-cli.html
3. Usage

hadoop jar WordCount.jar s3://<your-bucket>/input/  s3://<your-bucket>/output

 
