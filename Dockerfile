FROM eclipse-temurin:21-jre
# Copying the application
COPY target/*.jar iot-arch-wiki.jar
#Copying the H2 database file - the database nust be copyed to that directory
COPY  database/archiotdb.mv.db /iot-arch-wiki/database/
COPY  database/archiotdb.trace.db /iot-arch-wiki/database/
# Defina o diretório de trabalho dentro do contêiner
WORKDIR /iot-arch-wiki
EXPOSE 36519
ENTRYPOINT ["java", "-jar", "/iot-arch-wiki.jar"]