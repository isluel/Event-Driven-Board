# Event-Driven-Board

Board 관련하여, Article, Comment, Like, Unlike, Hot Article(인기글), article read(게시글 조회)의 기능을 MicroService 기반으로 설계 및 구현을 한다.
Kafka, Redis를 사용하여 해당 프로그램은 Docker로 실행하여 진행한다.

## Total

### 흐름도

### 사용 환경

Intellij, Spring boot, Redis, Kafka, Docker, MySQL

## Article Service

### 요구 사항 정의

- Article Id 로 DB에 저장된 ArticleId를 조회한다.
- Board Id, Page, PageSize로 해당 Board Id 하위의 Article을 조회한다.
- Board Id, PageSize, LastArticleId 로 Board Id 하위의 LastArticle ID 이후의 Article을 조회한다.
- Article Id에 해당되는 Article을 수정한다.
- Aritlce ID에 해당되는 Article을 삭제한다.
- BoarId에 해당되는 Article의 전체 개수를 반환한다.

### 상세 설계 내용

- Article Id에 해당되는 Article을 반환한다.
- Barod Id 에 해당되는 Article을 Page 번호와 PageSize 만큼 조회 한다.  
  데이터 전달시 Front에서 페이지 번호를 만들수 있도록 이동 가능한 페이지 개수를 전달해 준다.
- Board Id에 해당되는 Article을 마지막 조회 Article 부터 PageSize 만큼 조회한다.
- 사용자가 입력한 Article 내용(Borad Id, Write Id, Title, Content) 으로 Article을 생성한다. 생성시 Borad Article Count 에 해당 Board Id의
  Count를 증가시킨다.
- Artilce Id에 해당되는 Article 내용(title, content)를 수정한다.
- Article Id에 해당되는 Article을 삭제한다.
  삭제시 Borad Article Count 에 해당 Board Id의 Count를 감소시킨다.
- Board Id에 해당되는 Article의 개수를 반환한다.
- Article 추가, 수정, 삭제시 생성 내용을 Kafka로 전달한다.

### 흐름도도

## Comment Service

### 요구사항 정의

- Comment Id에 해당되는 Comment를 반환한다.
- Comment를 추가 또는 삭제 한다.
- Article Id, PageSize, page로 Article Id 하위의 comment를 조회한다.
- Aritlce Id, PageSize, path로 Path 이후의 Comment를 조회한다.
- Article Id에 해당되는 Article의 전체 개수를 반환한다.

### 상세 설계 내용

- Comment Id에 해당되는 Comment을 반환한다.
- Article Id 에 해당되는 Comment 을 Page 번호와 PageSize 만큼 조회 한다.
  데이터 전달시 Front에서 페이지 번호를 만들수 있도록 이동 가능한 페이지 개수를 전달해 준다.
- Article Id에 해당되는 Comment를 마지막 조회 Article 부터 PageSize 만큼 조회한다.
- 사용자가 입력한 Comment 내용(Article Id, parent Comment, content, writer Id) 으로 Comment을 생성한다.
  생성시 Article Comment Count 에 해당 Article Id의 Count를 증가시킨다.
- Comment Id에 해당되는 Comment를 삭제한다.
  삭제시 Article Comment Count 에 해당 Article Id의 Count를 감소시킨다.
  삭제시 하위 댓글이 있는 경우, DB로부터 완전삭제하지 않고, Deleted Flag만 변경한다.
  하위 댓글이 모두 삭제된 경우에만 삭제할 수 있다.
- Comment 삭제, 추가시 해당 내용을 Kafka로 전달한다.

### 흐름도

## Article Like Service

### 요구사항 정의

- Article Id 와 User Id로 사용자가 해당 Article에 Like 여부를 반환한다.
- Article Id와 User Id로 사용자가 Article의 Like 를 클릭한다.
- Article Id와 User Id로 사용자가 Article의 Like 를 클릭하여 취소한다.

### 상세 설계 내용용

- DB에서 Article Id, UserId에 해당되는 Arcile Like 내역을 전달한다.
- 사용자가 Like 클릭시 Article Id, User Id, 생성 일자를 DB에 저장한다.
  Article의 like 카운트 저장 DB에 값을 증가시킨 후 해당 내역을 Kafka로 전달한다.
- 사용자가 Like를 한번더 클릭하여 unLike를 할시, Article Id, User Id 에 해당되는 내역을 삭제하고, Article 의 Like 카운트를 감소시킨다.
  해당 내용은 Kafka를 통해 전달한다.

### 흐름도

## Article View Service

### 요구사항 정의

- Article Id 와 User Id로 사용자가 해당 Article의 조회수를 증가시킨다.
- 사용자는 같은 게시글을 일정 시간(10분) 동안 조회 했을 경우 조회수가 증가하지 않는다.

### 상세 설계 내용

- Like, UnLike 발생시 바로 DB에 저장하는 것이 아닌 Redis에 저장하고.
  Like의 값이 일정 값이 되었을 경우 DB에 저장한다.
  (BackUp 으로 수행)
- 어뷰징 방지 대책으로, 첫 Article 조회시 Redis에 조회 내역을 저장하여,
  10분 내에 이루어진 View Count는 업데이트 하지 않는다.
- DB 에 저장시 동시에 들어온 처리를 위해 높은 조회수 데이터만 저장하도록 한다.
- DB에 backup 수행시 해당 내역을 Kafka로 전달하도록 한다.

### 흐름도

## Hot Article Service

### 요구사항 정의

- 일자에 해당되는 인기글을 인기순으로 반환한다.

### 상세 설계 내용

- Article, Comment, Article Like, Article View Service에서 이벤트 발생시 전달한 Kafka의 Topic 데이터를 읽어서 처리 한다.
- Article Create, Article Update, Aritcle Delete, Comment Create, delete, Article Like, Article UnLike, Articcle View
  이벤트 발생시 Kafka의 각 Topic으로 데이터 전송한다.
  Article View는 DB에 저장될때 수행하도록 한다.
- kafka로 전송 받은 데이터는 모두 Redis에 저장해 인기글 Score 재 계산시 원본데이터 호출을 최대한 하지 않도록 한다.
- Article, Comment, Like, View 서비스는 이벤트 발생시 EventPublisher로 발행하여 OutBox Table에 내역을 저장한다.
  Transaction Commit 완료시 Kafka에 데이터를 전송하고 OutBox에 내역을 삭제한다.
- HotArticle Service는 Kafka Consumer로 동작하여 데이터를 Redis에 저정 한다.
- Redis에 저장된 데이터는 10일의 TTL을 가지고 있다.
- 인기글 상세 데이터는 Redis가 아닌 실제 원본데이터를 조회하도록 한다.

### 흐름도

## Article Read Service

### 요구사항 정의

- 전달 받은 Article Id에 해당되는 Article 데이터를 반환 한다.
- Board Id, page, pageSize에 해당되는 Article 데이터들을 반환한다.

### 상세 설계

- Article 생성, 삭제, Like, UnLike, Update 및 Comment 생성, 삭제 Event 발생시 해당 데이터를 ArticleModelQuery Object 를 JsonString 으로 변환해
  Redis에 저장한다.
  Reids에 저장시 TTL은 1일로 지정한다.
- Aritcle Id에 해당되는 데이터 조회시 Redis에서 ArticleQuerytModel을 전달 한다.   
  데이터가 없을 경우 Rest Client로 원본 데이터를 호출하여 저장후 전달한다.
- Article 생성, 삭제 Event 시 해당 id와 Board Id에 해당되는 Article Count수를 수정한다.
- Article ID List 항목은 Redis에 최대 1000개만 저장하도록 한다.

### 흐름도

