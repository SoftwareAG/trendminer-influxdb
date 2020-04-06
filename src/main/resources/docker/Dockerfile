FROM adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.6_10
RUN mkdir -p /user/share/app/bin
ADD /target/${project.build.finalName}.${project.packaging} /user/share/bin/app.jar
WORKDIR /user/share
ENTRYPOINT ["/opt/java/openjdk/bin/java", "-jar", "bin/app.jar"]
