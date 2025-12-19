-- 1. 데이터베이스 초기화
DROP DATABASE IF EXISTS fastfood_db;
CREATE DATABASE fastfood_db;
USE fastfood_db;


-- [DDL] 테이블 생성

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


-- [추가] 4-1. 메뉴 옵션 정의 (Menu Option)
CREATE TABLE menu_option (
    option_id INT AUTO_INCREMENT PRIMARY KEY,
    menu_id INT NOT NULL,
    ingredient_id INT NOT NULL,
    option_name VARCHAR(50) NOT NULL,     -- 예: '피클 제외', '양상추 추가'
    option_type ENUM('REMOVE','ADD') NOT NULL,  -- 제외/추가
    delta_quantity INT NOT NULL,          -- 재고 변화량: 제거는 음수, 추가는 양수
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,

    FOREIGN KEY (menu_id) REFERENCES menu(menu_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id) ON DELETE CASCADE
);

CREATE INDEX idx_menu_option_menu ON menu_option(menu_id);


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


-- [추가] 8-1. 주문 옵션 선택 내역 (Order Detail Option)
CREATE TABLE order_detail_option (
    detail_option_id INT AUTO_INCREMENT PRIMARY KEY,
    detail_id INT NOT NULL,
    option_id INT NOT NULL,
    option_qty INT DEFAULT 1, -- 확장용(같은 옵션 2번 등), 보통 1

    FOREIGN KEY (detail_id) REFERENCES order_detail(detail_id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES menu_option(option_id) ON DELETE CASCADE,
    UNIQUE KEY uq_detail_option (detail_id, option_id)
);

CREATE INDEX idx_odo_detail ON order_detail_option(detail_id);

ALTER TABLE menu_option
ADD COLUMN delta_price INT NOT NULL DEFAULT 0;


-- 9. 매출 (Store Sale)
CREATE TABLE store_sale (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_method VARCHAR(20),
    total_price INT NOT NULL,
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES customer_order(order_id) ON DELETE CASCADE
);

-- [DML] 초기 데이터 삽입 (Master Data)

-- 1. 매장 생성 ( 예 - 아이디: ST-001, 비번: 01012345678)
INSERT INTO store (store_name, store_code, phone_number, address, manager_name) 
VALUES ('테스트점', 'TMP-000', '01012345678', '서울시 어딘가', '테스트');

-- 2. 재료 생성
INSERT INTO ingredient (ingredient_name, ingredient_code, category_name, unit) VALUES 
('햄버거 빵', 'ING-001', '빵류', '개'),
('소고기 패티', 'ING-002', '육류', '장'),
('체다 치즈', 'ING-003', '유제품', '장'),
('콜라 시럽', 'ING-004', '음료', 'ml'),
('감자튀김', 'ING-005', '사이드', 'g'),
('마요네즈', 'ING-006', '소스', 'g'),
('머스타드', 'ING-007', '소스', 'g'),
('불고기 소스', 'ING-008', '소스', 'g'),
('케찹 소스', 'ING-009', '소스', 'g'),
('타르타르소스', 'ING-010', '소스', 'g'),
('피클', 'ING-011', '채소', 'g'),
('양상추', 'ING-012', '채소', 'g'),
('양파', 'ING-013', '채소', 'g'),
('치킨 패티', 'ING-014', '육류', '장'),
('새우 패티', 'ING-015', '새우', '장'),
('모짜렐라 패티', 'ING-016', '유제품', '장'),
('에그타르트', 'ING-017', '사이드', '개'),
('윙봉', 'ING-018', '사이드', '개'),
('아이스크림 믹스', 'ING-019', '사이드', 'ml'),
('환타 시럽', 'ING-020', '음료', 'ml'),
('제로콜라 시럽', 'ING-021', '음료', 'ml'),
('스프라이트 시럽', 'ING-022', '음료', 'ml'),
('아메리카노 원두', 'ING-023', '음료', 'g'), -- 30g
('마운틴듀 시럽', 'ING-024', '음료', 'ml'),
('시그니처 패티', 'ING-025', '육류', '장'),
('시그니처 소스', 'ING-026', '소스', 'g'),
('치즈스틱', 'ING-027', '사이드', '개'),
('코울슬로', 'ING-028', '사이드', '개')
;

-- 3. 메뉴 생성 (세트 가격 포함)
INSERT INTO menu (menu_name, menu_code, price, set_price, category_name, is_recommended) VALUES 
('치즈버거', 'MN-001', 6000, 8500, '버거', TRUE),
('불고기버거', 'MN-002', 5500, 8000, '버거', FALSE),
('새우버거', 'MN-003', 5500, 8000, '버거', FALSE),
('더블버거', 'MN-004', 6000, 8500, '버거', FALSE),
('치킨버거', 'MN-005', 5000, 7500, '버거', FALSE),
('빅불고기버거', 'MN-006', 8000, 10500, '버거', FALSE),
('감자튀김', 'MN-007', 2000, 0, '사이드', FALSE),
('코카콜라', 'MN-008', 2000, 0, '음료', FALSE),
('스프라이트', 'MN-009', 2000, 0, '음료', FALSE),
('데리버거', 'MN-010', 4000, 5500, '버거', FALSE),
('모짜렐라버거', 'MN-011', 7000, 9500, '버거', FALSE),
('에그타르트', 'MN-012', 2000, 0, '사이드', FALSE),
('윙봉', 'MN-013', 4000, 0, '사이드', FALSE),
('소프트콘', 'MN-015', 1000, 0, '사이드', FALSE),
('환타', 'MN-016', 2000, 0, '음료', FALSE),
('제로콜라', 'MN-017', 2000, 0, '음료', FALSE),
('아메리카노', 'MN-018', 1500, 0, '음료', FALSE),
('마운틴듀', 'MN-019', 2000, 0, '음료', FALSE),
('시그니처버거', 'MN-020', 10000, 11500, '버거', FALSE),
('치즈스틱', 'MN-021', 2500, 0, '사이드', FALSE),
('코울슬로', 'MN-022', 2500, 0, '사이드', FALSE);



-- 4. 레시피 연결
-- menu_id:
-- 1 치즈버거, 2 불고기버거, 3 새우버거, 4 더블버거, 5 치킨버거, 6 빅불고기버거,
-- 7 감자튀김, 8 코카콜라, 9 스프라이트, 10 데리버거, 11 모짜렐라버거,
-- 12 에그타르트, 13 윙봉, 14 소프트콘, 15 환타, 16 제로콜라

-- 치즈버거(1) = 빵1 + 패티1 + 치즈1 + 케찹 10g + 양상추 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(1, 1, 1), (1, 2, 1), (1, 3, 1), (1, 9, 10), (1, 12, 10);

-- 불고기버거(2) = 빵1 + 패티1 + 마요네즈 10g + 양상추 10g + 불고기소스 10g + 양파 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(2, 1, 1), (2, 2, 1), (2, 6, 10), (2, 12, 10), (2, 8, 10), (2, 13, 10);

-- 새우버거(3) = 빵1 + 새우패티1 + 피클 30g + 타르타르소스 10g + 양상추 10g + 양파 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(3, 1, 1), (3, 15, 1), (3, 11, 30), (3, 10, 10), (3, 12, 10), (3, 13, 10);

-- 더블버거(4) = 빵1 + 패티2 + 치즈1 + 케찹 10g + 양상추 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(4, 1, 1), (4, 2, 2), (4, 3, 1), (4, 9, 10), (4, 12, 10);

-- 치킨버거(5) = 빵1 + 치킨패티1 + 마요네즈 10g + 양상추 10g + 불고기소스 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(5, 1, 1), (5, 14, 1), (5, 6, 10), (5, 12, 10), (5, 8, 10);

-- 빅불고기버거(6) = 빵1 + 패티2 + 마요네즈 10g + 양상추 10g + 불고기소스 20g + 양파 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(6, 1, 1), (6, 2, 2), (6, 6, 10), (6, 12, 10), (6, 8, 20), (6, 13, 10);

-- 감자튀김(7) = 감자튀김 200g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(7, 5, 200);

-- 코카콜라(8) = 콜라 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(8, 4, 200);

-- 스프라이트(9) = 스프라이트 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(9, 22, 200);

-- 데리버거(10) = 빵1 + 패티1 + 마요네즈 10g + 양상추 10g + 불고기소스 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(10, 1, 1), (10, 2, 1), (10, 6, 10), (10, 12, 10), (10, 8, 10);

-- 모짜렐라버거(11) = 빵1 + 패티1 + 모짜렐라 패티 1 + 치즈 1
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(11, 1, 1), (11, 2, 1), (11, 16, 1), (11, 3, 1);

-- 시그니처버거(19) = 시그니처 패티1 + 치즈1 + 시그니처 소스 10g + 피클 30g + 양파 10g + 양상추 10g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(19, 25, 1), (19, 3, 1), (19, 26, 10), (19, 11, 30), (19, 12, 10), (19, 13, 10);

-- 에그타르트(12) = 에그타르트 1개
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(12, 17, 1);

-- 윙봉(13) = 윙봉 4개
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(13, 18, 4);

-- 치즈스틱(20) = 치즈스틱 1개
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(20, 27, 1);

-- 코울슬로(21) = 코울슬로 1개
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(21, 28, 1);

-- 소프트콘(14) = 아이스크림 믹스 90ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(14, 19, 90);

-- 환타(15) = 환타 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(15, 20, 200);

-- 제로콜라(16) = 제로콜라 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(16, 21, 200);

-- 아메리카노(17) = 아메리카노 원두 30g
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(17, 23, 30);

-- 마운틴듀(18) = 마운틴듀 시럽 200ml
INSERT INTO menu_recipe (menu_id, ingredient_id, required_quantity) VALUES
(18, 24, 200);


-- 대상 메뉴_id: 1,2,3,4,5,6,10,11 (버거류)
-- 재료_id: 피클(11), 양상추(12), 양파(13), 케찹(9), 마요(6), 불고기소스(8), 타르타르(10), 치즈(3), 시그니처 패티(25), 시그니처 소스(26)

INSERT INTO menu_option
(menu_id, ingredient_id, option_name, option_type, delta_quantity, delta_price, sort_order)
VALUES

-- 1) 치즈버거 (menu_id=1)

(1, 3,  '치즈 제외',     'REMOVE', -1,  0,   10),
(1, 9,  '케찹 제외',     'REMOVE', -10, 0,   20),
(1, 12, '양상추 제외',   'REMOVE', -10, 0,   30),

(1, 3,  '치즈 추가',     'ADD',     1,  500, 40),
(1, 9,  '케찹 추가',     'ADD',     10, 0,   50),
(1, 12, '양상추 추가',   'ADD',     10, 0,   60),
(1, 13, '양파 추가',     'ADD',     10, 0,   70),
(1, 11, '피클 추가',     'ADD',     30, 0,   80),

-- 2) 불고기버거 (menu_id=2)

(2, 6,  '마요네즈 제외', 'REMOVE', -10, 0,   10),
(2, 12, '양상추 제외',   'REMOVE', -10, 0,   20),
(2, 8,  '불고기소스 제외','REMOVE', -10, 0,  30),
(2, 13, '양파 제외',     'REMOVE', -10, 0,   40),

(2, 6,  '마요네즈 추가', 'ADD',     10, 0,   50),
(2, 8,  '불고기소스 추가','ADD',    10, 0,   60),
(2, 12, '양상추 추가',   'ADD',     10, 0,   70),
(2, 13, '양파 추가',     'ADD',     10, 0,   80),
(2, 11, '피클 추가',     'ADD',     30, 0,   90),
(2, 3,  '치즈 추가',     'ADD',     1,  500, 100),

-- 3) 새우버거 (menu_id=3)

(3, 11, '피클 제외',     'REMOVE', -30, 0,   10),
(3, 10, '타르타르소스 제외','REMOVE', -10, 0, 20),
(3, 12, '양상추 제외',   'REMOVE', -10, 0,   30),
(3, 13, '양파 제외',     'REMOVE', -10, 0,   40),

(3, 11, '피클 추가',     'ADD',     30, 0,   50),
(3, 10, '타르타르소스 추가','ADD',  10, 0,   60),
(3, 12, '양상추 추가',   'ADD',     10, 0,   70),
(3, 13, '양파 추가',     'ADD',     10, 0,   80),
(3, 3,  '치즈 추가',     'ADD',     1,  500, 90),
(3, 9,  '케찹 추가',     'ADD',     10, 0,   100),

-- 4) 더블버거 (menu_id=4)

(4, 3,  '치즈 제외',     'REMOVE', -1,  0,   10),
(4, 9,  '케찹 제외',     'REMOVE', -10, 0,   20),
(4, 12, '양상추 제외',   'REMOVE', -10, 0,   30),

(4, 3,  '치즈 추가',     'ADD',     1,  500, 40),
(4, 9,  '케찹 추가',     'ADD',     10, 0,   50),
(4, 12, '양상추 추가',   'ADD',     10, 0,   60),
(4, 13, '양파 추가',     'ADD',     10, 0,   70),
(4, 11, '피클 추가',     'ADD',     30, 0,   80),

-- 5) 치킨버거 (menu_id=5)

(5, 6,  '마요네즈 제외', 'REMOVE', -10, 0,   10),
(5, 12, '양상추 제외',   'REMOVE', -10, 0,   20),
(5, 8,  '불고기소스 제외','REMOVE', -10, 0,  30),

(5, 6,  '마요네즈 추가', 'ADD',     10, 0,   40),
(5, 8,  '불고기소스 추가','ADD',    10, 0,   50),
(5, 12, '양상추 추가',   'ADD',     10, 0,   60),
(5, 13, '양파 추가',     'ADD',     10, 0,   70),
(5, 11, '피클 추가',     'ADD',     30, 0,   80),
(5, 3,  '치즈 추가',     'ADD',     1,  500, 90),


-- 6) 빅불고기버거 (menu_id=6)

(6, 6,  '마요네즈 제외', 'REMOVE', -10, 0,   10),
(6, 12, '양상추 제외',   'REMOVE', -10, 0,   20),
(6, 8,  '불고기소스 제외','REMOVE', -20, 0,  30),
(6, 13, '양파 제외',     'REMOVE', -10, 0,   40),

(6, 6,  '마요네즈 추가', 'ADD',     10, 0,   50),
(6, 8,  '불고기소스 추가','ADD',    10, 0,   60), -- 추가는 10g 단위
(6, 12, '양상추 추가',   'ADD',     10, 0,   70),
(6, 13, '양파 추가',     'ADD',     10, 0,   80),
(6, 11, '피클 추가',     'ADD',     30, 0,   90),
(6, 3,  '치즈 추가',     'ADD',     1,  500, 100),

-- 10) 데리버거 (menu_id=10)

(10, 6,  '마요네즈 제외', 'REMOVE', -10, 0,   10),
(10, 12, '양상추 제외',   'REMOVE', -10, 0,   20),
(10, 8,  '불고기소스 제외','REMOVE', -10, 0,  30),

(10, 6,  '마요네즈 추가', 'ADD',     10, 0,   40),
(10, 8,  '불고기소스 추가','ADD',    10, 0,   50),
(10, 12, '양상추 추가',   'ADD',     10, 0,   60),
(10, 13, '양파 추가',     'ADD',     10, 0,   70),
(10, 11, '피클 추가',     'ADD',     30, 0,   80),
(10, 3,  '치즈 추가',     'ADD',     1,  500, 90),

-- 11) 모짜렐라버거 (menu_id=11)

(11, 3,  '치즈 제외',     'REMOVE', -1,  0,   10),

(11, 3,  '치즈 추가',     'ADD',     1,  500, 20),
(11, 9,  '케찹 추가',     'ADD',     10, 0,   30),
(11, 6,  '마요네즈 추가', 'ADD',     10, 0,   40),
(11, 12, '양상추 추가',   'ADD',     10, 0,   50),
(11, 13, '양파 추가',     'ADD',     10, 0,   60),

-- 20) 시그니처버거 (menu_id=20)
(19, 3,  '치즈 제외',     'REMOVE', -1,  0,   10),
(19, 11,  '피클 제외',     'REMOVE', -30, 0,   20),
(19, 12, '양상추 제외',   'REMOVE', -10, 0,   30),

(19, 26,  '시그니처 소스 추가','ADD',    10, 0,   40),
(19, 11, '피클 추가',     'ADD',     30, 0,   50),
(19, 12, '양상추 추가',   'ADD',     10, 0,   60),
(19, 13, '양파 추가',     'ADD',     10, 0,   70),
(19, 3,  '치즈 추가',     'ADD',     1,  500, 80);

-- 5. 매장 초기 재고 세팅

-- 단위: 빵/패티/치즈/사이드(개/장), 시럽/믹스(ml), 소스/채소/감튀(g)
INSERT INTO store_inventory (store_id, ingredient_id, quantity) VALUES
-- 빵류/패티류/치즈(개/장)
(1, 1, 200),   -- 햄버거 빵 (개)
(1, 2, 200),   -- 소고기 패티 (장)
(1, 3, 150),   -- 체다 치즈 (장)
(1, 14, 150),  -- 치킨 패티 (장)
(1, 15, 120),  -- 새우 패티 (장)
(1, 16, 80),   -- 모짜렐라 패티 (장)
(1, 25, 80), -- 시그니처 패티 (장)

-- 음료 시럽/아이스크림 믹스(ml)
(1, 4, 20000),  -- 콜라 시럽 (ml)
(1, 20, 12000), -- 환타 시럽 (ml)
(1, 21, 12000), -- 제로콜라 시럽 (ml)
(1, 22, 12000), -- 스프라이트 시럽 (ml)
(1, 19, 8000),  -- 아이스크림 믹스 (ml)
(1, 23, 9000), -- 아메리카노 원두 (g)

-- 사이드 원재료/완제품(개/g)
(1, 5, 30000),  -- 감자튀김 (g)
(1, 17, 80),    -- 에그타르트 (개)
(1, 18, 200),   -- 윙봉 (개)
(1, 27, 10000), -- 치즈스틱 (개)
(1, 28, 80), -- 코울슬로 (개)

-- 소스류(g)
(1, 6, 5000),  -- 마요네즈 (g)
(1, 7, 3000),  -- 머스타드 (g)
(1, 8, 5000),  -- 불고기 소스 (g)
(1, 9, 5000),  -- 케찹 소스 (g)
(1, 10, 4000), -- 타르타르소스 (g)
(1, 26, 3000), -- 시그니처소스 (g)

-- 채소류(g)
(1, 11, 3000), -- 피클 (g)
(1, 12, 4000), -- 양상추 (g)
(1, 13, 4000); -- 양파 (g)

COMMIT;