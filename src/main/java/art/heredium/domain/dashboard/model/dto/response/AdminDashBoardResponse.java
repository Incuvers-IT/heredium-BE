package art.heredium.domain.dashboard.model.dto.response;

import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AdminDashBoardResponse {
    private LocalDate date;
    private Count count;
    private List<TodayProject> todayProjects;

    @Getter
    @Setter
    public static class Count {
        private Integer visitCount;
        private Integer totalCount;
        private Integer saleCount;
        private Long salePrice;
        private Long refundCount;
        private Long refundPrice;
        private Long newRegister;

        public Count(Integer visitCount, Integer totalCount, Integer saleCount, Long salePrice, Long refundCount, Long refundPrice, Long newRegister) {
            this.visitCount = visitCount;
            this.totalCount = totalCount;
            this.saleCount = saleCount;
            this.salePrice = salePrice;
            this.refundCount = refundCount;
            this.refundPrice = refundPrice;
            this.newRegister = newRegister;
        }
    }

    @Getter
    @Setter
    public static class TodayProject {
        private Long id;
        private TicketKindType type;
        private String title;

        public TodayProject(Exhibition entity) {
            this.id = entity.getId();
            this.type = TicketKindType.EXHIBITION;
            this.title = entity.getTitle();
        }

        public TodayProject(Program entity) {
            this.id = entity.getId();
            this.type = TicketKindType.PROGRAM;
            this.title = entity.getTitle();
        }
    }

    public AdminDashBoardResponse(LocalDate date, Count count, List<TodayProject> todayProjects) {
        this.date = date;
        this.count = count;
        this.todayProjects = todayProjects;
    }
}