package art.heredium.domain.ticket;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.common.model.ProjectInfo;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.program.repository.ProgramRepository;
import art.heredium.domain.ticket.model.TicketInviteCreateInfo;
import art.heredium.domain.ticket.model.TicketInviteInfo;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketInviteRequest;
import art.heredium.domain.ticket.type.TicketKindType;

public enum ProjectRepository {
  EXHIBITION(TicketKindType.EXHIBITION),
  PROGRAM(TicketKindType.PROGRAM),
  COFFEE(TicketKindType.COFFEE);

  private JpaRepository repository;
  private final TicketKindType kindType;

  ProjectRepository(TicketKindType kindType) {
    this.kindType = kindType;
  }

  public static ProjectRepository finder(TicketKindType kind) {
    return Arrays.stream(values())
        .filter(x -> x.kindType == kind)
        .findAny()
        .orElseThrow(() -> new ApiException(ErrorCode.BAD_VALID));
  }

  public TicketInviteCreateInfo toTicketCreateInfo(
      PostAdminTicketInviteRequest dto, List<Account> accounts) {
    TicketInviteInfo info = (TicketInviteInfo) repository.findById(dto.getId()).orElse(null);

    if (accounts.size() != dto.getAccountIds().size() || info == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    TicketInviteCreateInfo ticketCreateInfo = info.getTicketCreateInfo(dto);
    if (ticketCreateInfo.getEndDate().isBefore(Constants.getNow())) {
      throw new ApiException(ErrorCode.BAD_VALID);
    }

    return ticketCreateInfo;
  }

  public ProjectInfo toProjectInfo(Long kindId) {
    ProjectInfo info = (ProjectInfo) repository.findById(kindId).orElse(null);
    return info;
  }

  @Component
  @AllArgsConstructor
  public static class ProjectRepositoryInjector {

    private final ExhibitionRepository exhibitionRepository;
    private final ProgramRepository programRepository;
    private final CoffeeRepository coffeeRepository;

    @PostConstruct
    public void postConstruct() {
      try {
        EXHIBITION.repository = exhibitionRepository;
        PROGRAM.repository = programRepository;
        COFFEE.repository = coffeeRepository;
      } catch (RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
}
