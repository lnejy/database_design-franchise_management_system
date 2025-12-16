-- 1. 데이터베이스 초기화
DROP DATABASE IF EXISTS fastfood_db;
CREATE DATABASE fastfood_db;
USE fastfood_db;

-- =============================================
-- [DDL] 테이블 생성
-- =============================================

-- 1. 매장 (Store)
CREATE TABLE store (
    store_id INT AUTO_INCREMENT PRIMARY KEY,
    store_name VARCHAR(50) NOT NULL,
    store_code VARCHAR(20) UNIQUE, -- 로그인 ID
    address VARCHAR(255),
    phone_number VARCHAR(20),      -- 로그인 PW (숫자만 사용)
    manager_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 2. 재료 (Ingredient)
CREATE TABLE ingredient (
    ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_name VARCHAR(50) NOT NULL,
    ingredient_code VARCHAR(20),
    category_name VARCHAR(30),
    unit VARCHAR(10) NOT NULL,
    image_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. 메뉴 (Menu) - 세트 가격 추가됨
CREATE TABLE menu (
    menu_id INT AUTO_INCREMENT PRIMARY KEY,
    menu_name VARCHAR(50) NOT NULL,
    menu_code VARCHAR(20),
    category_name VARCHAR(30),
    price INT NOT NULL,           -- 단품 가격
    set_price INT DEFAULT 0,      -- 세트 가격 (0이면 세트 없음)
    description TEXT,
    image_url VARCHAR(255),
    is_recommended BOOLEAN DEFAULT FALSE,
    is_sold_out BOOLEAN DEFAULT FALSE
);

-- 4. 레시피 (Menu Recipe)
CREATE TABLE menu_recipe (
    menu_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    required_quantity INT NOT NULL,
    PRIMARY KEY (menu_id, ingredient_id),
    FOREIGN KEY (menu_id) REFERENCES menu(menu_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE
);

-- 5. 매장 재고 (Store Inventory)
CREATE TABLE store_inventory (
    store_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity INT DEFAULT 0,
    min_threshold INT DEFAULT 10,  -- 알림 기준값
    last_updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (store_id, ingredient_id),
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE
);

-- 6. 자재 요청 (Store Material Request) - 물류창고용 날짜 추가됨
CREATE TABLE store_material_request (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    store_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING(대기), APPROVED(승인/출고), REJECTED(반려)
    request_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_date DATETIME NULL,         -- [중요] 본사 승인(출고) 일시 (물류창고 App 조회용)
    FOREIGN KEY (store_id) REFERENCES store(store_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE
);

-- 7. 주문 헤더 (Customer Order) - 상태관리 추가됨
CREATE TABLE customer_order (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    store_id INT NOT NULL,
    order_number VARCHAR(20) NOT NULL, -- 고객 호출 번호 (예: 101)
    total_amount INT NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING', -- WAITING(조리대기), COMPLETED(조리완료/픽업)
    order_type VARCHAR(20) DEFAULT 'DINE_IN',
    order_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES store(store_id)
);

-- 8. 주문 상세 (Order Detail) - 세트 여부 추가됨
CREATE TABLE order_detail (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_id INT NOT NULL,
    quantity INT NOT NULL,
    is_set BOOLEAN DEFAULT FALSE,  -- 세트 메뉴 여부 확인
    unit_price INT NOT NULL,       -- 결제 당시 단가
    subtotal_price INT NOT NULL,   -- unit_price * quantity
    FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menu(menu_id)
);

-- 9. 매출 (Store Sale)
CREATE TABLE store_sale (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(20),
    total_price INT NOT NULL,
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE
);

-- =============================================
-- [DML] 초기 데이터 삽입 (Master Data)
-- =============================================

-- 1. 매장 생성 ( 예 - 아이디: ST-001, 비번: 01012345678)
INSERT INTO store (store_name, store_code, phone_number, address, manager_name) 
VALUES ('테스트점', 'TMP-000', '01012345678', '서울시 어딘가', '테스트');

-- 2. 재료 생성
INSERT INTO ingredient (ingredient_name, ingredient_code, category_name, unit) VALUES 
('햄버거 빵', 'ING-001', '빵류', '개'),
('소고기 패티', 'ING-002', '육류', '장'),
('체다 치즈', 'ING-003', '유제품', '장'),
('콜라 시럽', 'ING-004', '음료', 'ml'),
('감자튀김', 'ING-005', '사이드', 'g');

-- 3. 메뉴 생성 (세트 가격 포함)
INSERT INTO menu (menu_name, menu_code, price, set_price, category_name, is_recommended) VALUES 
('치즈버거', 'MN-001', 6000, 8500, '버거', TRUE),
('더블버거', 'MN-002', 8500, 11000, '버거', FALSE),
('감자튀김', 'MN-003', 2000, 0, '사이드', FALSE), -- 사이드는 세트가격 0
('코카콜라', 'MN-004', 2000, 0, '음료', FALSE);

-- 4. 레시피 연결
-- 치즈버거(1) = 빵1 + 패티1 + 치즈1
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES (1, 1, 1), (1, 2, 1), (1, 3, 1);
-- 더블버거(2) = 빵1 + 패티2 + 치즈1
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES (2, 1, 1), (2, 2, 2), (2, 3, 1);
-- 감자튀김(3) = 감자 200g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES (3, 5, 200);
-- 콜라(4) = 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES (4, 4, 200);

-- 5. 매장 초기 재고 세팅
INSERT INTO store_inventory (store_id, ingredient_id, quantity) VALUES
(1, 1, 100), (1, 2, 100), (1, 3, 100), (1, 4, 5000), (1, 5, 1000);

COMMIT;