package art.heredium.ncloud.service.sens.sms.model.kit;

import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSFile;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSMessage;
import art.heredium.ncloud.service.sens.sms.model.ncloud.NCloudSMSRequestModel;
import com.amazonaws.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import art.heredium.ncloud.error.NCloudKitErrorCode;
import art.heredium.ncloud.error.NCloudKitException;
import art.heredium.ncloud.service.sens.sms.type.NCloudSMSContentType;
import art.heredium.ncloud.service.sens.sms.type.NCloudSMSType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NCloudSMSBuilder extends NCloudSMSRequestModel {

    public NCloudSMSBuilder() {

    }

    public NCloudSMSBuilder type(NCloudSMSType type){
        this.type = type.name();
        return this;
    }

    public NCloudSMSBuilder contentType(NCloudSMSContentType contentType){
        this.contentType = contentType.name();
        return this;
    }

    /**
     * @link <a href="https://guide.ncloud-docs.com/docs/sens-sens-1-6">...</a>
     * @param countryCode : 국가 코드
     * @return
     */
    public NCloudSMSBuilder countryCode(String countryCode){
        this.countrycode = countryCode;
        return this;
    }

    public NCloudSMSBuilder from(String from){
        this.from = from;
        return this;
    }

    public NCloudSMSBuilder subject(String subject){
        this.subject = subject;
        return this;
    }

    public NCloudSMSBuilder content(String content){
        this.content = content;
        return this;
    }

    public NCloudSMSBuilder messages(List<NCloudSMSMessage> messages){
        this.messages = messages;
        return this;
    }

    /**
     * 기본 타임존인 Asia/Seoul을 사용할 경우
     * @param reserveTime : 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)
     * @return
     */
    public NCloudSMSBuilder useReserve(String reserveTime){
        this.reserveTime = reserveTime;
        return this;
    }

    /**
     * @link 지원 타임존 목록 : https://en.wikipedia.org/wiki/List_of_tz_database_time_zones, TZ database name 값 사용
     * @param reserveTime : 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)
     * @param reserveTimeZone : - 예약 일시 타임존 (기본: Asia/Seoul)
     * @return
     */
    public NCloudSMSBuilder useReserve(String reserveTime, String reserveTimeZone){
        this.reserveTime = reserveTime;
        this.reserveTimeZone = reserveTimeZone;
        return this;
    }

    /**
     *
     * @param scheduleCode : NCloud 관리자에서 {Service}-Reservation 메뉴에서 스케줄 생성한 코드
     * @return
     */
    public NCloudSMSBuilder useSchedule(String scheduleCode){
        this.scheduleCode = scheduleCode;
        return this;
    }

    /**
     * MMS 필수
     * 해상도는 최대 1500 * 1440, 공백 사용 불가
     * @param name : 파일명 최대 40자
     * @param base64Body : jpg, jpeg Base64 인코딩 값, 최대 300Kbyte,
     * @return
     */
    public NCloudSMSBuilder useFile(String name, String base64Body){
        if(!this.type.equals("MMS")){
            throw new NCloudKitException(NCloudKitErrorCode.SMS_TYPE_INVALID, "타입이 MMS여야만 합니다.");
        }

        if(name.length() > 40){
            throw new NCloudKitException(NCloudKitErrorCode.SMS_FILE_LENGTH, "파일명은 최대 40자까지 지원합니다.");
        }

        NCloudSMSFile file = new NCloudSMSFile(name, base64Body);
        if(this.files == null)
            this.files = new ArrayList<>();
        this.files.add(file);
        return this;
    }

    public NCloudSMSRequest build() {
        if(!StringUtils.isNullOrEmpty(this.reserveTime) || !StringUtils.isNullOrEmpty(this.scheduleCode)){
            if(StringUtils.isNullOrEmpty(this.content)){
                throw new NCloudKitException(NCloudKitErrorCode.SMS_CONTENT_INVALID, "예약 발송시에는 Root의 Content가 존재하여야 합니다.");
            }
        }
        return new NCloudSMSRequest(this);
    }
}
