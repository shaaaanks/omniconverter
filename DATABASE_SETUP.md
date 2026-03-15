DATABASE SETUP DOCUMENTATION
=============================

## Database Architecture

The OmniConverter app uses Room Database for persistent storage of conversion history.

### Components

1. **ConversionEntity.java** (Room Entity)
   - Represents a single conversion record
   - Fields:
     - id (auto-generated primary key)
     - inputUri (source file URI)
     - outputUri (destination file URI)
     - type (converter type, e.g., "IMAGE", "MP4_TO_MP3")
     - status (SUCCESS, FAILED, IN_PROGRESS)
     - message (error/info message)
     - timestamp (conversion timestamp)

2. **ConversionDao.java** (Data Access Object)
   - insert() - Add a new conversion record
   - update() - Update existing record
   - delete() - Delete single record
   - getAll() - Retrieve all conversions ordered by timestamp DESC
   - getById(id) - Get specific conversion by ID
   - getByStatus(status) - Get all conversions with specific status
   - getByType(type) - Get all conversions of a specific type
   - deleteAll() - Clear all records
   - getCount() - Get total number of records

3. **ConversionDatabase.java** (Room Database)
   - Singleton database instance
   - Version: 1 (with exportSchema=false for development)
   - Access via: ConversionDatabase.getDatabase(context)

4. **DatabaseManager.java** (Abstraction Layer)
   - Singleton wrapper around ConversionDatabase
   - Thread-safe CRUD operations
   - Handles database access from any component
   - Methods:
     - insertConversion() - Add record
     - updateConversion() - Update record (runs on background thread)
     - deleteConversion() - Delete record (runs on background thread)
     - getAllConversions() - Fetch all records
     - getConversionById() - Fetch by ID
     - getConversionsByStatus() - Filter by status
     - getConversionsByType() - Filter by type
     - deleteAllConversions() - Clear all records
     - getTotalCount() - Get count

5. **DatabaseTestHelper.java** (Testing Utility)
   - testDatabase() - Verifies basic database operations
   - clearAllRecords() - Clear all records for testing
   - Logs all operations for debugging

### Initialization Flow

1. **OmniConverterApp.onCreate()** is called when the app starts
2. Calls ConversionDatabase.getDatabase(this) to initialize the singleton
3. Optionally calls DatabaseTestHelper.testDatabase() for verification
4. Database is now ready for use throughout the app

### Usage Examples

#### Storing a Conversion Result
```java
ConversionEntity entity = new ConversionEntity(
    inputUri.toString(),
    outputUri.toString(),
    "IMAGE",
    "SUCCESS",
    "Conversion completed",
    System.currentTimeMillis()
);
DatabaseManager.getInstance(context).insertConversion(entity);
```

#### Retrieving Conversion History
```java
List<ConversionEntity> allConversions = 
    DatabaseManager.getInstance(context).getAllConversions();
```

#### Filter by Type
```java
List<ConversionEntity> imageConversions = 
    DatabaseManager.getInstance(context).getConversionsByType("IMAGE");
```

#### Filter by Status
```java
List<ConversionEntity> successfulConversions = 
    DatabaseManager.getInstance(context).getConversionsByStatus("SUCCESS");
```

### WorkManager Integration

ConversionWorker automatically stores conversion results to the database:
1. Executes conversion synchronously on background thread
2. Creates ConversionEntity from result
3. Inserts into database via DatabaseManager
4. Returns Result.success() or Result.failure()

### HistoryFragment Integration

HistoryFragment loads and displays conversion history:
1. Calls DatabaseManager.getInstance().getAllConversions() on background thread
2. Passes results to HistoryAdapter
3. Refreshes on onResume() to show latest results

### Testing

Enable database testing by ensuring OmniConverterApp calls:
```java
DatabaseTestHelper.testDatabase(this);
```

This will:
- Create a test record
- Retrieve all records
- Log record details
- Log total count
- Verify database operations work correctly

Look for logs tagged "DatabaseTest" in Android Logcat.

### Best Practices

1. Always use DatabaseManager, not direct DAO access
2. Run insert/update/delete operations on background threads
3. Handle exceptions gracefully
4. Clear old records periodically to save storage
5. Use appropriate query methods (getByStatus, getByType) instead of filtering in Java

### Future Enhancements

- Add migration support for schema changes (version > 1)
- Implement background cleanup for old records (>30 days)
- Add search/filter UI for conversion history
- Export history as CSV/JSON
- Add backup/restore functionality
