# instagram-clone
Instagram clone 프로젝트 

## 강의 내용과 다르게 구현한 점
* ConstraintLayout, CoordinatorLayout 사용 
* 화면 이동에 Navigation 사용 
* RecyclerView의 adapter로 ListAdapter 사용
* 각종 NPE 처리 
* Firebase 인스턴스들을 Object로 추출 
* Firebase 사용할 때 Coroutine(Flow) 적용
* ViewModel 추가해서 Activity/Fragment에 있는 UI와 관련 없는 코드들 이전
* 로그인 한 계정 화면 Fragment와 DetailView에서 사용자 프로필 클릭하여 이동할 수 있는 유저 정보 화면의 분리
