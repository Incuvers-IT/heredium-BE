# heredium-BE

# Alimtalk template

### USER_REGISTER_MEMBERSHIP_PACKAGE

[멤버십 가입 안내]  
안녕하세요 #{accountName}님
헤레디움 멤버십 가입을 환영합니다.

[#{membershipName} 멤버십]  
멤버십 적용 기간 : #{startDate} ~ #{endDate}

[멤버십 혜택]
#{detailCoupons}

[//]: # "- 쿠폰명 : #{couponName}"
[//]: # "  할인혜택 : #{couponType}, {#{disCountPercent}%, #{무료}}"
[//]: # "  사용횟수 : {#{numberOfUses}회, #{상시할인}}"

클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 멤버십을 통해 더욱 다양한 전시와 공연을 만나보세요!

### COUPON_HAS_BEEN_USED

헤레디움
[멤버십 사용 안내] 안녕하세요 #{accountName}님 헤레디움 #{membershipName} 멤버십 이용 내역 안내입니다.

■ #{membershipName} 멤버십 이용 내역 :
● 이용 일시 : #{issuedDate}
● 이용 혜택 : #{issuedCouponName}

■ #{membershipName}잔여 혜택
● #{remainedDetailCoupons}
클래식 음악과 컨템포러리 아트가 함께하는 헤레디움, 멤버십을 통해 더욱 다양한 전시와 공연을 만나보세요!

### MEMBERSHIP_PACKAGE_HAS_EXPIRED

헤레디움 [멤버십 만료 안내]

안녕하세요 #{accountName}님
헤레디움 멤버십 #{membershipName}이 만료되었습니다.

[#{membershipName}] 멤버십 적용: #{startDate} ~ #{endDate}

이 메시지는 고객님의 헤레디움 멤버십 기한 만료 안내를 위해 발송되었습니다.

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
