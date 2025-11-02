# Deployment Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Docker & Docker Compose (optional)

## Local Development Setup

### 1. Database Setup

Create MySQL databases:

```sql
CREATE DATABASE jira_user_db;
CREATE DATABASE jira_issue_db;

-- Optional: Create dedicated user
CREATE USER 'jira_user'@'localhost' IDENTIFIED BY 'jira_password';
GRANT ALL PRIVILEGES ON jira_user_db.* TO 'jira_user'@'localhost';
GRANT ALL PRIVILEGES ON jira_issue_db.* TO 'jira_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Update Configuration

If using custom database credentials, update `application.yml` in each service:

**user-service/src/main/resources/application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jira_user_db?createDatabaseIfNotExist=true
    username: jira_user
    password: jira_password
```

**issue-service/src/main/resources/application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jira_issue_db?createDatabaseIfNotExist=true
    username: jira_user
    password: jira_password
```

### 3. Build All Services

```bash
cd /Users/chaithu/Downloads/jira-clone
mvn clean install -DskipTests
```

### 4. Start Services

**Terminal 1 - Discovery Service:**
```bash
cd discovery-service
mvn spring-boot:run
```
Wait until you see: `Started DiscoveryServiceApplication`

**Terminal 2 - User Service:**
```bash
cd user-service
mvn spring-boot:run
```

**Terminal 3 - Issue Service:**
```bash
cd issue-service
mvn spring-boot:run
```

**Terminal 4 - API Gateway (optional):**
```bash
cd api-gateway
mvn spring-boot:run
```

### 5. Verify Services

- **Eureka Dashboard:** http://localhost:8761
- **User Service:** http://localhost:8081/actuator/health
- **Issue Service:** http://localhost:8082/actuator/health

## Docker Deployment

### 1. Create Dockerfiles

Create `Dockerfile` in each service directory:

**discovery-service/Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**user-service/Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**issue-service/Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Build JARs

```bash
mvn clean package -DskipTests
```

### 3. Start with Docker Compose

```bash
docker-compose up -d
```

### 4. Check Container Logs

```bash
docker-compose logs -f user-service
docker-compose logs -f issue-service
```

### 5. Stop Services

```bash
docker-compose down
```

To remove volumes:
```bash
docker-compose down -v
```

## Production Deployment

### Security Checklist

1. **Change JWT Secret**
   ```yaml
   jwt:
     secret: <STRONG_RANDOM_SECRET_256_BITS>
   ```

2. **Use Environment Variables**
   ```yaml
   spring:
     datasource:
       url: ${DB_URL}
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
   ```

3. **Enable HTTPS**
   - Configure SSL certificates
   - Update application.yml with keystore settings

4. **Database Security**
   - Use strong passwords
   - Enable SSL/TLS for database connections
   - Restrict database access to application servers only

5. **Enable CORS Properly**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public CorsFilter corsFilter() {
           CorsConfiguration config = new CorsConfiguration();
           config.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
           // ... configure properly
       }
   }
   ```

### Environment Variables

Create `.env` file for production:

```bash
# Database
DB_HOST=production-db-host
DB_USER=jira_prod_user
DB_PASSWORD=strong_password_here

# JWT
JWT_SECRET=your_256_bit_secret_key_here
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Service Discovery
EUREKA_URL=http://discovery-service:8761/eureka/
```

### Kubernetes Deployment (Optional)

**deployment.yaml** for user-service:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: jira-clone/user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
  - port: 8081
    targetPort: 8081
  type: LoadBalancer
```

### Monitoring & Logging

1. **Add Actuator Endpoints**
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: health,metrics,prometheus
   ```

2. **Integrate with Prometheus**
   - Add `micrometer-registry-prometheus` dependency
   - Expose metrics endpoint

3. **Centralized Logging**
   - Use ELK Stack (Elasticsearch, Logstash, Kibana)
   - Or integrate with cloud logging (CloudWatch, Stackdriver)

## Database Migration

For production, use Flyway migrations instead of `hibernate.ddl-auto=update`:

**pom.xml:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**application.yml:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Don't auto-create schemas
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Create migration files in `src/main/resources/db/migration/`:
- `V1__create_users_table.sql`
- `V2__create_organizations_table.sql`

## Scaling Considerations

1. **Horizontal Scaling**
   - Services are stateless and can be scaled horizontally
   - Use load balancer in front of multiple instances

2. **Database**
   - Use read replicas for read-heavy operations
   - Consider database sharding for large datasets

3. **Caching**
   - Implement Redis for session storage and caching
   - Cache frequently accessed data (categories, users, etc.)

4. **Message Queue**
   - Use RabbitMQ or Kafka for async operations
   - Send notifications asynchronously

## Backup Strategy

```bash
# Backup databases
mysqldump -u root -p jira_user_db > backup_user_$(date +%Y%m%d).sql
mysqldump -u root -p jira_issue_db > backup_issue_$(date +%Y%m%d).sql

# Restore
mysql -u root -p jira_user_db < backup_user_20241101.sql
```

## Health Checks

Each service exposes health endpoints:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## Troubleshooting

### Services Not Registering with Eureka

- Check network connectivity
- Verify `eureka.client.service-url.defaultZone` is correct
- Ensure discovery-service started first

### Database Connection Issues

```bash
# Test MySQL connection
mysql -h localhost -u root -p -e "SHOW DATABASES;"

# Check service logs
docker-compose logs user-service | grep -i error
```

### High Memory Usage

Adjust JVM settings:
```bash
java -Xms512m -Xmx1024m -jar app.jar
```

## Performance Tuning

**application.yml:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

## Monitoring URLs

- Eureka Dashboard: `http://localhost:8761`
- User Service Health: `http://localhost:8081/actuator/health`
- Issue Service Health: `http://localhost:8082/actuator/health`
- Prometheus Metrics: `http://localhost:8081/actuator/prometheus`

---

For any deployment issues, refer to logs or create an issue in the repository.
