package art.heredium.domain.ticket;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.coffee.repository.CoffeeRoundRepository;
import art.heredium.domain.exhibition.repository.ExhibitionRoundRepository;
import art.heredium.domain.program.repository.ProgramRoundRepository;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketInfo;
import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketGroupRequest;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;

public enum ProjectRounderRepository {
    EXHIBITION(TicketKindType.EXHIBITION),
    PROGRAM(TicketKindType.PROGRAM),
    COFFEE(TicketKindType.COFFEE);

    private JpaRepository repository;
    private final TicketKindType kindType;

    ProjectRounderRepository(TicketKindType kindType) {
        this.kindType = kindType;
    }

    public static ProjectRounderRepository finder(TicketKindType kind) {
        return Arrays.stream(values())
                .filter(x -> x.kindType == kind)
                .findAny()
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_VALID));
    }

    public TicketCreateInfo toTicketCreateInfo(TicketOrderInfo dto) {
        TicketInfo info = (TicketInfo) repository.findById(dto.getRoundId()).orElse(null);

        // 전시가 열려있는가?
        if (info == null || !info.isEnabledTicket())
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        return info.getTicketCreateInfo(dto);
    }


    public TicketCreateInfo toTicketCreateInfo(PostAdminTicketGroupRequest dto) {
        TicketInfo info = (TicketInfo) repository.findById(dto.getRoundId()).orElse(null);

        // 전시가 열려있는가?
        if (info == null || !info.isEnabledTicket())
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        return info.getTicketCreateInfo(dto);
    }

    @Component
    @AllArgsConstructor
    public static class ProjectRounderRepositoryInjector {

        private final ExhibitionRoundRepository exhibitionRoundRepository;
        private final ProgramRoundRepository programRoundRepository;
        private final CoffeeRoundRepository coffeeRoundRepository;

        @PostConstruct
        public void postConstruct() {
            try {
                EXHIBITION.repository = exhibitionRoundRepository;
                PROGRAM.repository = programRoundRepository;
                COFFEE.repository = coffeeRoundRepository;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}
