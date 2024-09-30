package art.heredium.domain.popup.repository;

import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.popup.model.dto.request.GetAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PutAdminPopupOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PopupRepositoryQueryDsl {
    Page<Popup> search(GetAdminPopupRequest dto, Pageable pageable);

    List<Popup> search(PutAdminPopupOrderRequest dto, Long min, Long max);
}
