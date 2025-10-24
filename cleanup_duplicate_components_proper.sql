-- Cleanup duplicate component_code records properly
-- Problem: TTA0E104002~AA79375 and TTA0E104007~AA79375 have duplicate IDs
-- Keep: 30925, 30926
-- Delete: 30930, 30932

START TRANSACTION;

-- Step 1: Delete duplicate spec records for components to be deleted
-- Since they have the same specs, we can safely delete the duplicate specs
DELETE FROM components_spec WHERE Component_ID IN (30930, 30932);

-- Step 2: Update container_components_breakdown foreign key references
UPDATE container_components_breakdown 
SET Sub_Component_ID = 30925 
WHERE Sub_Component_ID = 30930;

UPDATE container_components_breakdown 
SET Sub_Component_ID = 30926 
WHERE Sub_Component_ID = 30932;

-- Step 3: Delete duplicate component records
DELETE FROM components WHERE ID IN (30930, 30932);

-- Step 4: Add unique constraint on Component_Code to prevent future duplicates
ALTER TABLE components ADD UNIQUE KEY uk_component_code (Component_Code);

COMMIT;

-- Verification queries
SELECT '=== Verification: No more duplicate component_codes ===' as status;
SELECT Component_Code, COUNT(*) as count 
FROM components 
GROUP BY Component_Code 
HAVING count > 1;

SELECT '=== Verification: Unique constraint exists ===' as status;
SELECT CONSTRAINT_NAME, COLUMN_NAME 
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'mms_db' 
  AND TABLE_NAME = 'components' 
  AND CONSTRAINT_NAME = 'uk_component_code';

SELECT '=== Verification: Cleanup completed successfully ===' as status;

