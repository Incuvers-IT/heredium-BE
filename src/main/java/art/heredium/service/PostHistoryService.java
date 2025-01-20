package art.heredium.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.entity.PostHistory;
import art.heredium.domain.post.model.dto.response.AdminPostDetailsResponse;
import art.heredium.domain.post.model.dto.response.PostHistoryBaseResponse;
import art.heredium.domain.post.model.dto.response.PostHistoryResponse;
import art.heredium.domain.post.repository.PostHistoryRepository;
import art.heredium.domain.post.repository.PostHistoryRepositoryImpl;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostHistoryService {
  private final PostHistoryRepository postHistoryRepository;
  private final PostHistoryRepositoryImpl postHistoryRepositoryImpl;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Transactional(rollbackFor = Exception.class)
  public PostHistory save(PostHistory entity) {
    return this.postHistoryRepository.save(entity);
  }

  public Page<PostHistoryBaseResponse> listPostHistory(
      LocalDateTime modifyDateFrom,
      LocalDateTime modifyDateTo,
      String modifyUser,
      Pageable pageable) {
    return this.postHistoryRepositoryImpl.search(
        modifyDateFrom, modifyDateTo, modifyUser, pageable);
  }

  public PostHistoryResponse getPostHistory(Long postHistoryId) {
    PostHistory postHistory =
        this.postHistoryRepository
            .findById(postHistoryId)
            .orElseThrow(
                () -> new ApiException(ErrorCode.POST_HISTORY_NOT_FOUND, "Post history not found"));
    AdminPostDetailsResponse content = null;
    try {
      content =
          this.objectMapper.readValue(postHistory.getPostContent(), AdminPostDetailsResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Error serialize AdminPostDetailsResponse");
    }
    return PostHistoryResponse.builder()
        .postHistoryId(postHistory.getId())
        .modifiedDate(postHistory.getLastModifiedDate())
        .modifyUserEmail(postHistory.getModifyUserEmail())
        .modifyUserName(postHistory.getLastModifiedName())
        .content(content)
        .build();
  }
}
