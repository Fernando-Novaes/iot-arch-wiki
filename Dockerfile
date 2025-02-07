FROM eclipse-temurin:21-jre
# Copying the application
COPY target/*.jar iot-arch-wiki.jar
#Copying the H2 database file - the database nust be copyed to that directory
COPY  database/save_db/docker_db/archiotdb.mv.db /iot-arch-wiki/database/
COPY  database/save_db/docker_db/archiotdb.trace.db /iot-arch-wiki/database/
# Defina o diretório de trabalho dentro do contêiner
WORKDIR /iot-arch-wiki
EXPOSE 36519
ENTRYPOINT ["java", "-jar", "/iot-arch-wiki.jar"]

#docker build -t iot-arch-wiki
#docker tag iot-arch-wiki fernandonrs/iot-arch-wiki
#docker run -d -p 36519:36519 iot-arch-wiki