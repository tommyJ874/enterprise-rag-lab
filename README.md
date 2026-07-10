# Enterprise RAG Lab

Java 21과 Spring Boot 3.x로 만드는 기업용 RAG 학습 프로젝트입니다.

이 프로젝트의 목표는 Pinecone, Qdrant, Milvus, Weaviate, Chroma 같은 Vector DB가 내부에서 어떤 일을 하는지 라이브러리 없이 직접 구현하며 이해하는 것입니다.

## Current Focus

현재 단계에서는 Mini Vector DB를 직접 구현합니다.

```text
insert()
  |
  v
Document 저장
  |
  v
vector 저장
  |
  v
search(query)
  |
  v
cosine similarity 계산
  |
  v
Top K 반환
```

## Implemented

### Brute Force Search

모든 문서를 순회하며 query vector와 document vector의 cosine similarity를 계산합니다.

```text
Query
  |
  v
Document 1
Document 2
Document 3
...
Document N
```

특징:

- 구현이 단순합니다.
- 검색 결과가 정확합니다.
- 문서가 많아질수록 검색 시간이 O(N)으로 증가합니다.

### Clustered Search

문서를 cluster 단위로 나누어 저장하고, 선택된 cluster 안에서만 검색합니다.

```text
Query
  |
  v
Cluster 선택
  |
  v
Cluster 내부 문서 검색
  |
  v
Top K 반환
```

예시:

```text
Cluster 1: 육아휴직, 출산휴가, 경조휴가, 연차
Cluster 2: 재무제표, 손익계산서, 자산, 부채
Cluster 3: 보험가입, 가입설계, 납입기간, 해지환급금
```

질문이 "육아휴직 신청은 어디서 하나요?"에 가깝다면 재무/보험 문서를 모두 비교하지 않고 HR cluster 안에서만 검색할 수 있습니다.

### Centroid Search

각 cluster의 대표 벡터인 centroid를 계산하고, query vector와 가장 가까운 centroid를 먼저 찾습니다.

```text
Query
  |
  v
Cluster centroid들과 similarity 계산
  |
  v
가장 가까운 Cluster 선택
  |
  v
선택된 Cluster 내부 문서 검색
  |
  v
Top K 반환
```

이 구조는 IVF(Inverted File Index)의 핵심 아이디어와 연결됩니다.

## Core Classes

- `Document`: id, text, vector, cluster를 가진 저장 단위
- `VectorDB`: 문서를 저장하고 검색하는 Mini Vector DB
- `Similarity`: cosine similarity 계산
- `SearchResult`: 검색된 문서와 score를 함께 담는 결과 객체

## Project Structure

```text
src/main/java/com/example/enterpriseraglab
  Document.java
  EnterpriseRagLabApplication.java
  SearchResult.java
  Similarity.java
  VectorDB.java

src/test/java/com/example/enterpriseraglab
  EnterpriseRagLabApplicationTests.java
  SimilarityTests.java
  VectorDBTests.java
```

## Requirements

- Java 21
- Spring Boot 3.x
- Maven Wrapper

## Run Tests

```bash
./mvnw test
```

## Run Application

```bash
./mvnw spring-boot:run
```

## Learning Roadmap

1. Brute Force Search
2. Clustered Search
3. Centroid 기반 cluster 선택
4. IVF(Inverted File Index) 아이디어 구현
5. ANN(Approximate Nearest Neighbor) 탐색 실험
6. HNSW 그래프 구조 구현
7. 탐색 과정 시각화

## Next Step

현재는 가장 가까운 cluster 하나만 선택해 검색합니다.

다음 단계에서는 가까운 cluster 여러 개를 후보로 선택해 검색 품질과 검색 속도의 균형을 조절합니다.

이 과정을 통해 IVF에서 `nprobe` 값이 왜 중요한지 실험합니다.
