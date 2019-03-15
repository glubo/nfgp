FROM openjdk:8-jre

ENV DEBUG_PORT=8787

COPY docker/docker-entrypoint.sh /
COPY target/nfgp-0.0.1-SNAPSHOT.jar /var/lib/app/nfgp.jar

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["run"]
