# 🎲 Test Data Generator

[![GitHub](https://img.shields.io/badge/GitHub-ostbergjohan%2Ftestdatagen-black)](https://github.com/ostbergjohan/testdatagen)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green)](https://spring.io/projects/spring-boot)

Generate realistic Swedish test data including valid personnummer (personal identity numbers) with Luhn algorithm validation. Perfect for performance testing, development, and QA environments.

## 📋 Table of Contents

- [Features](#-features)
- [Quick Start](#-quick-start)
- [API Documentation](#-api-documentation)
- [Endpoints](#-endpoints)
  - [Generate Random Persons (CSV)](#generate-random-persons-csv)
  - [Generate Random Person (JSON)](#generate-random-person-json)
  - [Generate Random UUIDs](#generate-random-uuids)
  - [Execute SQL Query](#execute-sql-query)
- [Swedish Personnummer](#-swedish-personnummer)
- [Data Fields](#-data-fields)
- [Configuration](#%EF%B8%8F-configuration)
- [Deployment](#-deployment)

## ✨ Features

- 🇸🇪 **Valid Swedish Personnummer** - Generated with correct Luhn algorithm validation
- ✅ **Correct Age Formatting** - Properly handles 100+ year old persons with `+` delimiter
- 📊 **Multiple Formats** - CSV and JSON output formats
- 🎯 **Realistic Data** - Names, addresses, phone numbers, emails, job titles
- 🔢 **Bulk Generation** - Generate up to 25,000 persons at once
- 📍 **Real Swedish Postal Codes** - Uses actual Swedish postal codes and areas
- 🆔 **UUID Generation** - Generate unique identifiers for testing
- 🗄️ **SQL Execution** - Execute queries for database testing
- 📚 **OpenAPI Documentation** - Interactive Swagger UI

## 🚀 Quick Start

### Run with Docker

```bash
docker run -d \
  -p 8080:8080 \
  --name testdatagen \
  testdatagen:latest
```

### Run Locally

```bash
mvn spring-boot:run
```

### Generate Test Data

**CSV Format (multiple persons):**
```bash
curl "http://localhost:8080/RandomPerson?antal=100" -o testdata.csv
```

**JSON Format (single person):**
```bash
curl "http://localhost:8080/RandomPersonJson"
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/healthcheck`

## 🎯 Endpoints

### Generate Random Persons (CSV)

Generate multiple Swedish persons with complete profiles in CSV format.

**Endpoint:** `GET /RandomPerson`

**Parameters:**

| Parameter | Type | Required | Range | Description |
|-----------|------|----------|-------|-------------|
| `antal` | String | ✅ Yes | 1-25000 | Number of persons to generate |

**Example Request:**
```bash
curl "http://localhost:8080/RandomPerson?antal=1000" -o persons.csv
```

**Example Response (CSV):**
```csv
Personnummer;longPersonnummer;namn;efterNamn;Address;postAdress;zip;telefon;mobil;jobPosition;jobTitel;email
591125-3291;195911253291;Lennart;Larsson;Övre Fabriksvägen 20;Halland;22969;917603114;0735074106;Designer;District Marketing Planner;sjxxic394769@kihtij249753.com
690705-8645;196907058645;Hans;Änglund;Lennarts Väg 24;Skåne;64984;457837500;0731807980;Consultant;Internal Education Consultant;hyyjfj880250@ooxlmk992126.com
241015+1234;192410151234;Astrid;Bergström;Storgatan 45;Stockholm;11122;812345678;0701234567;Engineer;Senior Software Engineer;abcdef123456@ghijkl789012.com
```

**Features:**
- ✅ Age range: 17-70 years old
- ✅ Valid personnummer with Luhn algorithm
- ✅ Correctly formatted with `-` or `+` based on age
- ✅ Real Swedish postal codes and areas
- ✅ UTF-8 BOM for Excel compatibility

---

### Generate Random Person (JSON)

Generate a single Swedish person with complete profile in JSON format.

**Endpoint:** `GET /RandomPersonJson`

**Parameters:** None

**Example Request:**
```bash
curl "http://localhost:8080/RandomPersonJson"
```

**Example Response (JSON):**
```json
{
  "Personnummer": "850709-1232",
  "longPersonnummer": "198507091232",
  "namn": "Erik",
  "efterNamn": "Andersson",
  "Address": "Storgatan 12",
  "postAdress": "Stockholm",
  "zip": "11122",
  "telefon": "0812345678",
  "mobil": "0701234567",
  "jobPosition": "Developer",
  "jobTitel": "Senior Software Developer",
  "email": "abcdef123456@ghijkl789012.com",
  "kommun": "Stockholms kommun"
}
```

---

### Generate Random UUIDs

Generate multiple random UUIDs for test data correlation.

**Endpoint:** `GET /RandomUUID`

**Parameters:**

| Parameter | Type | Required | Range | Description |
|-----------|------|----------|-------|-------------|
| `antal` | String | ✅ Yes | 1-50000 | Number of UUIDs to generate |

**Example Request:**
```bash
curl "http://localhost:8080/RandomUUID?antal=5"
```

**Example Response:**
```
f03f3dde-be25-4459-9221-f8740406068b
e1d80578-c9dd-4e8d-90dd-ece6af423a0a
a7b2c3d4-e5f6-4789-a012-b3c4d5e6f789
9f8e7d6c-5b4a-3210-fedc-ba9876543210
1a2b3c4d-5e6f-7890-a1b2-c3d4e5f6a7b8
```

---

### Execute SQL Query

Execute SQL queries against databases and return results in CSV format.

> ⚠️ **Warning:** This endpoint executes raw SQL queries. Use only in test environments with proper security controls.

**Endpoint:** `POST /SQL`

**Request Body:**
```json
{
  "jdbc": "jdbc:mysql://localhost:3306/testdb",
  "sql": "SELECT * FROM users LIMIT 10",
  "user": "dbuser",
  "password": "dbpass"
}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/SQL" \
  -H "Content-Type: application/json" \
  -d '{
    "jdbc": "jdbc:mysql://localhost:3306/testdb",
    "sql": "SELECT id, name, email FROM users LIMIT 5",
    "user": "testuser",
    "password": "testpass"
  }'
```

**Example Response (CSV):**
```csv
ID;NAME;EMAIL
1;John Doe;john@example.com
2;Jane Smith;jane@example.com
3;Bob Johnson;bob@example.com
```

**Supported Databases:**
- MySQL
- PostgreSQL
- Oracle
- SQL Server
- SQLite

---

## 🇸🇪 Swedish Personnummer

### What is a Personnummer?

A Swedish personnummer is a unique personal identity number with the format:

**`YYMMDD-XXXX`** or **`YYYYMMDD-XXXX`**

### Structure

- **YYMMDD/YYYYMMDD**: Date of birth (year-month-day)
- **XXX**: Serial number (second-to-last digit indicates gender)
  - **Odd**: Male
  - **Even**: Female
- **X**: Control digit (calculated using Luhn algorithm)

### Age-Based Delimiter

**CRITICAL:** The delimiter indicates age, not century:

| Delimiter | Age | Example |
|-----------|-----|---------|
| `-` (dash) | Under 100 years | `850709-1232` (39 years old in 2024) |
| `+` (plus) | 100 years or older | `241015+1234` (100 years old in 2024) |

This application **correctly** implements this format, including the `+` delimiter for centenarians.

### Luhn Algorithm

The control digit is calculated using the Luhn algorithm:

1. **Take all digits except the control digit**
2. **Multiply every other digit by 2** (from right to left)
3. **If a product > 9, add its digits** (e.g., 14 → 1 + 4 = 5)
4. **Sum all digits**
5. **Round up to nearest 10**
6. **Control digit = (rounded value - sum)**

#### Example Calculation

For **`850709-123X`**:

```
Digits:     8  5  0  7  0  9  1  2  3
Multiply:   ×2    ×2    ×2    ×2    ×2
Result:    16  5  0 14  0 18  2  2  6
Sum:      (1+6)+5+0+(1+4)+0+(1+8)+2+2+6 = 36

Round up to 40
Control digit: 40 - 36 = 4

Final: 850709-1234
```

### Example Personnummer Formats

| Birth Year | Current Age | Short Format | Long Format |
|------------|-------------|--------------|-------------|
| 1924 | 100 | `241015+1234` | `19241015123` |
| 1950 | 74 | `501015-5678` | `19501015567` |
| 1985 | 39 | `850709-1232` | `19850709123` |
| 2005 | 19 | `051201-9876` | `20051201987` |

---

## 📋 Data Fields

Each generated person includes the following fields:

| Field | Description | Example |
|-------|-------------|---------|
| `Personnummer` | Short format with delimiter | `850709-1232` |
| `longPersonnummer` | Long format (12 digits) | `198507091232` |
| `namn` | First name (Swedish) | `Erik` |
| `efterNamn` | Last name (Swedish) | `Andersson` |
| `Address` | Street address | `Storgatan 12` |
| `postAdress` | Postal area/region | `Stockholm` |
| `zip` | Postal code | `11122` |
| `telefon` | Landline phone | `0812345678` |
| `mobil` | Mobile phone | `0701234567` |
| `jobPosition` | Job position | `Developer` |
| `jobTitel` | Job title | `Senior Software Developer` |
| `email` | Random email address | `abc123@def456.com` |
| `kommun` | Municipality (JSON only) | `Stockholms kommun` |

---

## 📍 Postal Codes and Addresses

### Source

Postal codes and areas are loaded from `src/main/resources/static/postnummer.csv`.

### Format

```csv
ZIP_CODE;POSTAL_AREA;MUNICIPALITY
293 93;Gränum;Olofströms kommun
293 94;Olofström;Olofströms kommun
294 07;Sölvesborg;Sölvesborgs kommun
```

### Coverage

The file contains **authentic Swedish postal codes** covering all municipalities and regions in Sweden.

---

## ⚙️ Configuration

### Application Properties

Create `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=8080

# Logging
logging.level.root=INFO
logging.level.com.testdatagen=DEBUG

# Spring configuration
spring.main.allow-bean-definition-overriding=false

```

### Required Resources

Ensure `postnummer.csv` exists in `src/main/resources/static/`:

```
src/
└── main/
    └── resources/
        └── static/
            └── postnummer.csv
```

---

## 🐳 Deployment

### Docker

#### Dockerfile

```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### Build and Run

```bash
# Build JAR
mvn clean package

# Build Docker image
docker build -t testdatagen:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  --name testdatagen \
  --restart unless-stopped \
  testdatagen:latest
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: testdatagen
spec:
  replicas: 2
  selector:
    matchLabels:
      app: testdatagen
  template:
    metadata:
      labels:
        app: testdatagen
    spec:
      containers:
      - name: testdatagen
        image: testdatagen:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /healthcheck
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /healthcheck
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: testdatagen
spec:
  selector:
    app: testdatagen
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
```

---

## 🔄 CI/CD Integration

### Jenkins Pipeline

```groovy
pipeline {
    agent any
    
    environment {
        TESTDATAGEN_URL = 'http://testdatagen:8080'
    }
    
    stages {
        stage('Generate Test Data') {
            steps {
                script {
                    // Generate 1000 test persons
                    sh """
                        curl "${TESTDATAGEN_URL}/RandomPerson?antal=1000" \
                        -o test-data/persons.csv
                    """
                    
                    // Generate UUIDs for test correlation
                    sh """
                        curl "${TESTDATAGEN_URL}/RandomUUID?antal=100" \
                        -o test-data/uuids.txt
                    """
                }
            }
        }
        
        stage('Use Test Data') {
            steps {
                // Your test execution here
                sh 'neoload run --test-data test-data/persons.csv'
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'test-data/**', allowEmptyArchive: true
        }
    }
}
```

### GitLab CI

```yaml
stages:
  - prepare
  - test

generate_testdata:
  stage: prepare
  script:
    - curl "http://testdatagen:8080/RandomPerson?antal=5000" -o testdata.csv
    - curl "http://testdatagen:8080/RandomUUID?antal=500" -o uuids.txt
  artifacts:
    paths:
      - testdata.csv
      - uuids.txt
    expire_in: 1 day

performance_test:
  stage: test
  dependencies:
    - generate_testdata
  script:
    - jmeter -n -t test.jmx -Jtestdata=testdata.csv -l results.jtl
```

---

## 📊 Usage Examples

### Example 1: Load Test with NeoLoad

```yaml
name: load_test_with_swedish_data

variables:
- file:
    name: swedish_persons
    path: testdata.csv
    is_first_line_column_names: true
    delimiter: ';'
    change_policy: each_iteration
    scope: local

user_paths:
- name: test_userpath
  actions:
    steps:
      - transaction:
          name: create_user
          steps:
            - request:
                url: https://api.example.com/users
                method: POST
                body: |
                  {
                    "personnummer": "${swedish_persons.Personnummer}",
                    "name": "${swedish_persons.namn} ${swedish_persons.efterNamn}",
                    "email": "${swedish_persons.email}",
                    "phone": "${swedish_persons.mobil}"
                  }
```

### Example 2: Database Seeding

```bash
#!/bin/bash

# Generate 10,000 test persons
curl "http://localhost:8080/RandomPerson?antal=10000" -o persons.csv

# Import to database
mysql -u root -p testdb << EOF
LOAD DATA LOCAL INFILE 'persons.csv'
INTO TABLE test_users
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(personnummer, long_personnummer, first_name, last_name, 
 address, postal_area, zip, phone, mobile, job_position, 
 job_title, email);
EOF

echo "Imported 10,000 test persons to database"
```

### Example 3: JMeter CSV Data Set

```xml
<CSVDataSet guiclass="TestBeanGUI" testclass="CSVDataSet">
  <stringProp name="filename">testdata.csv</stringProp>
  <stringProp name="fileEncoding">UTF-8</stringProp>
  <stringProp name="delimiter">;</stringProp>
  <boolProp name="recycle">true</boolProp>
  <boolProp name="stopThread">false</boolProp>
  <stringProp name="shareMode">shareMode.all</stringProp>
</CSVDataSet>
```

---

## 🏥 Health Check

Verify the service is running:

```bash
curl http://localhost:8080/healthcheck
```

**Response:**
```json
{
  "status": "ok",
  "service": "API Health Check"
}
```

---

## 📖 Additional Resources

- 📚 [Interactive API Documentation (Swagger UI)](http://localhost:8080/swagger-ui/index.html)
- 📄 [OpenAPI JSON Specification](http://localhost:8080/v3/api-docs)
- 🐙 [GitHub Repository](https://github.com/ostbergjohan/testdatagen)
- 🇸🇪 [Swedish Personal Identity Numbers (Wikipedia)](https://en.wikipedia.org/wiki/Personal_identity_number_(Sweden))
- 🔢 [Luhn Algorithm](https://en.wikipedia.org/wiki/Luhn_algorithm)
- 📖 [Java Faker Library](https://github.com/DiUS/java-faker)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is licensed under the Apache License 2.0.

---

## 🔖 Version Information

- **Version**: 1.0
- **Java Version**: 21
- **Spring Boot**: 3.3.4
- **OpenAPI**: 3.0

---

## ⚠️ Important Notes

### Personnummer Formatting

This application correctly implements the Swedish personnummer format:
- **`-` (dash)**: Person under 100 years old
- **`+` (plus)**: Person 100 years or older

Many systems incorrectly assume `-` means 1900s and `+` means 2000s. This is **wrong**. The delimiter is based on **age**, not century.

### Luhn Validation

All generated personnummer are validated using the Luhn algorithm, ensuring they are mathematically valid according to Swedish standards.

### Test Data Only

This application generates **fake test data** for development and testing purposes. The personnummer, while mathematically valid, are randomly generated and do not correspond to real people.

---

Made with ❤️ for Swedish performance engineers and testers