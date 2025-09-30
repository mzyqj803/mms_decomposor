-- MMS Database Schema
-- This file contains the table creation statements

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) Components
CREATE TABLE IF NOT EXISTS components (
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
CREATE TABLE IF NOT EXISTS contracts (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_No       VARCHAR(255),
  Client_Name       VARCHAR(511),
  Project_Name      VARCHAR(511),
  Quantity           INT,
  status            INT DEFAULT 0,
  Entry_TS           TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User         VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS     TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User   VARCHAR(50) DEFAULT 'SYS_USER'
) ENGINE=InnoDB;

-- 3) Containers
CREATE TABLE IF NOT EXISTS containers (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Contract_ID       INT,
  Container_No      VARCHAR(255),
  Name               VARCHAR(511),
  Container_Size     VARCHAR(50),
  Container_Weight   VARCHAR(50),
  Comments            TEXT,
  status             INT DEFAULT 0,
  Entry_TS             TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User             VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS          TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User         VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cont_contract (Contract_ID),
  CONSTRAINT fk_cont_contract
    FOREIGN KEY (Contract_ID) REFERENCES contracts(ID)
) ENGINE=InnoDB;

-- 4) Container_Components
CREATE TABLE IF NOT EXISTS container_components (
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
    FOREIGN KEY (Container_ID) REFERENCES containers(ID)
) ENGINE=InnoDB;

-- 5) Components_Spec
CREATE TABLE IF NOT EXISTS components_spec (
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
    FOREIGN KEY (Component_ID) REFERENCES components(ID)
) ENGINE=InnoDB;

-- 6) Components_Relationship
CREATE TABLE IF NOT EXISTS components_relationship (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Parent_ID          INT,
  Child_ID             INT,
  Entry_TS                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                  VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User               VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cr_parent (Parent_ID),
  INDEX idx_cr_child  (Child_ID),
  CONSTRAINT fk_cr_parent   FOREIGN KEY (Parent_ID) REFERENCES components(ID),
  CONSTRAINT fk_cr_child  FOREIGN KEY (Child_ID)  REFERENCES components(ID)
) ENGINE=InnoDB;

-- 7) Components_processes
CREATE TABLE IF NOT EXISTS components_processes (
  ID                INT PRIMARY KEY AUTO_INCREMENT,
  Component_ID      INT,
  Seq_No                 INT,
  Name                      VARCHAR(511),
  Comments                    TEXT,
  Entry_TS                        TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                        VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                     VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_cpp_component (Component_ID),
  CONSTRAINT fk_cpp_component
    FOREIGN KEY (Component_ID) REFERENCES components(ID)
) ENGINE=InnoDB;

-- 8) Contract_Parameters
CREATE TABLE IF NOT EXISTS contract_parameters (
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
    FOREIGN KEY (Contract_ID) REFERENCES contracts(ID)
) ENGINE=InnoDB;

-- 9) Containers_Components_Summary
CREATE TABLE IF NOT EXISTS containers_components_summary (
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
  CONSTRAINT fk_ccs_contract  FOREIGN KEY (Contract_ID)  REFERENCES contracts(ID),
  CONSTRAINT fk_ccs_component FOREIGN KEY (Component_ID) REFERENCES components(ID),
  CONSTRAINT fk_ccs_container FOREIGN KEY (Container_ID) REFERENCES containers(ID)
) ENGINE=InnoDB;

-- 10) Container_Components_Breakdown
CREATE TABLE IF NOT EXISTS container_components_breakdown (
  ID                         INT PRIMARY KEY AUTO_INCREMENT,
  Container_Component_ID    INT,
  Sub_Component_ID            INT,
  Container_ID                  INT,
  Quantity                          INT,
  Entry_TS                                TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                                VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS                                TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User                                 VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_ccb_ccid      (Container_Component_ID),
  INDEX idx_ccb_subcomp   (Sub_Component_ID),
  INDEX idx_ccb_container (Container_ID),
  CONSTRAINT fk_ccb_ccid      FOREIGN KEY (Container_Component_ID) REFERENCES container_components(ID),
  CONSTRAINT fk_ccb_subcomp   FOREIGN KEY (Sub_Component_ID)        REFERENCES components(ID),
  CONSTRAINT fk_ccb_container FOREIGN KEY (Container_ID)            REFERENCES containers(ID)
) ENGINE=InnoDB;

-- 11) Container_Components_Breakdown_Problems
CREATE TABLE IF NOT EXISTS container_components_breakdown_problems (
  ID                         INT PRIMARY KEY AUTO_INCREMENT,
  Container_ID               INT,
  Component_No               VARCHAR(255),
  Quantity                   INT,
  Entry_TS                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                 VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS             TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User           VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_ccbp_container (Container_ID),
  INDEX idx_ccbp_component_no (Component_No),
  CONSTRAINT fk_ccbp_container FOREIGN KEY (Container_ID) REFERENCES containers(ID)
) ENGINE=InnoDB;

-- 12) Fastener_Warehouse (紧固件库表)
CREATE TABLE IF NOT EXISTS fastener_warehouse (
  ID                         INT PRIMARY KEY AUTO_INCREMENT,
  Specs                      VARCHAR(511),
  Product_Code               VARCHAR(255),
  ERP_Code                   VARCHAR(255),
  Name                       VARCHAR(511),
  Level                      VARCHAR(50),
  Material                   VARCHAR(255),
  Surface_Treatment          VARCHAR(255),
  Remark                     TEXT,
  Default_Flag               TINYINT(1) DEFAULT 0,
  Entry_TS                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                 VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS             TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User           VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_fw_product_code (Product_Code),
  INDEX idx_fw_erp_code (ERP_Code),
  INDEX idx_fw_default_flag (Default_Flag),
  INDEX idx_fw_material (Material),
  INDEX idx_fw_level (Level)
) ENGINE=InnoDB;

-- 13) Container_Components_Breakdown_ERP (工艺分解ERP代码表)
CREATE TABLE IF NOT EXISTS container_components_breakdown_erp (
  ID                         INT PRIMARY KEY AUTO_INCREMENT,
  Breakdown_ID               INT NOT NULL,
  ERP_Code                   VARCHAR(255),
  Comments                   TEXT,
  Entry_TS                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
  Entry_User                 VARCHAR(50) DEFAULT 'SYS_USER',
  Last_Update_TS             TIMESTAMP DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
  Last_Update_User           VARCHAR(50) DEFAULT 'SYS_USER',
  INDEX idx_ccbe_breakdown   (Breakdown_ID),
  INDEX idx_ccbe_erp_code    (ERP_Code),
  CONSTRAINT fk_ccbe_breakdown FOREIGN KEY (Breakdown_ID) REFERENCES container_components_breakdown(ID) ON DELETE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;
