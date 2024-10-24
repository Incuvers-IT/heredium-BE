FROM openjdk:11 AS heredium-backend
RUN mkdir /app
WORKDIR /app
ENV TZ="Asia/Ho_Chi_Minh"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY /build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=local", "-jar","/app/app.jar"]
