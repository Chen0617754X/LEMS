-- ==========================================
-- 校园实验室设备管理系统
-- Laboratory Equipment Management System
-- Database: lems
-- ==========================================


-- 第一部分：创建数据库
CREATE DATABASE IF NOT EXISTS lems
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE lems;


-- 第二部分：创建用户表
USE lems;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
        COMMENT '角色：ADMIN管理员，USER普通用户',
    status TINYINT NOT NULL DEFAULT 1
        COMMENT '状态：1启用，0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='系统用户表';


-- 第三部分：创建设备分类表
CREATE TABLE IF NOT EXISTS equipment_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(100) NOT NULL UNIQUE COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='设备分类表';


-- 第四部分：创建设备信息表
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',

    equipment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    equipment_name VARCHAR(100) NOT NULL COMMENT '设备名称',

    category_id BIGINT NOT NULL COMMENT '设备分类ID',

    brand VARCHAR(100) COMMENT '设备品牌',
    model VARCHAR(100) COMMENT '设备型号',

    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '设备价格',
    quantity INT NOT NULL DEFAULT 1 COMMENT '设备数量',

    location VARCHAR(100) COMMENT '存放位置',
    status VARCHAR(20) NOT NULL DEFAULT '正常' COMMENT '设备状态',

    purchase_date DATE COMMENT '购入日期',
    description TEXT COMMENT '设备描述',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    CONSTRAINT fk_equipment_category
        FOREIGN KEY (category_id)
        REFERENCES equipment_category(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_equipment_name (equipment_name),
    INDEX idx_equipment_category (category_id),
    INDEX idx_equipment_status (status)

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='实验室设备信息表';


-- 第五部分：创建维修记录表
CREATE TABLE IF NOT EXISTS equipment_repair (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维修记录ID',

    equipment_id BIGINT NOT NULL COMMENT '设备ID',

    fault_description TEXT COMMENT '故障描述',
    repair_person VARCHAR(50) COMMENT '维修人员',

    repair_status VARCHAR(20) NOT NULL DEFAULT '待维修'
        COMMENT '维修状态',

    repair_result TEXT COMMENT '维修结果',

    report_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '报修时间',

    finish_time DATETIME COMMENT '完成时间',

    CONSTRAINT fk_repair_equipment
        FOREIGN KEY (equipment_id)
        REFERENCES equipment(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_repair_equipment (equipment_id),
    INDEX idx_repair_status (repair_status)

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='设备维修记录表';


-- 第六部分：创建系统日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',

    user_id BIGINT COMMENT '操作用户ID',

    operation VARCHAR(100) COMMENT '操作类型',
    detail VARCHAR(500) COMMENT '操作描述',

    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '操作时间',

    CONSTRAINT fk_log_user
        FOREIGN KEY (user_id)
        REFERENCES sys_user(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    INDEX idx_log_user (user_id),
    INDEX idx_log_time (create_time)

    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    COMMENT='系统操作日志表';


-- 第七部分：初始化设备分类数据
INSERT INTO equipment_category
(category_name, description)
VALUES
    ('网络设备', '路由器、交换机、防火墙等网络设备'),
    ('计算机设备', '台式电脑、笔记本电脑、服务器等'),
    ('实验仪器', '示波器、万用表、信号发生器等'),
    ('办公设备', '打印机、投影仪等')
    AS new
    ON DUPLICATE KEY UPDATE
    description = new.description;


-- 第八部分：初始化设备信息
INSERT INTO equipment
(equipment_no, equipment_name, category_id,
 brand, model, price, quantity, location,
 status, purchase_date, description)
VALUES
    ('SB2026001', '网络交换机', 1,
     'Cisco', 'C2960', 2500.00, 10,
     '实验室A201', '正常', '2026-09-01',
     '用于实验室网络通信'),

    ('SB2026002', '无线路由器', 1,
     'TP-Link', 'AX3000', 450.00, 5,
     '实验室A202', '正常', '2026-09-05',
     '用于无线网络实验'),

    ('SB2026003', '台式计算机', 2,
     'Dell', 'OptiPlex', 4800.00, 20,
     '实验室B101', '正常', '2026-08-15',
     '用于计算机网络实验'),

    ('SB2026004', '数字示波器', 3,
     'Rigol', 'DS1102', 1800.00, 3,
     '实验室C301', '维修中', '2026-07-20',
     '用于电子实验'),

    ('SB2026005', '网络防火墙', 1,
     'H3C', 'SecPath', 6800.00, 2,
     '实验室A201', '正常', '2026-09-10',
     '用于网络安全实验')

    AS new
    ON DUPLICATE KEY UPDATE
    equipment_name = new.equipment_name,
    quantity = new.quantity,
    status = new.status;


-- 第九部分：初始化维修记录
INSERT INTO equipment_repair
(equipment_id, fault_description, repair_person,
 repair_status, repair_result)
SELECT id, '设备无法正常显示波形', '张师傅',
       '维修中', '正在检查设备电路'
FROM equipment
WHERE equipment_no = 'SB2026004'
  AND NOT EXISTS (
    SELECT 1 FROM equipment_repair
    WHERE equipment_id = equipment.id
      AND fault_description = '设备无法正常显示波形'
);

-- 创建项目专有MySQL账号
CREATE USER IF NOT EXISTS 'lems'@'localhost'
IDENTIFIED BY 'lems';
GRANT SELECT, INSERT, UPDATE, DELETE
    ON lems.*
    TO 'lems'@'localhost';