package art.heredium.service;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.post.entity.PostHistory;
import art.heredium.domain.post.repository.PostHistoryRepository;

@Service
@RequiredArgsConstructor
public class PostHistoryService {
  private PostHistoryRepository postHistoryRepository;

  @Async
  @Transactional(rollbackFor = Exception.class)
  public void save(PostHistory entity) {
    this.postHistoryRepository.save(entity);
  }
}
