-- 13) Container_Components_Breakdown_ERP (工艺分解ERP代码表)
-- 用于存储工艺分解结果中每个组件的ERP代码信息
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
