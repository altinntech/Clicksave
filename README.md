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

## How to Install and Configure

1. Import maven dependency to you project

   Example:
   ```xml
   <repositories>
        <repository>
            <snapshots />
            <id>nexus-server</id>
            <name>maven-snapshots</name>
            <url>https://maven.altinntech.com/repository/maven-snapshots/</url>
        </repository>
    </repositories>
   
   <dependency>
        <groupId>com.altinntech</groupId>
        <artifactId>clicksave</artifactId>
        <version>1.1.16-SNAPSHOT</version>
   </dependency>
   ```

2. Reload pom

3. Add config class

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
4. Add to properties
    ```properties
   spring.main.allow-bean-definition-overriding=true
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
clicksave.core.batch-save-rate=1200
#Clicksave will use all host machine processors
clicksave.core.thread-manager.max-processors=-1
clicksave.core.core.thread-manager.max-queue-size=1000
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

1. **clicksave.connection.pool.max-size**: A significantly larger maximum pool size may lead to excessive consumption of database server and application resources. It can overload the server and increase query response times.

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
    @Batching(batchSize = 100) // add batch for saving
    public class Person {

    // entity class must have a public no arguments constructor
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
   - Annotate your entity class with ```@ClickHouseEntity``` to mark it as a persistence entity.
   - Use @Batching annotation to specify a batch size for saving multiple entities at once.
   - Annotate the fields of your entity class with ```@Column``` to specify their types.
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