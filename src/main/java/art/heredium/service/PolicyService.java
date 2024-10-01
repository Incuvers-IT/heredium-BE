package art.heredium.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.policy.entity.Policy;
import art.heredium.domain.policy.model.dto.request.GetAdminPolicyPostCheckRequest;
import art.heredium.domain.policy.model.dto.request.GetAdminPolicyRequest;
import art.heredium.domain.policy.model.dto.request.PostAdminPolicyRequest;
import art.heredium.domain.policy.model.dto.response.GetAdminPolicyDetailResponse;
import art.heredium.domain.policy.model.dto.response.GetAdminPolicyResponse;
import art.heredium.domain.policy.model.dto.response.GetUserPolicyDetailResponse;
import art.heredium.domain.policy.model.dto.response.GetUserPolicyResponse;
import art.heredium.domain.policy.repository.PolicyRepository;
import art.heredium.domain.policy.type.PolicyType;
import art.heredium.ncloud.bean.CloudStorage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PolicyService {

  private final CloudStorage cloudStorage;
  private final PolicyRepository policyRepository;
  private final LogRepository logRepository;

  public Page<GetAdminPolicyResponse> list(@Valid GetAdminPolicyRequest dto, Pageable pageable) {
    GetUserPolicyDetailResponse posting = this.posting(dto.getType());
    return policyRepository
        .search(dto, pageable)
        .map(x -> new GetAdminPolicyResponse(x, posting != null ? posting.getId() : null));
  }

  public GetAdminPolicyDetailResponse detail(Long id) {
    Policy entity = policyRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    return new GetAdminPolicyDetailResponse(entity);
  }

  public boolean insert(PostAdminPolicyRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Policy entity = new Policy(dto);
    policyRepository.saveAndFlush(entity);
    logRepository.save(entity.createInsertLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    policyRepository.flush();
    return true;
  }

  public boolean update(Long id, PostAdminPolicyRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Policy entity = policyRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    List<String> removeFiles = entity.getRemoveFile(dto);
    entity.update(dto);
    logRepository.saveAndFlush(entity.createUpdateLog(userPrincipal.getAdmin()));

    entity.applyTempContents(cloudStorage);
    policyRepository.flush();

    cloudStorage.delete(removeFiles);
    return true;
  }

  public boolean delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Policy entity = policyRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    policyRepository.delete(entity);
    logRepository.saveAndFlush(entity.createDeleteLog(userPrincipal.getAdmin()));
    cloudStorage.deleteFolder(entity.getFileFolderPath());
    return true;
  }

  public GetUserPolicyDetailResponse posting(PolicyType type) {
    Policy entity = policyRepository.findPosting(type.getCode());
    return entity != null ? new GetUserPolicyDetailResponse(entity) : null;
  }

  public List<GetUserPolicyResponse> listByUser(PolicyType type) {
    return policyRepository.listByUser(type.getCode()).stream()
        .map(GetUserPolicyResponse::new)
        .collect(Collectors.toList());
  }

  public GetUserPolicyDetailResponse detailByUser(Long id) {
    Policy entity = policyRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    return new GetUserPolicyDetailResponse(entity);
  }

  public boolean postCheck(GetAdminPolicyPostCheckRequest dto) {
    Policy entity = policyRepository.findPosting(dto.getType().getCode());
    LocalDateTime now = Constants.getNow();
    return (!dto.getPostDate().isAfter(now)
        && (entity == null || !entity.getPostDate().isAfter(dto.getPostDate())));
  }
}
