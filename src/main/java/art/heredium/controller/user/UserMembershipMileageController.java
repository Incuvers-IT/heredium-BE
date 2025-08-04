package art.heredium.controller.user;

import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.membership.model.dto.request.MembershipMileageSearchRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileagePage;
import art.heredium.domain.program.repository.ProgramRepository;
import art.heredium.service.MembershipMileageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/membershipMileage")
public class UserMembershipMileageController {

    private final MembershipMileageService membershipMileageService;
    private final ExhibitionRepository exhibitionRepo;
    private final ProgramRepository programRepo;
    private final CoffeeRepository coffeeRepo;

    @GetMapping("/{accountId}")
    public ResponseEntity<MembershipMileagePage> getMembershipsMileageList(
            @PathVariable Long accountId,
            @Valid MembershipMileageSearchRequest request,
            Pageable pageable) {

        request.setAccountId(accountId);

        MembershipMileagePage response =
                membershipMileageService.getMembershipsMileageListWithTotal(request, pageable);

        return ResponseEntity.ok(response);
    }
}