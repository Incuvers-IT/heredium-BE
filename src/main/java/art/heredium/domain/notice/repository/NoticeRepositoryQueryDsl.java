package art.heredium.domain.notice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.notice.model.dto.request.GetAdminNoticeRequest;

public interface NoticeRepositoryQueryDsl {
  Page<Notice> search(GetAdminNoticeRequest dto, Pageable pageable);

  Page<Notice> search(Pageable pageable);

  List<Notice> home();
}
