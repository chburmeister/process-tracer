```
mvn clean package
java -DprocessCorrelationId=123 -Dserverport=9123 -jar target/webserver-1.0-SNAPSHOT-jar-with-dependencies.jar
java -DprocessCorrelationId=456 -Dserverport=9456 -jar target/webserver-1.0-SNAPSHOT-jar-with-dependencies.jar
java -DprocessCorrelationId=789 -Dserverport=9789 -jar target/webserver-1.0-SNAPSHOT-jar-with-dependencies.jar
groovy process-tracer-renderer.groovy
firefox traces/index.html
```