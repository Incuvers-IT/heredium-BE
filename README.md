# 헤레디움 ERD
[Membership - coupon ERD.pdf](https://github.com/user-attachments/files/19808735/Membership.-.coupon.ERD.pdf)

# heredium-BE

# Alimtalk template

### COUPON_HAS_BEEN_USED

헤레디움
[멤버십 사용 안내] 안녕하세요 #{accountName}님 헤레디움 #{membershipName} 멤버십 이용 내역 안내입니다.

■ #{membershipName} 멤버십 이용 내역 :
● 이용 일시 : #{issuedDate}
● 이용 혜택 : #{issuedCouponName}

■ #{membershipName}잔여 혜택
● #{remainedDetailCoupons}
클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 멤버십을 통해 더욱 다양한 전시와 공연을 만나보세요!

### COUPON_HAS_BEEN_DELIVERED

헤레디움 [쿠폰 발급 안내]

안녕하세요 #{accountName}님
헤레디움 #{couponType} 쿠폰이 발급되었습니다.

[#{couponType} 쿠폰]

- 쿠폰명 : #{couponName}. #{numberOfUses}
- 쿠폰혜택 : #{couponType} #{disCountPercent} : #{numberOfUses}
- 사용기한 : #{couponStartDate} ~ #{couponEndDate}

클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 다양한 전시와 공연을 만나보세요!

위 알림톡은 헤레디움 고객 대상으로 지급된 쿠폰 안내 메시지입니다.

### COUPON_HAS_BEEN_ISSUED_V4

헤레디움
[쿠폰 발급 안내]
안녕하세요 #{accountName}님, 헤레디움 #{couponType} 쿠폰이 발급되었습니다.

#{couponType} 쿠폰

- 쿠폰명 : #{couponName}
- 쿠폰혜택 : #{couponType} #{discountPercent} #{numberOfUses}
- 사용기한 : #{couponStartDate} ~ #{couponEndDate}

클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 다양한 전시와 공연을 만나보세요!

위 알림톡은 헤레디움 고객님의 동의에 의해 지급된 쿠폰 발급 메시지입니다.

Example:

[//]: # "헤레디움"
[//]: # "[쿠폰 발급 안내]"
[//]: # "안녕하세요 김예람님, 헤레디움 커피 쿠폰이 발급되었습니다."
[//]: # "[커피 쿠폰]"
[//]: # "- 쿠폰명 : [설정한 쿠폰명]"
[//]: # "- 쿠폰혜택 : [종류] [할인율] [횟수]"
[//]: # "- 사용기한 : yyyy-mm-dd ~ yyyy-mm-dd"
[//]: # "클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 다양한 전시와 공연을 만나보세요!"
[//]: # "위 알림톡은 헤레디움 고객님의 동의에 의해 지급된 쿠폰 발급 메시지입니다."
