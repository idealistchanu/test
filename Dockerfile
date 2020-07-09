FROM adoptopenjdk:11-jdk-hotspot
COPY build/libs/user-management-0.0.1-SNAPSHOT.jar user-management.jar
ARG SPRING_PROFILES_ACTIVE
RUN echo $SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
ENTRYPOINT ["java", "-jar", "/user-management.jar"]