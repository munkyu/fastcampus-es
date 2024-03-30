import pandas as pd
from sqlalchemy import create_engine
import glob

# 데이터베이스 연결 설정
username = 'user'
password = 'password'
database_name = 'db'
host = 'localhost'
port = '3306'

# SQLAlchemy 엔진 생성
engine = create_engine(f'mysql+pymysql://{username}:{password}@{host}:{port}/{database_name}')

# CSV 파일 경로
# CSV 파일들이 있는 디렉토리 경로 및 패턴
directory_path = '/Users/johnny/fastcampus-es/dataset/products'
pattern = f"{directory_path}/product_chunk_*.csv"

# 패턴과 일치하는 모든 파일을 찾아서 처리
for file_path in glob.glob(pattern):
    # CSV 파일 읽기 (첫 번째 줄은 컬럼명으로 사용)
    df = pd.read_csv(file_path)
    # 데이터베이스의 'products' 테이블에 데이터 삽입
    df.to_sql('products', con=engine, if_exists='append', index=False)
    print(f"{file_path} 데이터가 MySQL 데이터베이스에 성공적으로 삽입되었습니다.")
