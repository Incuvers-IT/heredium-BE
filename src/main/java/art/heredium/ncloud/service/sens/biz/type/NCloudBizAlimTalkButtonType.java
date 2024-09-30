package art.heredium.ncloud.service.sens.biz.type;

public enum NCloudBizAlimTalkButtonType {
    DS("배송조회"),
    WL("웹 링크"), // Mandatory : linkMobile, linkPc
    AL("앱 링크"), // Mandatory : schemeIos, schemeAndroid
    BK("봇 키워드"),
    MD("메시지 전달"),
    AC("채널 추가"), // Mandatory : 버튼 명은 채널 추가로 고정
    ;

    private final String name;

    NCloudBizAlimTalkButtonType(String name) {
        this.name = name;
    }
}
