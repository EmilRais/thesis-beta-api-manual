FROM jboss/wildfly
EXPOSE 8080

COPY target/beta-api.war /opt/jboss/wildfly/standalone/deployments/beta-api.war
