package art.heredium.controller;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.hanabank.HanaParams;
import art.heredium.hanabank.HanaParamsException;
import art.heredium.hanabank.HanaParamsRequest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping({"/api/test"})
@Profile({"local", "stage"})
public class HanaTestController {
    @Value("${app.hana-bank.partner_key}")
    private String PARTNER_KEY;

    @Value("${app.hana-bank.partner_salt}")
    private String PARTNER_SALT;

    @PostMapping({"/decrypt"})
    public ResponseEntity hanaDecrypt(@RequestBody @Valid HanaParamsRequest dto) {
        String response = this.parseTest(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/encrypt"})
    public ResponseEntity hanaEncrypt(@RequestBody @Valid String data) {
        String nonce = Constants.getNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        Map<String, String> encodeTest = encodeTest(data, nonce);
        return ResponseEntity.ok(encodeTest);
    }

    private Map<String, String> encodeTest(String data, String nonceParam) {
        final String key = PARTNER_KEY;

        final String salt = PARTNER_SALT;
        Map<String, String> message = null;
        try {
            message = HanaParams.encrypt(key, salt, nonceParam, data);
        } catch (HanaParamsException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private String parseTest(HanaParamsRequest dto) {
        String key = this.PARTNER_KEY;
        String salt = this.PARTNER_SALT;

        try {
            String message = HanaParams.parse(key, salt, dto.getMessage(), dto.getMac(), dto.getNonce());
            return message;
        } catch (HanaParamsException var6) {
            throw new ApiException(ErrorCode.BAD_REQUEST, var6);
        }
    }
}
