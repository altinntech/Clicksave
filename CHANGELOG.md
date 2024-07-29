# Changelog

### [1.1.22-SNAPSHOT] - 2024-07-29
- Added support for UInt32 and UInt64

### [1.1.21-SNAPSHOT] - 2024-07-23
- Added support for UInt8 bool type
- Added auto generated sql migration scripts

### [1.1.20-SNAPSHOT] - 2024-07-12
- Added sync manager allows syncing multiple clicksave instances
- Added waiting for all asynchronous tasks to complete before executing fetch requests
- Fix redundant calls to utility table

### [1.1.19-SNAPSHOT] - 2024-06-13
- Added basic monitoring functionality
- Added batchSave() method for repository

### [1.1.18-SNAPSHOT] - 2024-05-28
- Added health check for connection to db
- Refactor code to DI style

### [1.1.17-SNAPSHOT] - 2024-05-07
- Added batch automatic saving
- Added max processors property
- Added max queue size property
- Some code optimizations improvements

### [1.1.16-SNAPSHOT] - 2024-04-26
- Added asyncSave method (~12000 e/s)
- Added count() method

### [1.1.15 - Experimental] - 2024-04-16
- Added entity functions like @PrePersist, @PreUpdate and @PostLoad

### [1.1.14 - Experimental] - 2024-04-12
- New deleteAll method

### [1.1.13 - Experimental] - 2024-04-12
- Added support for LocalDate
- Fix error when convert sql.Date to java time

### [1.1.12 - Experimental] - 2024-04-10
- Fix nulls params in query

### [1.1.11 - Experimental] - 2024-04-10
- Fix query params overload exception

### [1.1.10 - Experimental] - 2024-04-08
- Add support for custom replaceable queries

### [1.1.9 - Experimental] - 2024-04-08
- Add support for aggregate functions
- Added indexes
- Added VersionedCollapsingMergeTree engine (IN PROGRESS)
- Downgrade to java 17
- Fix table creation
- Fix id fields counting

### [1.1.8] - 2024-03-14
- Fix ConnectionManager memory leaks
- Fix properties resolver error

### [1.1.7] - 2024-03-13
- Return dependencies

### [1.1.6] - 2024-03-12
- Disable Clicksave if not resolve properties
- Improved table updater
- Reduce dependencies
- Fixed bug: that caused the application to terminate incorrectly

### [1.1.5] - 2024-03-05
- Add database source replacement in runtime (for test containers)

### [1.1.4] - 2024-03-04
- Added support for LocalDateTime
- Added support for Boolean

### [1.1.3] - 2024-03-01
- Fix creation of a clicksave_sequence table

### [1.1.2] - 2024-03-01
- Added support for Lob

### [1.1.1] - 2024-02-29
- Added support for Embedded entities
- Fix clicksave_sequence table

### [1.1.0] - 2024-02-28
- Added auto increment id
- Added support for BigDecimal

### [1.0.1] - 2024-02-21
- Renamed configuration file

### [1.0.0] - 2024-02-20
- Initial release