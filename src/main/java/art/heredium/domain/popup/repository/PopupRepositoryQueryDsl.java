package art.heredium.domain.popup.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.popup.model.dto.request.GetAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PutAdminPopupOrderRequest;

public interface PopupRepositoryQueryDsl {
  Page<Popup> search(GetAdminPopupRequest dto, Pageable pageable);

  List<Popup> search(PutAdminPopupOrderRequest dto, Long min, Long max);
}
