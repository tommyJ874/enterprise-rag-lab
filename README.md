# Enterprise RAG Lab

Java 21과 Spring Boot 3.x로 만드는 기업용 RAG 학습 프로젝트입니다.

이 프로젝트의 목표는 Pinecone, Qdrant, Milvus, Weaviate, Chroma 같은 Vector DB가 내부에서 어떤 일을 하는지 라이브러리 없이 직접 구현하며 이해하는 것입니다.

현재는 Mini Vector DB의 핵심 검색 흐름을 직접 구현하고 있습니다.

## Current Focus

현재 단계에서는 IVF(Inverted File Index)의 기본 아이디어를 학습합니다.

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
cluster centroid O(1) 업데이트
      |
      v
search(query)
      |
      v
가장 가까운 cluster 선택
      |
      v
선택된 cluster 내부에서 cosine similarity 계산
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
- 실제 Vector DB가 왜 index를 쓰는지 이해하기 좋은 기준점입니다.

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

각 cluster의 대표 벡터인 centroid를 유지하고, query vector와 가장 가까운 centroid를 먼저 찾습니다.

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

### Online Centroid Update

새 문서가 들어올 때마다 cluster의 모든 문서를 다시 읽어 평균을 구하지 않습니다.

기존 centroid와 cluster 문서 수만 사용해 새 centroid를 갱신합니다.

```text
newCentroid = (oldCentroid * count + newVector) / (count + 1)
```

벡터에서는 각 차원마다 같은 공식을 적용합니다.

```text
old centroid: (4, 5)
count: 3
new vector: (8, 9)

x = (4 * 3 + 8) / 4 = 5
y = (5 * 3 + 9) / 4 = 6

new centroid: (5, 6)
```

이 방식은 새 문서를 추가할 때 전체 cluster를 다시 평균내지 않으므로, insert 시 centroid 업데이트 비용을 문서 수가 아니라 vector dimension에만 비례하도록 줄입니다.

## Usage Example

```java
VectorDB vectorDB = new VectorDB();

vectorDB.insert(new Document(
        "parental-leave",
        "직원은 자녀 양육을 위해 육아휴직을 신청할 수 있습니다.",
        new double[]{1.0, 0.0},
        1
));

vectorDB.insert(new Document(
        "finance",
        "회사는 매 회계연도 종료 후 재무제표를 제출해야 합니다.",
        new double[]{0.0, 1.0},
        2
));

List<SearchResult> results =
        vectorDB.searchNearestCluster(new double[]{1.0, 0.0}, 3);
```

이 예시는 query vector와 가장 가까운 cluster centroid를 먼저 찾고, 선택된 cluster 안에서만 문서를 정렬해 반환합니다.

## Current API

- `insert(Document document)`: 문서 저장 및 cluster centroid 점진 업데이트
- `bruteForceSearch(double[] queryVector)`: 전체 문서 검색
- `search(double[] queryVector, int queryCluster)`: 특정 cluster 내부 검색
- `searchNearestCluster(double[] queryVector)`: 가장 가까운 cluster 자동 선택 후 검색
- `searchNearestCluster(double[] queryVector, int topK)`: 가장 가까운 cluster에서 Top K 반환
- `nearestCluster(double[] queryVector)`: query와 가장 가까운 cluster 번호 반환
- `centroid(int cluster)`: cluster의 현재 centroid 반환
- `updateCentroid(double[] centroid, double[] newVector, int count)`: Online K-Means 방식 centroid 업데이트

## Complexity

| Operation | Current Implementation | Meaning |
| --- | --- | --- |
| Brute force search | O(N) | 모든 문서와 query를 비교 |
| Cluster search | O(M) | 선택된 cluster 안의 M개 문서만 비교 |
| Nearest cluster selection | O(K) | K개 centroid와 query를 비교 |
| Nearest cluster search | O(K + M) | cluster 선택 후 해당 cluster 내부 검색 |
| Online centroid update | O(D) | D차원 vector의 각 차원 평균만 갱신 |

N은 전체 문서 수, K는 cluster 수, M은 선택된 cluster의 문서 수, D는 vector dimension입니다.

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

1. Brute Force Search - 완료
2. Clustered Search - 완료
3. Centroid 기반 cluster 선택 - 완료
4. Online K-Means 방식 centroid 업데이트 - 완료
5. IVF의 `nprobe` 아이디어 구현
6. ANN(Approximate Nearest Neighbor) 탐색 실험
7. HNSW 그래프 구조 구현
8. 탐색 과정 시각화

## Next Step

현재는 insert 시 centroid를 점진적으로 갱신하고, 검색 시 가장 가까운 cluster 하나만 선택합니다.

다음 단계에서는 가까운 cluster 여러 개를 후보로 선택해 검색 품질과 검색 속도의 균형을 조절합니다.

이 과정을 통해 IVF에서 `nprobe` 값이 왜 중요한지 실험합니다.
