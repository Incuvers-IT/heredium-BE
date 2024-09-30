package art.heredium.domain.coffee.repository;

import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.coffee.model.dto.request.GetAdminCoffeeRequest;
import art.heredium.domain.coffee.model.dto.request.GetUserCoffeeRequest;
import art.heredium.domain.coffee.model.dto.response.GetAdminCoffeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface CoffeeRepositoryQueryDsl {
    Page<GetAdminCoffeeResponse> search(GetAdminCoffeeRequest dto, Pageable pageable);

    Slice<Coffee> search(GetUserCoffeeRequest dto, Pageable pageable);

    List<Coffee> searchByHome();
}
