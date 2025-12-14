FROM amazoncorretto:21


ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /opt/aws-opentelemetry-agent.jar

ENV JAVA_TOOL_OPTIONS=-javaagent:/opt/aws-opentelemetry-agent.jar \
OTEL_METRICS_EXPORTER=none \
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=http/protobuf \
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://xray.us-east-1.amazonaws.com/v1/traces \
OTEL_LOGS_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_LOGS_PROTOCOL=http/protobuf \
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://logs.us-east-1.amazonaws.com/v1/logs \
OTEL_EXPORTER_OTLP_LOGS_HEADERS=x-aws-log-group=/aws/bedrock-agentcore/runtimes/agentcore_runtime_spring_ai_demo-tD7f1W6RGi,x-aws-log-stream=runtime-logs,x-aws-metric-namespace=bedrock-agentcore \ 
OTEL_RESOURCE_ATTRIBUTES=service.name=agentcore_runtime_spring_ai_demo,aws.log.group.names=/aws/bedrock-agentcore/runtimes/agentcore_runtime_spring_ai_demo-tD7f1W6RGi

COPY target/spring-ai-agent-demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]n