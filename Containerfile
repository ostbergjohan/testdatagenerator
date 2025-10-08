FROM registry.access.redhat.com/ubi9/openjdk-21 AS builder

# Switch to root user for building
USER root

# Install Maven
RUN microdnf install -y maven && \
    microdnf clean all

# Set working directory for build
WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code (includes resources with CSV file)
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM registry.access.redhat.com/ubi9/openjdk-21

# Switch to root user to adjust permissions
USER root

# Ensure /var/cache/yum directory exists and has proper permissions
RUN mkdir -p /var/cache/yum && \
    chmod -R 777 /var/cache/yum

# Remove any socket files from yum metadata that cause issues during build
RUN find /var/cache/yum/metadata -type s -delete || true

# Install dnf and update all installed packages
RUN microdnf install -y dnf && \
    dnf update -y && \
    dnf update -y libxml2 openssl openssl-libs rsync freetype krb5-libs && \
    dnf clean all

# Remove the entire yum cache directory to eliminate any socket files that cause build errors
RUN rm -rf /var/cache/yum

# Set the working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Switch back to non-root user
USER 1001

EXPOSE 8080

# Run the Spring Boot application
CMD ["java", "-Dspring.output.ansi.enabled=always", "-Dsun.stdout.encoding=UTF-8", "-jar", "app.jar"]