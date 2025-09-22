# Data Modeling

## Diagram


```mermaid
erDiagram
  COMPONENTS ||--o{ COMPONENTS_RELATIONSHIP : "parent-of/child-of"
  COMPONENTS ||--o{ COMPONENTS_SPEC : "has specs"
  COMPONENTS ||--o{ COMPONENTS_PROCESSES : "has processes"
  CONTRACTS  ||--o{ CONTAINERS : "has containers"
  CONTAINERS ||--o{ CONTAINER_COMPONENTS : "has items"
  CONTRACTS  ||--o{ CONTRACT_PARAMETERS : "has params"
  CONTRACTS  ||--o{ CONTAINERS_COMPONENTS_SUMMARY : "summarizes"
  COMPONENTS ||--o{ CONTAINERS_COMPONENTS_SUMMARY : "summarizes"
  CONTAINERS ||--o{ CONTAINERS_COMPONENTS_SUMMARY : "summarizes"
  CONTAINER_COMPONENTS ||--o{ CONTAINER_COMPONENTS_BREAKDOWN : "breaks into"
  COMPONENTS ||--o{ CONTAINER_COMPONENTS_BREAKDOWN : "sub-components"
  CONTAINERS ||--o{ CONTAINER_COMPONENTS_BREAKDOWN : "by container"

  COMPONENTS {
    int        ID PK
    string(50) Category_Code
    string(50) Component_Code
    string(511) Name
    text       Comment
    boolean    procurement_flag
    boolean    common_parts_flag
    timestamp  Entry_TS
    string(50) Entry_User
    timestamp  Last_Update_TS
    string(50) Last_Update_User
  }

  CONTRACTS {
    int         ID PK
    string(255) Contract_No
    string(511) Client_Name
    string(511) Project_Name
    int         Quantity
    timestamp   Entry_TS
    string(50)  Entry_User
    timestamp   Last_Update_TS
    string(50)  Last_Update_User
  }

  CONTAINERS {
    int         ID PK
    int         Contract_ID FK
    string(255) Container_No
    string(511) Name
    string(50)  Container_Size
    string(50)  Container_Weight
    text        Comments
    timestamp   Entry_TS
    string(50)  Entry_User
    timestamp   Last_Update_TS
    string(50)  Last_Update_User
  }

  CONTAINER_COMPONENTS {
    int         ID PK
    int         Container_ID FK
    string(255) Component_No
    string(511) Component_Name
    string(50)  Unit_Code
    int         Quantity
    text        Comments
    timestamp   Entry_TS
    string(50)  Entry_User
    timestamp   Last_Update_TS
    string(50)  Last_Update_User
  }

  COMPONENTS_SPEC {
    int         ID PK
    int         Component_ID FK
    string(50)  Spec_Code
    string(511) Spec_Value
    text        Comments
    timestamp   Entry_TS
    string(50)  Entry_User
    timestamp   Last_Update_TS
    string(50)  Last_Update_User
  }

  COMPONENTS_RELATIONSHIP {
    int        ID PK
    int        Parent_ID FK
    int        Child_ID FK
    timestamp  Entry_TS
    string(50) Entry_User
    timestamp  Last_Update_TS
    string(50) Last_Update_User
  }

  COMPONENTS_PROCESSES {
    int        ID PK
    int        IDComponent_ID FK
    int        Seq_No
    string(511) Name
    text       Comments
    timestamp  Entry_TS
    string(50) Entry_User
    timestamp  Last_Update_TS
    string(50) Last_Update_User
  }

  CONTRACT_PARAMETERS {
    int         ID PK
    int         Contract_ID FK
    string(255) Param_Name
    string(511) Param_Value
    timestamp   Entry_TS
    string(50)  Entry_User
    timestamp   Last_Update_TS
    string(50)  Last_Update_User
  }

  CONTAINERS_COMPONENTS_SUMMARY {
    int        ID PK
    int        Contract_ID FK
    int        Component_ID FK
    int        Container_ID FK
    int        Quantity
    timestamp  Entry_TS
    string(50) Entry_User
    timestamp  Last_Update_TS
    string(50) Last_Update_User
  }

  CONTAINER_COMPONENTS_BREAKDOWN {
    int        ID PK
    int        Container_Componenet_ID FK
    int        Sub_Component_ID FK
    int        Container_ID FK
    int        Quantity
    timestamp  Entry_TS
    string(50) Entry_User
    timestamp  Last_Update_TS
    string(50) Last_Update_User
  }

```

## DDL

```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Components
CREATE TABLE IF NOT EXISTS Components (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Category_Code     VARCHAR(50),
  Component_Code    VARCHAR(50),
  Name              VARCHAR(511),
  Comment           TEXT,
  procurement_flag  TINYINT(1) DEFAULT 0,
  common_parts_flag TINYINT(1) DEFAULT 0,
  Entry_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS    TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User  VARCHAR(50) DEFAULT 'SYS_USER'
) ENGINE=InnoDB;

-- 2) Contracts
CREATE TABLE IF NOT EXISTS Contracts (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_No       VARCHAR(255),
  Client_Name       VARCHAR(511),
  Project_Name      VARCHAR(511),
  Quantity           INT,
  Entry_TS           TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User         VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS     TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User   VARCHAR(50) DEFAULT 'SYS_USER'
) ENGINE=InnoDB;

-- 3) Containers
CREATE TABLE IF NOT EXISTS Containers (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_ID       INT,
  Container_No      VARCHAR(255),
  Name               VARCHAR(511),
  Container_Size     VARCHAR(50),
  Container_Weight   VARCHAR(50),
  Comments            TEXT,
  Entry_TS             TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User             VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User         VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cont_contract (Contract_ID),
  CONSTRAINT fk_cont_contract
    FOREIGN KEY (Contract_ID) REFERENCES Contracts(ID)
) ENGINE=InnoDB;

-- 4) Container_Components
CREATE TABLE IF NOT EXISTS Container_Components (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Container_ID      INT,
  Component_No      VARCHAR(255),
  Component_Name    VARCHAR(511),
  Unit_Code          VARCHAR(50),
  Quantity             INT,
  Comments               TEXT,
  Entry_TS                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                  VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User               VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cc_container (Container_ID),
  CONSTRAINT fk_cc_container
    FOREIGN KEY (Container_ID) REFERENCES Containers(ID)
) ENGINE=InnoDB;

-- 5) Components_Spec
CREATE TABLE IF NOT EXISTS Components_Spec (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Component_ID      INT,
  Spec_Code          VARCHAR(50),
  Spec_Value          VARCHAR(511),
  Comments              TEXT,
  Entry_TS                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                  VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User               VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cs_component (Component_ID),
  CONSTRAINT fk_cs_component
    FOREIGN KEY (Component_ID) REFERENCES Components(ID)
) ENGINE=InnoDB;

-- 6) Components_Relationship
CREATE TABLE IF NOT EXISTS Components_Relationship (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Parent_ID          INT,
  Child_ID             INT,
  Entry_TS                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                  VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User               VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cr_parent (Parent_ID),
  INDEX idx_cr_child  (Child_ID),
  CONSTRAINT fk_cr_parent FOREIGN KEY (Parent_ID) REFERENCES Components(ID),
  CONSTRAINT fk_cr_child  FOREIGN KEY (Child_ID)  REFERENCES Components(ID)
) ENGINE=InnoDB;

-- 7) Components_processes
CREATE TABLE IF NOT EXISTS Components_processes (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  IDComponent_ID      INT,
  Seq_No                 INT,
  Name                      VARCHAR(511),
  Comments                    TEXT,
  Entry_TS                        TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                     VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cpp_component (IDComponent_ID),
  CONSTRAINT fk_cpp_component
    FOREIGN KEY (IDComponent_ID) REFERENCES Components(ID)
) ENGINE=InnoDB;

-- 8) Contract_Parameters
CREATE TABLE IF NOT EXISTS Contract_Parameters (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_ID         INT,
  Param_Name             VARCHAR(255),
  Param_Value               VARCHAR(511),
  Entry_TS                        TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                     VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cp_contract (Contract_ID),
  CONSTRAINT fk_cp_contract
    FOREIGN KEY (Contract_ID) REFERENCES Contracts(ID)
) ENGINE=InnoDB;

-- 9) Containers_Components_Summary
CREATE TABLE IF NOT EXISTS Containers_Components_Summary (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_ID         INT,
  Component_ID           INT,
  Container_ID               INT,
  Quantity                       INT,
  Entry_TS                            TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                            VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                            TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                             VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_ccs_contract  (Contract_ID),
  INDEX idx_ccs_component (Component_ID),
  INDEX idx_ccs_container (Container_ID),
  CONSTRAINT fk_ccs_contract  FOREIGN KEY (Contract_ID)  REFERENCES Contracts(ID),
  CONSTRAINT fk_ccs_component FOREIGN KEY (Component_ID) REFERENCES Components(ID),
  CONSTRAINT fk_ccs_container FOREIGN KEY (Container_ID) REFERENCES Containers(ID)
) ENGINE=InnoDB;

-- 10) Container_Components_Breakdown
CREATE TABLE IF NOT EXISTS Container_Components_Breakdown (
  ID                         INT PRIMARY KEY AUTO_INCREMENT,
  Container_Componenet_ID    INT,
  Sub_Component_ID            INT,
  Container_ID                  INT,
  Quantity                          INT,
  Entry_TS                                TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                                VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                                 VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_ccb_ccid      (Container_Componenet_ID),
  INDEX idx_ccb_subcomp   (Sub_Component_ID),
  INDEX idx_ccb_container (Container_ID),
  CONSTRAINT fk_ccb_ccid      FOREIGN KEY (Container_Componenet_ID) REFERENCES Container_Components(ID),
  CONSTRAINT fk_ccb_subcomp   FOREIGN KEY (Sub_Component_ID)        REFERENCES Components(ID),
  CONSTRAINT fk_ccb_container FOREIGN KEY (Container_ID)            REFERENCES Containers(ID)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

```

