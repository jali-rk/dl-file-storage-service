FROM amazoncorretto:21-alpine

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the application (skip tests for faster builds, run tests separately)
RUN ./gradlew build -x test

# Extract the built JAR
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*-SNAPSHOT.jar)

# Production stage
FROM amazoncorretto:21-alpine

WORKDIR /app

# Copy the extracted application from build stage
COPY --from=0 /app/build/dependency/BOOT-INF/lib /app/lib
COPY --from=0 /app/build/dependency/META-INF /app/META-INF
COPY --from=0 /app/build/dependency/BOOT-INF/classes /app

# Expose port
EXPOSE 8901

# Run the application
ENTRYPOINT ["java", "-cp", "/app:/app/lib/*", "com.dopaminelite.dl_file_storage_service.DlFileStorageServiceApplication"]

