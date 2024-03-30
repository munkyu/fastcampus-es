# 파이썬 & 패키지 설치
```
brew install python3
pip3 install -r requirements.txt

```
# mysql에 데이터 넣기
```
# 테이블 생성
use db;
CREATE TABLE products (
    id INT NOT NULL AUTO_INCREMENT,
    asin VARCHAR(255) NOT NULL,
    title TEXT,
    img_url TEXT,
    product_url TEXT,
    stars FLOAT,
    reviews INT,
    price DECIMAL(10, 2),
    list_price DECIMAL(10, 2),
    category_id INT,
    is_best_seller BOOLEAN,
    bought_in_last_month INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_asin (asin)
);

# 데이터 넣기
python3 data.py
```
