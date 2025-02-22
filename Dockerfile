# 1. Base image
FROM maven:3.8.7-eclipse-temurin-17 AS build

# 2. Loyihani yuklash
WORKDIR /app
COPY . /app

# 3. Build qilish
RUN mvn clean install -DskipTests

# 4. JAR faylini final image ga o'tkazish
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar /app/bot.jar

# 5. Ishga tushirish
ENTRYPOINT ["java", "-jar", "/app/bot.jar"]
