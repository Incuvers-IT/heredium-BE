package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.AppProperties;
import art.heredium.core.jwt.AuthToken;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.jwt.MailTokenProvider;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.model.dto.request.*;
import art.heredium.domain.account.model.dto.response.GetAdminAccountInfoResponse;
import art.heredium.domain.account.model.dto.response.GetAdminResponse;
import art.heredium.domain.account.repository.AdminInfoRepository;
import art.heredium.domain.account.repository.AdminRepository;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.ticket.repository.TicketLogRepository;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.type.MailTemplate;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.niceId.service.NiceIdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AdminService {

    private final AdminRepository adminRepository;
    private final AdminInfoRepository adminInfoRepository;
    private final LogRepository logRepository;
    private final TicketLogRepository ticketLogRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final NiceIdService niceIdService;
    private final AppProperties appProperties;
    private final CloudMail cloudMail;
    private final MailTokenProvider mailTokenProvider;
    private final JwtRedisUtil jwtRedisUtil;

    public Page<GetAdminResponse> list(Pageable pageable) {
        return adminRepository.search(pageable).map(GetAdminResponse::new);
    }

    public GetAdminResponse detail(Long id) {
        Admin entity = adminRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        return new GetAdminResponse(entity);
    }

    public boolean insert(PostAdminRequest dto, boolean isInit) {
        if (isExistEmail(dto.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        Admin entity = new Admin(dto, bCryptPasswordEncoder.encode(dto.getPassword()));
        adminRepository.save(entity);
        if (!isInit) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));
        }
        return true;
    }

    public boolean update(Long id, PutAdminRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Admin entity = adminRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        if (!entity.getEmail().equals(dto.getEmail()) && isExistEmail(dto.getEmail())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        entity.update(dto);
        logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));
        return true;
    }

    public boolean delete(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Admin entity = adminRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
        logRepository.setAdminNull(id);
        ticketLogRepository.setAdminNull(id);
        adminRepository.delete(entity);
        return true;
    }

    public boolean password(PutAdminPasswordRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Admin entity = adminRepository.findById(userPrincipal.getId()).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), userPrincipal.getPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_NOT_MATCHED);
        }
        entity.updatePassword(dto.getChangePassword(), bCryptPasswordEncoder.encode(dto.getChangePassword()));
        jwtRedisUtil.deleteData(entity.getEmail() + false); //로그인 실패 카운트 초기화
        return true;
    }

    public boolean password(Long id, PutAccountPasswordRequest dto) {
        Admin entity = adminRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        entity.updatePassword(dto.getPassword(), bCryptPasswordEncoder.encode(dto.getPassword()));
        jwtRedisUtil.deleteData(entity.getEmail() + true); //로그인 실패 카운트 초기화
        return true;
    }

    public GetAdminAccountInfoResponse info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return new GetAdminAccountInfoResponse(userPrincipal.getAdmin());
    }

    public boolean isExistEmail(String email) {
        return adminRepository.existsAdminByEmail(email);
    }

    public List<String> findId(String encodeData) {
        PostNiceIdEncryptResponse info = niceIdService.decrypt(encodeData);
        List<String> emails = adminInfoRepository.findEmailByPhone(info.getMobileNo());
        return emails.stream().map(Constants::emailMasking)
                .collect(Collectors.toList());
    }

    public String findPwByPhone(String email, String encodeData) {
        PostNiceIdEncryptResponse info = niceIdService.decrypt(encodeData);
        boolean isExist = adminRepository.existsByEmailAndAdminInfo_Phone(email, info.getMobileNo());
        if (!isExist) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        AuthToken authToken = mailTokenProvider.createAuthToken(email, new Date(new Date().getTime() + appProperties.getAuth().getMailTokenExpiry().toMillis()));
        jwtRedisUtil.setDataExpire(authToken.getToken(), email, appProperties.getAuth().getMailTokenExpiry().getSeconds());
        return authToken.getToken();
    }

    public boolean findPwByEmail(String email, String redirectUrl) {
        boolean isExist = adminRepository.existsAdminByEmail(email);
        if (!isExist) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        AuthToken authToken = mailTokenProvider.createAuthToken(email, new Date(new Date().getTime() + appProperties.getAuth().getMailTokenExpiry().toMillis()));
        redirectUrl += ("?token=" + authToken.getToken());
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> params = new HashMap<>();
        params.put("link", redirectUrl);
        params.put("expired_date", formatter.format(authToken.getExpiration()));
        cloudMail.mail(email, params, MailTemplate.PASSWORD_CHANGE_ADMIN);
        jwtRedisUtil.setDataExpire(authToken.getToken(), email, appProperties.getAuth().getMailTokenExpiry().getSeconds());
        return true;
    }

    public boolean changePw(PostAuthFindPwRequest dto) {
        AuthToken authToken = mailTokenProvider.convertAuthToken(dto.getToken());
        if (!authToken.validate() || authToken.isExpired()) {
            throw new ApiException(ErrorCode.BAD_VALID);
        }
        String email = authToken.getTokenClaims().getSubject();
        if (jwtRedisUtil.getData(authToken.getToken()) == null) {
            throw new ApiException(ErrorCode.BAD_VALID);
        }
        Admin admin = adminRepository.findByEmailEquals(email);
        if (admin == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        admin.updatePassword(dto.getPassword(), bCryptPasswordEncoder.encode(dto.getPassword()));
        jwtRedisUtil.deleteData(authToken.getToken());
        jwtRedisUtil.deleteData(email + true); //로그인 실패 카운트 초기화
        return true;
    }
}