package art.heredium.domain.event.repository;

import art.heredium.domain.event.entity.Event;
import art.heredium.domain.event.model.dto.request.GetAdminEventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface EventRepositoryQueryDsl {
    Page<Event> home(GetAdminEventRequest dto, Pageable pageable);

    Slice<Event> home(Pageable pageable);

    List<Event> home();
}
