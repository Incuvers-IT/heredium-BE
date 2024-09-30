package art.heredium.domain.notice.repository;

import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.notice.model.dto.request.GetAdminNoticeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeRepositoryQueryDsl {
    Page<Notice> search(GetAdminNoticeRequest dto, Pageable pageable);

    Page<Notice> search(Pageable pageable);

    List<Notice> home();
}
