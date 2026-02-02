# Maven-Nexus Integration - Project Specific Guide

## Project Overview
**Location:** `/home/hesham/ecommerce-microservices`
**Nexus URL:** `http://localhost:8081`
**Services:** 6 microservices

## Nexus Repositories Used
| Repository | URL | Purpose |
|------------|-----|---------|
| maven-releases | http://localhost:8081/repository/maven-releases/ | Release artifacts |
| maven-snapshots | http://localhost:8081/repository/maven-snapshots/ | Snapshot artifacts |
| maven-public | http://localhost:8081/repository/maven-public/ | Dependency resolution |

## Configuration Files

### 1. Maven Authentication: `~/.m2/settings.xml`

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <mirrors>
    <mirror>
      <id>nexus-public</id>
      <mirrorOf>*</mirrorOf>
      <url>http://localhost:8081/repository/maven-public/</url>
    </mirror>
  </mirrors>

  <servers>
    <server>
      <id>nexus-releases</id>
      <username>admin</username>
      <password>123456</password>
    </server>
    <server>
      <id>nexus-snapshots</id>
      <username>admin</username>
      <password>123456</password>
    </server>
    <server>
      <id>nexus-public</id>
      <username>admin</username>
      <password>123456</password>
    </server>
  </servers>
</settings>
```

### 2. Service POM Configuration

Each service POM has these sections added:

```xml
    <distributionManagement>
        <repository>
            <id>nexus-releases</id>
            <url>http://localhost:8081/repository/maven-releases/</url>
        </repository>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <url>http://localhost:8081/repository/maven-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>

    <repositories>
        <repository>
            <id>nexus-public</id>
            <url>http://localhost:8081/repository/maven-public/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <id>nexus-public</id>
            <url>http://localhost:8081/repository/maven-public/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
```

**Services configured:**
- `backend/user-service/pom.xml`
- `backend/product-service/pom.xml`
- `backend/media-service/pom.xml`
- `backend/order-service/pom.xml`
- `backend/eureka-service-discovery/pom.xml`
- `backend/apigateway/pom.xml`

## How to Deploy to Nexus

### Deploy Single Service

Navigate to the service directory and run:

```bash
cd /home/hesham/ecommerce-microservices/backend/user-service
mvn deploy
```

**Example output:**
```
[INFO] Uploading to nexus-snapshots: http://localhost:8081/repository/maven-snapshots/com/sayedhesham/userservice/0.0.1-SNAPSHOT/userservice-0.0.1-20260201.193629-1.jar
[INFO] Uploaded to nexus-snapshots: ... (45.2 MB)
[INFO] BUILD SUCCESS
```

### Deploy Multiple Services

```bash
cd /home/hesham/ecommerce-microservices/backend/user-service && mvn deploy
cd /home/hesham/ecommerce-microservices/backend/product-service && mvn deploy
cd /home/hesham/ecommerce-microservices/backend/media-service && mvn deploy
cd /home/hesham/ecommerce-microservices/backend/order-service && mvn deploy
cd /home/hesham/ecommerce-microservices/backend/eureka-service-discovery && mvn deploy
cd /home/hesham/ecommerce-microservices/backend/apigateway && mvn deploy
```

## Verification in Nexus

1. Open: http://localhost:8081
2. Login: `admin` / `123456`
3. Navigate: **Browse** → **maven-snapshots**
4. Browse to artifact:
   ```
   com/sayedhesham/userservice/0.0.1-SNAPSHOT/
   ```
5. Verify files:
   - `userservice-0.0.1-SNAPSHOT.pom`
   - `userservice-0.0.1-SNAPSHOT.jar`
   - `maven-metadata.xml`

## Version Information

Current versions in project:

| Service | Artifact ID | Version | Repository |
|---------|--------------|---------|------------|
| user-service | userservice | 0.0.1-SNAPSHOT | maven-snapshots |
| product-service | productservice | 0.0.1-SNAPSHOT | maven-snapshots |
| media-service | mediaservice | 0.0.1-SNAPSHOT | maven-snapshots |
| order-service | orderservice | 0.0.1-SNAPSHOT | maven-snapshots |
| eureka-service-discovery | eurekaserver | 0.0.1-SNAPSHOT | maven-snapshots |
| apigateway | apigateway | 0.0.1-SNAPSHOT | maven-snapshots |

All services initially used `-SNAPSHOT` versions, deployed to `maven-snapshots` repository.

## Version Management - First Release

### Version Strategy

**Approach:** First official release from snapshot to stable version

| Type | Old Version | New Version | Repository |
|------|-------------|-------------|------------|
| Parent POM | 1.0.0 | 2.0.0 | maven-releases |
| All Services | 0.0.1-SNAPSHOT | 1.0.0 | maven-releases |

**Services updated:**
- user-service
- product-service
- media-service
- order-service
- eureka-service-discovery
- apigateway

### Step 1: Update Parent POM

File: `backend/pom.xml` (line 10)

**Before:**
```xml
<version>1.0.0</version>
```

**After:**
```xml
<version>2.0.0</version>
```

### Step 2: Update All Service POMs

Update version in each service POM (line 13):

**user-service/pom.xml, product-service/pom.xml, media-service/pom.xml, order-service/pom.xml, eureka-service-discovery/pom.xml, apigateway/pom.xml**

**Before:**
```xml
<version>0.0.1-SNAPSHOT</version>
```

**After:**
```xml
<version>1.0.0</version>
```

### Step 3: Deploy New Versions to Nexus

```bash
cd /home/hesham/ecommerce-microservices/backend

# Build all services
./mvnw clean package

# Deploy all services to Nexus
./mvnw deploy
```

**Expected output:**
```
[INFO] Uploading to nexus-releases: http://localhost:8081/repository/maven-releases/com/sayedhesham/userservice/1.0.0/userservice-1.0.0.jar
[INFO] Uploaded to nexus-releases: ... (45.2 MB)
[INFO] BUILD SUCCESS
```

### Step 4: Verify Versions in Nexus UI

1. Open: http://localhost:8081
2. Login: `admin` / `123456`
3. Navigate: **Browse** → **maven-releases**
4. Browse to each service:
   - `com/sayedhesham/userservice/1.0.0/`
   - `com/sayedhesham/productservice/1.0.0/`
   - `com/sayedhesham/mediaservice/1.0.0/`
   - `com/sayedhesham/orderservice/1.0.0/`
   - `com/sayedhesham/eurekaserver/1.0.0/`
   - `com/sayedhesham/apigateway/1.0.0/`
5. View `maven-metadata.xml` - shows both versions:
   ```xml
   <versions>
     <version>0.0.1-SNAPSHOT</version>
     <version>1.0.0</version>
   </versions>
   ```

### Step 5: Test Version Retrieval

**Create test project** (`backend/test-version-retrieval/pom.xml`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.sayedhesham</groupId>
    <artifactId>test-version-retrieval</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.sayedhesham</groupId>
            <artifactId>userservice</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>nexus-public</id>
            <url>http://localhost:8081/repository/maven-public/</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
        </repository>
    </repositories>
</project>
```

**Test dependency resolution:**
```bash
cd /home/hesham/ecommerce-microservices/backend/test-version-retrieval
mvn dependency:resolve
```

**Expected output:**
```
[INFO] Downloading from nexus-public: http://localhost:8081/repository/maven-public/com/sayedhesham/userservice/1.0.0/userservice-1.0.0.pom
[INFO] Downloaded from nexus-public: ... userservice-1.0.0.pom
[INFO] BUILD SUCCESS
```

### Current Version Status

| Service | Version | Repository | Status |
|---------|---------|------------|--------|
| user-service | 1.0.0 | maven-releases | ✅ Deployed |
| product-service | 1.0.0 | maven-releases | ✅ Deployed |
| media-service | 1.0.0 | maven-releases | ✅ Deployed |
| order-service | 1.0.0 | maven-releases | ✅ Deployed |
| eureka-service-discovery | 1.0.0 | maven-releases | ✅ Deployed |
| apigateway | 1.0.0 | maven-releases | ✅ Deployed |

**Note:** Old SNAPSHOT versions (0.0.1-SNAPSHOT) still exist in `maven-snapshots` repository.

### Quick Commands

```bash
# Deploy all services to Nexus
cd backend && ./mvnw clean deploy

# Resolve specific version from Nexus
cd backend/test-version-retrieval && mvn dependency:resolve

# View Maven metadata for an artifact
cd backend/test-version-retrieval && mvn dependency:list
```

## Dependency Management - Nexus Proxy Setup

### Repositories Used for Proxy

| Repository | URL | Purpose |
|------------|-----|---------|
| maven-central | http://localhost:8081/repository/maven-central/ | Proxy to Maven Central (pre-configured) |
| maven-public | http://localhost:8081/repository/maven-public/ | Group: maven-central, maven-releases, maven-snapshots |

### Configure Nexus Public Access in settings.xml

**Required:** Add `nexus-public` server credentials to `~/.m2/settings.xml`

**Before (missing nexus-public):**
```xml
<servers>
  <server>
    <id>nexus-releases</id>
    <username>admin</username>
    <password>123456</password>
  </server>
  <server>
    <id>nexus-snapshots</id>
    <username>admin</username>
    <password>123456</password>
  </server>
</servers>
```

**After (add nexus-public):**
```xml
<servers>
  <server>
    <id>nexus-releases</id>
    <username>admin</username>
    <password>123456</password>
  </server>
  <server>
    <id>nexus-snapshots</id>
    <username>admin</username>
    <password>123456</password>
  </server>
  <server>
    <id>nexus-public</id>
    <username>admin</username>
    <password>123456</password>
  </server>
</servers>
```

**Why:** Without this, Maven gets `401 Unauthorized` when accessing `maven-public` group for dependency resolution.

### Build with Nexus Proxy

```bash
cd /home/hesham/ecommerce-microservices/backend

# Force update to clear cached 401 errors
./mvnw clean package -U
```

**Expected output:**
```
[INFO] Downloading from nexus-public: http://localhost:8081/repository/maven-public/org/springframework/boot/spring-boot-starter-parent/3.5.7/spring-boot-starter-parent-3.5.7.pom
[INFO] Downloaded from nexus-public: http://localhost:8081/repository/maven-public/org/springframework/boot/spring-boot-starter-parent/3.5.7/spring-boot-starter-parent-3.5.7.pom (13 kB at 8.8 kB/s)
```

### Verify Cached Dependencies in Nexus

1. Open: http://localhost:8081
2. Login: `admin` / `123456`
3. Navigate: **Browse** → **maven-central**
4. Browse cached artifacts:
   ```
   org/springframework/boot/spring-boot-starter-parent/3.5.7/
   org/springframework/cloud/spring-cloud-dependencies/2025.0.0/
   ```

### Fix 401 Unauthorized Error

**Error message:**
```
Could not transfer artifact ... from/to nexus-public: status code: 401, reason phrase: Unauthorized (401)
```

**Steps to fix:**
```bash
# 1. Edit settings.xml
nano ~/.m2/settings.xml

# 2. Add nexus-public server entry (see above)

# 3. Clear cached errors
rm -rf ~/.m2/repository/org/springframework

# 4. Rebuild with -U flag
cd backend && ./mvnw clean package -U
```

### Mirror Configuration

**Current `~/.m2/settings.xml` mirror setup:**
```xml
<mirrors>
  <mirror>
    <id>nexus-public</id>
    <mirrorOf>*</mirrorOf>
    <url>http://localhost:8081/repository/maven-public/</url>
  </mirror>
</mirrors>
```

**Result:** All Maven repository requests go through Nexus, no direct access to Maven Central.

## Testing Dependency Resolution

Verify Maven resolves dependencies through Nexus:

```bash
cd /home/hesham/ecommerce-microservices/backend/user-service
mvn dependency:resolve
```

## Quick Commands

```bash
# Deploy user-service
cd backend/user-service && mvn deploy

# Deploy without tests
cd backend/user-service && mvn deploy -DskipTests

# Clean and deploy
cd backend/user-service && mvn clean deploy

# Verify settings.xml
cat ~/.m2/settings.xml
```

## Summary

This setup enables:
- Maven authentication with Nexus using admin/1236
- All 6 services configured for deployment to Nexus
- Dependency resolution through maven-public group
- Snapshot artifacts deployed to maven-snapshots repository
- Release artifacts deployed to maven-releases repository
- Version management with multiple versions stored in Nexus
- External dependencies proxied through maven-central
