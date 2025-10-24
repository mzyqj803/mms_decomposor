# Transaction Nesting Deadlock Fix - 2025-10-24

## Problem Discovery

### Log Analysis
```log
2025-10-24 04:52:25 - Contract status updated to: COMPLETED
2025-10-24 04:53:15 - Lock wait timeout exceeded (50 seconds later)
```

### Root Cause
**Transaction Nesting Deadlock** caused by:

1. **Outer Transaction**: `ContractsServiceImpl.startBreakdown()` had `@Transactional` annotation
   - Started a transaction at the beginning
   - Updated contract status to `PROCESSING`
   - Held a row lock on the `contracts` table

2. **Inner Transaction**: `BreakdownServiceImpl.updateContractStatusToCompleted()` with `@Transactional(propagation = REQUIRES_NEW)`
   - Attempted to start a new independent transaction
   - Tried to update the SAME contract row
   - **Blocked** waiting for the outer transaction to release the lock

3. **Result**: 
   - Inner transaction waited 50 seconds (MariaDB default `innodb_lock_wait_timeout`)
   - Threw `Lock wait timeout exceeded` exception
   - Contract breakdown failed despite successful completion

### Detailed Stack Trace Analysis
```java
at BreakdownServiceImpl$$SpringCGLIB$$0.updateContractStatusToCompleted(<generated>)
at BreakdownServiceImpl.breakdownContract(BreakdownServiceImpl.java:327)
...
at ContractsServiceImpl.lambda$startBreakdown$4(ContractsServiceImpl.java:278)
at DistributedLockService.executeWithLock(DistributedLockService.java:57)
at ContractsServiceImpl.startBreakdown(ContractsServiceImpl.java:264)  ← @Transactional (OUTER)
```

The outer `@Transactional` at `startBreakdown()` prevented the inner `REQUIRES_NEW` transaction from acquiring the lock.

## Solution

### Code Change
**File**: `src/main/java/com/mms/service/impl/ContractsServiceImpl.java`

**Before**:
```java
@Override
@Transactional  // ❌ This held locks throughout the entire breakdown
public Map<String, Object> startBreakdown(Long contractId) {
    return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
        contract.setStatus(PROCESSING);
        contractsRepository.save(contract);  // Lock acquired here
        
        // Inner method tries to update same row → DEADLOCK!
        Map<String, Object> result = breakdownService.breakdownContract(contractId);
        // ... outer transaction not committed yet
    });
}
```

**After**:
```java
@Override
// ✅ Removed @Transactional - let each operation use independent transactions
public Map<String, Object> startBreakdown(Long contractId) {
    return distributedLockService.executeWithLock(lockKey, 10, 30, TimeUnit.SECONDS, () -> {
        contract.setStatus(PROCESSING);
        contractsRepository.save(contract);  // Independent transaction 1
        
        try {
            // Independent transactions 2+ (including status update to COMPLETED)
            return breakdownService.breakdownContract(contractId);
        } catch (Exception e) {
            contract.setStatus(ERROR);
            contractsRepository.save(contract);  // Independent transaction N
            throw e;
        }
    });
}
```

### Why This Fix Works

1. **No Outer Transaction**: Removing `@Transactional` from `startBreakdown()` means:
   - No long-lived transaction holding locks
   - Each database operation completes and releases locks immediately

2. **Independent Operations**:
   - Update to `PROCESSING` → commits immediately
   - Breakdown process → uses its own transactions
   - Update to `COMPLETED` → runs in `REQUIRES_NEW` transaction without conflict
   - Update to `ERROR` (if needed) → runs independently

3. **Distributed Lock Protection**: The `distributedLockService.executeWithLock()` still ensures:
   - Only one breakdown per contract at a time
   - No concurrent modifications
   - But without holding database locks unnecessarily

## Testing Results

### Build and Deploy
```bash
mvn clean package -DskipTests
docker-compose down
docker-compose build --no-cache backend
docker-compose up -d
```

### Expected Behavior After Fix
✅ No more 50-second waits
✅ Contract status updates successfully
✅ No "Lock wait timeout exceeded" errors
✅ Proper transaction isolation with `REQUIRES_NEW`

## Related Fixes

This fix is part of a series of optimizations to the breakdown process:

1. **Initial Optimization**: Sequential deletion of breakdown records before parallel processing
2. **Connection Pool**: Added HikariCP configuration for better connection management
3. **Concurrent Updates**: Removed individual container status updates, added batch update
4. **Spring AOP**: Made methods `public` and used proxy calls for `REQUIRES_NEW` propagation
5. **Transaction Nesting** (this fix): Removed outer `@Transactional` to prevent deadlock

## Key Learnings

1. **`@Transactional` Placement Matters**: 
   - Don't wrap long-running operations in a single transaction
   - Be careful with nested transactions, especially when updating the same records

2. **`REQUIRES_NEW` Doesn't Always Work**:
   - The outer transaction can still hold locks
   - Inner transaction will wait if trying to access the same rows

3. **Distributed Lock ≠ Database Lock**:
   - Redis distributed lock prevents concurrent execution
   - But doesn't prevent database-level lock conflicts
   - Each level serves a different purpose

4. **Timeout Values**:
   - MariaDB default `innodb_lock_wait_timeout` = 50 seconds
   - This matches the observed wait time in logs

## Monitoring Points

After deploying, monitor for:
- ✅ Breakdown completion time (should be < 5 seconds for 31 containers)
- ✅ No PessimisticLockingFailureException
- ✅ Contract status correctly updated to COMPLETED
- ✅ HikariCP connection pool stats (leak detection, active connections)

## References
- Spring Transaction Propagation: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html
- MariaDB InnoDB Lock Wait Timeout: https://mariadb.com/kb/en/innodb-system-variables/#innodb_lock_wait_timeout
