from datetime import timedelta, datetime

from app import db
from models import User, Answer, Follower, Question

users = (
    User(id='vincent', password='12345676', name='빈센트', description='나는 그림을 그리기 위해서 살아있다', photo='vincent.jpg'),
    User(id='postman', password='12345676', name='룰랭', description='편지는 사랑을 싣고', photo='roulin.jpg'),
    User(id='tanguy', password='12345676', name='탕기', description='예술가는 나의 친구입니다', photo='tanguy.jpg'),
    User(id='segatori', password='12345676', name='세가토리', description='커피는 나의 힘', photo='segatori.jpg'),
    User(id='zuave', password='12345676', name='주아브 병사', description='NO WAR', photo='zuave.jpg'),
    User(id='ginoux', password='12345676', name='지누 부인', description='책은 마음의 양식', photo='ginoux.jpg'),
    User(id='gachet', password='12345676', name='가셰 박사', description='그림 그리는 의사', photo='gachet.jpg'),
    User(id='anonymous', password='12345676', name='아무개', description='챕터1', photo='skeleton.jpg'),
)

vincent, postman, tanguy, segatori, zuave, ginoux, gachet, anonymous = users
relation = {
    vincent: (postman, tanguy, ginoux),
    postman: (vincent, gachet),
    tanguy: (vincent, segatori),
    segatori: (postman, ginoux),
    zuave: (ginoux, gachet, segatori),
    ginoux: (vincent, gachet, tanguy),
    gachet: ()
}
questions_and_answers = (
    ('기억에 남는 추억의 장소는?', ()),
    ('유년시절 가장 생각나는 친구 이름은?', ()),
    ('인상 깊게 읽은 책을 알려주세요', ()),
    ('죽음 다음엔 무엇이 있을까요?', ()),
    ('받았던 선물 중 기억에 남는 선물은?', ()),
    ('책 중에서 좋아하는 구절이 있다면?', ()),
    ('존경하는 인물은?', (
        ('vincent', '밀레. 내 예술과 삶의 모범이다.'),
    )),
    ('올해 목표는?', (
        ('tanguy', '다이어트. 아니면 지금 몸에 맞는 바지 사기.'),
        ('gachet', '매일 매일 그림 그리기')
    )),
    ('어릴 적 별명이 무엇인가요?', ()),
    ('초등학교 때 기억에 남는 짝꿍 이름은?', ()),
    ('언젠가 가보고 싶은 곳', (
        ('postman', '고요의 바다'),
        ('segatori', '금강산'),
    )),
    ('내가 좋아하는 캐릭터는?', (
        ('postman', '머털도사'),
    )),
    ('몇 살까지 살고 싶나요?', (
        ('zuave', '짧고 굵게 마흔살!'),
        ('gachet', '150살까지 살고 그 이후론 인공 장기로 교체해 영생할 생각입니다.'),
    )),
    ('인생의 목표가 무엇인가요?', (
        ('vincent', '그림을 그려서 테오에게 보답해야지'),
        ('zuave', '세계 평화'),
        ('ginoux', '오랫동안 살아있을 코드를 만들고 싶어요'),
    )),
    ('다시 태어나면 되고 싶은 것은?', (
        ('ginoux', '뿌리 깊은 나무'),
        ('vincent', '3대가 놀고 먹어도 될 부자'),
        ('postman', '부모님의 부모님'),
    )),
    ('인생 좌우명은?', (
        ('segatori', '내일은 내일의 해가 뜬다'),
        ('zuave', '오늘 못먹은 밥은 평생 못먹는다'),
        ('gachet', '맛있으면 0칼로리'),
    )),
    ('나의 인생샷을 올려주세요', ()),
    ('하루에 가장 행복한 순간은? ', ()),
    ('내가 생각하는 나의 단점은?', ()),
    ('하루 중 가장 좋아하는 시간은 언제인가요?', ()),
    ('혼자 시간을 보낼 때 주로 무엇을 하나요?', (
        ('vincent', '그림을 그립니다. 항상 똑같죠.'),
        ('ginoux', '와인을 마시며 책을 읽어요'),
    )),
    ('세 단어로 나를 표현하면?', (
        ('vincent', '해바라기, 별밤, 소용돌이'),
        ('postman', '편지, 수염, 가족'),
    )),
    ('무엇을 더 잘했으면 좋겠습니까?', ()),
    ('램프의 지니가 한 가지 소원을 들어준다면?', (
        ('vincent', '가족과의 관계를 회복하고 싶어요'),
        ('zuave', '이번주 로또 당첨 번호를 알려줘'),
    )),
    ('한 달 동안 여행을 할 수 있다면 어디로 가겠습니까?', ()),
    ('악기를 배운다면 어떤 악기를 배우고 싶나요?', ()),
    ('휴가를 보내는 이상적인 방법은 무엇입니까?', ()),
    ('가장 좋아하는 동물은?', ()),
    ('산타를 몇 살까지 믿었나요?', ()),
    ('내 묘비명은 무엇이 될까요?', ()),
    ('오늘의 달을 올려주세요?', ()),
    ('점심은 무엇을 드셨나요?', ()),
    ('길가에 꽃을 공유해요!', ()),
    ('주변의 녹색을 찾아보아요', ()),
)


def create_users():
    if User.query.count() > 0:
        return
    for user in users:
        db.session.add(user)
    db.session.commit()


def create_relations():
    if Follower.query.count() > 0:
        return
    for followee, followers in relation.items():
        for follower in followers:
            db.session.add(Follower(follower_id=follower.id, uid=followee.id))
            follower.following_count += 1
            followee.follower_count += 1

            db.session.add(follower)
        db.session.add(followee)

    db.session.commit()


def create_question_and_answers(n: int = 180 * 2):
    if Question.query.count() > 0:
        return
    now = datetime.now()
    start_date = -(n / 2)
    for i in range(n):
        question, answers = questions_and_answers[i % len(questions_and_answers)]
        qid = (now + timedelta(days=start_date + i)).date().strftime('%Y-%m-%d')
        db.session.add(Question(id=qid, text=question))
        for answer in answers:
            user = eval(answer[0])
            text = answer[1]
            db.session.add(Answer(uid=user.id, qid=qid, text=text))
            user.answer_count += 1
            db.session.add(user)

        db.session.commit()