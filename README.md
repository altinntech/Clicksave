# Introduce to Clicksave

Versions history: [Changelog](CHANGELOG.md)

## Authors

- Fyodor Plotnikov // Author, Main Developer | [Diveloment](https://github.com/Diveloment)
- Sergey Tsarev // Acting, Team Leader | [Slipcod](https://github.com/Slipcod)
- Anton Volkov // Technical Consultant | [wolches](https://github.com/wolches)

## Description:

Clicksave is a powerful Java library
designed to streamline the process of mapping Java objects to the ClickHouse database.
With Clicksave,
developers can significantly reduce the time spent interacting with the database and managing application objects.

## Key Features:

- Simple and Intuitive API: Clicksave provides developers with a simple and intuitive API for mapping Java objects to ClickHouse tables.

- Flexibility and Customization: The library offers flexibility in configuring object mapping, allowing developers to define mappings between object fields and ClickHouse table columns.

- Efficient Resource Utilization: Clicksave is optimized for efficient interaction with the ClickHouse database, minimizing the application's resource footprint.

- Support for Advanced Data Types: The library supports a wide range of Java and ClickHouse data types, including custom types and arrays.

- Testing and Reliability: Clicksave comes with a set of unit tests, ensuring high reliability and stability in operation.

## How to Install and Configure (equals or above 1.1.9)

1. Create settings.xml and configure it

    - Edit maven settings.xml
      ```bash
      nano ~/.m2/settings.xml
       ```

    - Paste text into file
       ```xml
      <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
       http://maven.apache.org/xsd/settings-1.0.0.xsd">
       
            <mirrors>
                <mirror>
                    <id>my-repository-http-unblocker</id>
                    <mirrorOf>snapshots</mirrorOf>
                    <name></name>
                    <url>http://217.25.90.14:8081/artifactory/libs-snapshot</url>
                </mirror>
            </mirrors>
       </settings>
       ```

2. Import maven dependency to you project

   Example:
   ```xml
   <repositories>
        <repository>
            <snapshots />
            <id>snapshots</id>
            <name>libs-snapshot</name>
            <url>http://217.25.90.14:8081/artifactory/libs-snapshot</url>
        </repository>
   </repositories>
   
   <dependency>
        <groupId>com.altinntech</groupId>
        <artifactId>clicksave</artifactId>
        <version>1.1.9-EXPERIMENTAL</version>
   </dependency>
   ```

3. Reload pom

4. Add config class

   ```java
       @Configuration
       @InterfaceComponentScan(basePackages = {"path.to.your.repository"})
       @ComponentScan(basePackages = {"com.altinntech.clicksave"})
       public class ClicksaveConfig {

            @Autowired
            Environment environment;

            @Bean
            public CSBootstrap csBootstrap() throws ClassCacheNotFoundException, SQLException {
                DefaultProperties defaultProperties = DefaultProperties.fromEnvironment(environment);
                return new CSBootstrap(defaultProperties);
            }
       }
   ```
5. Add to properties
    ```properties
   spring.main.allow-bean-definition-overriding=true
    ```

## How to Install and Configure (below 1.1.9)

1. Create settings.xml and configure it

   - Create settings.xml
      ```bash
      touch ~/.m2/settings.xml
      ```

   - Edit settings.xml
     ```bash
     nano ~/.m2/settings.xml
      ```

   - Paste text into file
      ```xml
     <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
      http://maven.apache.org/xsd/settings-1.0.0.xsd">
      
        <activeProfiles>
          <activeProfile>github</activeProfile>
        </activeProfiles>
      
        <profiles>
          <profile>
            <id>github</id>
            <repositories>
              <repository>
                <id>central</id>
                <url>https://repo1.maven.org/maven2</url>
              </repository>
              <repository>
                <id>github</id>
                <url>https://maven.pkg.github.com/altinntech/Clicksave</url>
                <snapshots>
                  <enabled>true</enabled>
                </snapshots>
              </repository>
            </repositories>
          </profile>
        </profiles>
      
        <servers>
          <server>
            <id>github</id>
            <username>Diveloment</username>
            <password>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</password>
          </server>
        </servers>
      </settings>
      ```

2. Import maven dependency to you project

   [Get maven package here](https://github.com/altinntech/Clicksave/packages/2084734)
   
   Example:
   ```xml
   <repositories>
        <repository>
            <id>github</id>
            <name>GitHub Packages</name>
            <url>https://maven.pkg.github.com/altinntech/Clicksave</url>
        </repository>
   </repositories>
   
   <dependency>
    <groupId>com.altinntech</groupId>
    <artifactId>clicksave</artifactId>
    <version>1.1.3</version> // set any version
   </dependency>
   ```
   
3. Reload pom

4. Add config class

    ```java
    @InterfaceComponentScan(basePackages = {"path.to.repository.package"})
    @ComponentScan(basePackages = {"com.altinntech.clicksave"})
    public class AppConfiguration {
    }
    ```

## Clicksave Configuration Properties

All properties:

```properties
clicksave.connection.datasource.url=jdbc:clickhouse://localhost:8123/default
clicksave.connection.datasource.username=username
clicksave.connection.datasource.password=password
clicksave.connection.pool.initial-size=20
clicksave.connection.pool.refill-threshold=5
clicksave.connection.pool.max-size=50
clicksave.connection.pool.allow-expansion=true
clicksave.core.root-package=
```

### ClickHouse Database Connection Settings

- **clicksave.connection.datasource.url**:
    - Description: The URL for connecting to the ClickHouse database.
    - Example value: `jdbc:clickhouse://localhost:8123/default`

- **clicksave.connection.datasource.username**:
    - Description: The username for authentication when connecting to the ClickHouse database.
    - Example value: `username`

- **clicksave.connection.datasource.password**:
    - Description: The password for authentication when connecting to the ClickHouse database.
    - Example value: `password`

### Connection Pool Settings

- **clicksave.connection.pool.initial-size**:
    - Description: The initial size of the connection pool to the ClickHouse database.
    - Example value: `20`

- **clicksave.connection.pool.refill-threshold**:
    - Description: The threshold for automatically replenishing connections in the pool.
    - Example value: `5`

- **clicksave.connection.pool.max-size**:
    - Description: The maximum size of the connection pool to the ClickHouse database.
    - Example value: `50`

- **clicksave.connection.pool.allow-expansion**:
    - Description: Permission to expand the connection pool as needed.
    - Example value: `true`

### Core Clicksave Settings

- **clicksave.core.root-package**:
    - Description: The root package where Clicksave will search for classes to map.
    - Example value: `com.example.models`

### Handling Small and Large Values

When setting up the connection pool configuration for the ClickHouse database, it's crucial to consider the impact of using either too small or too large values for the configuration parameters:

#### Small Values:

1. **clicksave.connection.pool.initial-size**: Setting the initial size of the pool too small may lead to connection shortages during application startup. This can result in delays and errors when executing queries to the database.

2. **clicksave.connection.pool.refill-threshold**: If the refill threshold is set too low, the connection pool may not replenish in time, causing connection shortages.

#### Large Values:

1. **clicksave.connection.pool.max-size**: A significantly large maximum pool size may lead to excessive consumption of database server and application resources. It can overload the server and increase query response times.

In general, optimal values for these parameters depend on the application workload, available resources, and database characteristics. It's recommended to conduct testing and optimization to ensure stable and efficient application performance.

## How to Use

1. Create a new entity class

    ```java
    public class Person {
    
        // entity class must have a no arguments constructor
        public Person() {
        }
        
        UUID id;
        String name;
        String lastName;
    }
    ```
   - Ensure that your entity class has a no-arguments constructor as required by JPA specifications.
   - Define the fields of your entity class.

2. Mark all required items with annotations

    ```java
    @ClickHouseEntity // you should use this annotation for persistence entity
    @Batching(batchSize = 10) // add batch for saving
    public class Person {

    // entity class must have a no arguments constructor
    public Person() {
    }

    // it is recommended to make the id field a UUID type
    @Column(value = FieldType.UUID, id = true)
    UUID id;
    @Column(FieldType.STRING)
    String name;
    @Column(FieldType.STRING)
    String lastName;
    }
    ```
   - Annotate your entity class with @ClickHouseEntity to mark it as a persistence entity.
   - Use @Batching annotation to specify a batch size for saving multiple entities at once.
   - Annotate the fields of your entity class with @Column to specify their types.
   - Supported id field types: UUID, Long, Integer
   - Not supported primitive types

3. Make JPA repository for entity

   ```java
   @ClickHouseRepository // repository interface must be marked with this annotation
   public interface JpaPersonRepository extends ClickHouseJpa<Person, UUID> { 
   
   Optional<Person> findByName(String name); // findBy always returns the Optional<T>
   }
   ```
   - Define a repository interface for your entity and annotate it with @ClickHouseRepository.
   - Extend ClickHouseJpa interface, specifying the entity class (Person) and the type of its primary key (UUID).
   - Define repository methods as needed. For example, findByName method retrieves a person by their name.
   
4. Enjoy

   ```java
   private final JpaPersonRepository jpaPersonRepository;

   @Autowired
   public PersonService(JpaPersonRepository jpaPersonRepository) {
        this.jpaPersonRepository = jpaPersonRepository;
   }
   
   public void save(Person person) {
        // saving a new entity is fast, but updating an entity takes a long time
        jpaPersonRepository.save(person);
   }
   ```
   - Inject the repository into your service or controller.
   - Use repository methods to perform CRUD (Create, Read, Update, Delete) operations on your entities.