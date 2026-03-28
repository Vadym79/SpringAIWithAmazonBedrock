FROM amazoncorretto:21


ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /opt/aws-opentelemetry-agent.jar

ENV JAVA_TOOL_OPTIONS=-javaagent:/opt/aws-opentelemetry-agent.jar \
AGENT_OBSERVABILITY_ENABLED=true \
OTEL_RESOURCE_ATTRIBUTES=service.name=spring_mcp_conference_search_mcp_server,aws.log.group.names=/aws/bedrock-agentcore/runtimes/spring_mcp_conference_search_mcp_server-B6d3WzFoGa \
OTEL_EXPORTER_OTLP_LOGS_HEADERS=x-aws-log-group=/aws/bedrock-agentcore/runtimes/spring_mcp_conference_search_mcp_server-B6d3WzFoGa,x-aws-log-stream=runtime-logs,x-aws-metric-namespace=bedrock-agentcore \
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf \
OTEL_EXPORTER_OTLP_TRACES_PROTOCOL=http/protobuf \
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://xray.us-east-1.amazonaws.com/v1/traces \
OTEL_EXPORTER_OTLP_LOGS_PROTOCOL=http/protobuf \
OTEL_LOGS_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://logs.us-east-1.amazonaws.com/v1/logs 

COPY target/spring-ai-1.1-conference-search-bedrock-agentcore-runtime-mcp-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]