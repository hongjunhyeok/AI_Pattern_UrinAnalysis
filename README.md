# AI기술을 이용한 고정밀 소변검사키트 개발


## **[의의]**
- 스마트폰 기반의 정밀 소변검사 플랫폼을 이용, 임산부의 건강상태를 지속적으로 모니터링.

- 임신중독증으로 인한 사망을 방지할 수 있는 건강관리 시스템 개발.

- 모바일 헬스케어 분야의 핵심 기술 및 지식재산권 확보

- 스마트폰을 이용해 일반인의 소변상태를 지속적으로 모니터링, IT와 헬스케어가 융합된 융합 기술 구현

## **[기존검사의 한계]**

![image](https://user-images.githubusercontent.com/34786411/105121860-6d897600-5b18-11eb-9112-d135ecadc9aa.png)

- 전통적인 검사의 경우 정량적으로 측정하지 못하여 정확도 편차가 존재
- 개개인마다 반응하는 양, 시간에 따라 결과의 신뢰도가 감소

## **[제안]**

![image](https://user-images.githubusercontent.com/34786411/105126143-7468b680-5b21-11eb-8551-3cb92ac0ac82.png)

- 별도의 센서구조를 설계하여 시간, 양에 따른 결과의 편차를 일정하게 유지
- 측정하는 센서의 값을 이용해 (당뇨, 단백뇨, PH, RBC) 딥러닝을 적용하여 환자의 위험도를 제시
- 패턴인식과 동시에 이미지 분석을 통하여 정확도향상

## **[설계 구조]**

![image](https://user-images.githubusercontent.com/34786411/105124348-51d49e80-5b1d-11eb-8e88-317a4ce4a642.png)

- 별도로 제작한 센서를 컵하단에 부착하여 스마트폰에서 이미지 인식 및 결과를 도출하도록 함.
- 데이터 분석을 위해서는 정확한 리딩이 필요하기에 외부 조명 영향을 보정하는 알고리즘 개발

## **[원리]**

### 1. 인공지능을 이용한 위치 인식 알고리즘 - 패턴인식 모델 학습

사용자는 컵의 위치를 자세하게 조정할 필요 없을 뿐만아니라 항상 같은 위치에서 이미지 분석을 진행하므로 정확도가 높다는 장점이 있음.


![image](https://user-images.githubusercontent.com/34786411/105124535-ad069100-5b1d-11eb-8861-f66365f1ceda.png)

![image](https://user-images.githubusercontent.com/34786411/105124684-01117580-5b1e-11eb-8ed0-82fc259a4b0a.png)

- 컵에 특수한 패턴을 삽입하여 이를 인식하게 함
- 인식된 패턴을 기준으로 컵의 회전각, 카메라 사이와의 거리를 자동분석함
- 회전, 거리에 따라 자동적으로 측정해야하는 구간을 변동
- 인식한 위치를 토대로 센서의 색 변화를 측정하여 수치 정량화를 진행함. 정량화에 사용된 수식은 다음과 같음.

![image](https://user-images.githubusercontent.com/34786411/105129501-8bf76d80-5b28-11eb-8fcc-9d04fd58532f.png)



### 2. 읽어들인 센서의 값을 이용해 질병진단 - 데이터를 이용한 질병진단 모델 학습


### 학습데이터 

![image](https://user-images.githubusercontent.com/34786411/105126049-32d80b80-5b21-11eb-8210-137b08617624.png)

 국가 건강검진 2018 공공데이터를 사용하여 환자의 혈색소 콜레스테롤, 글루코즈를 Input으로 설정하고 요단백을 output으로 지정함.
  - 약 100만건의 데이터를 이용하여 평가 및 훈련의 학습데이터로 지정
  - 공공데이터의 정보가 소변검사와 맞지 않는 부분이 몇몇 존재하나 상당히 유사한 관계를 가지는 항목이 존재하므로 이를 치환.

### 모델 구조

  - 각 데이터는 개인의 진료내역이기 때문에 연관성이 없으므로 연관된 데이터에 흔히 사용되는 RNN, LSTM구조 대신 DNN구조를 사용.
  - Training Function, Hidden Layer 개수, Learning Rate는 각각 다음 다음 도표와 같이 설정
  - Hidden Layer의 경우는 10x10의 3층구조를 사용
  - Learning Rate는 0.001을 이용
  모델 학습에 필요한 기본 설정과 아이디어는 [reference] (https://cultivo-hy.github.io/deep%20learning/python/tensorflow/2019/03/12/%EB%94%A5%EB%9F%AC%EB%8B%9D-%EC%8B%A4%EC%8A%B5/)를 이용
  
  ![image](https://user-images.githubusercontent.com/34786411/105127416-34ef9980-5b24-11eb-879c-33aace616f5b.png)
  
### 3. 질병진단을 이용해 데이터베이스 전달

  - 안드로이드의 경우 보안정책으로 인해 외부 데이터베이스와 **직접적인 연결이 불가** 하다 이를 해결하기위해 두가지 방식이 존재한다.
  1. 기기 내 자체 SQLite 모듈을 작성하여 내부 데이터베이스 이용.
  2. ANDROID -> PHP -> WebDatabase 간접적인 연결을 통한 데이터베이스 접속
  
     - [ANDROID -> PHP] 레지스터요청 
     ----------
     
     ![image](https://user-images.githubusercontent.com/34786411/105127977-5bfa9b00-5b25-11eb-85df-f0618032ffda.png)
  
  
     - [PHP -> WebDatabase] 연결
     -----------
     
     ![image](https://user-images.githubusercontent.com/34786411/105128047-86e4ef00-5b25-11eb-9ef1-7e219d8b4e62.png)
     
     
## 최종 시스템 도식도

![image](https://user-images.githubusercontent.com/34786411/105129134-c57ba900-5b27-11eb-9553-5d1e58cb4b6b.png)
     
     
## [결과 및 논의점]

1. 측정한 결과는 표와 그래프로 확인이 가능하도록 제작

![image](https://user-images.githubusercontent.com/34786411/105129571-b517fe00-5b28-11eb-8ef9-5fcb3f2c55d9.png)

2. 측정한 결과값은 Volume, 시간에 변동없이 일정하게 Saturation을 보였음
![image](https://user-images.githubusercontent.com/34786411/105129634-d37df980-5b28-11eb-8b38-ebd880b5fe6c.png)


3. Tensorflow를 이용한 질병진단의 경우 충분한 환자데이터를 확보하지 못해 실험을 진행하지 못하였으나, 
약 70%수준의 정확도를 보여 기대에 못미치는 수준이었으며 예상되는 문제점은 다음과 같다.

  - 기존 건강검진데이터와 소변검사의 케이스가 정확하게 맞지않는다.
  
  - 소변검사 -> 건강검진 데이터와 치환하는 과정에서 선형성을 확보하지 못했다.
  
  - 소변검사를 이용한 환자데이터의 수가 현저히 적다.
  
  다만 이러한 문제점은 데이터베이스를 활용한 업데이트와 추가적인 데이터의 확보를 통해 정확도향상을 진행할 수 있을 것으로 판단
  




