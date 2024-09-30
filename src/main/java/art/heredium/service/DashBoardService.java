package art.heredium.service;

import art.heredium.core.util.Constants;
import art.heredium.domain.dashboard.model.dto.response.AdminDashBoardResponse;
import art.heredium.domain.dashboard.repository.DashboardRepository;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class DashBoardService {

    private final DashboardRepository dashboardRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ProgramRepository programRepository;

    public AdminDashBoardResponse dashboard() {
        LocalDate now = Constants.getNow().toLocalDate();
        List<Exhibition> exhibitions = exhibitionRepository.findAllByProgress();
        List<Program> programs = programRepository.findAllByProgress();
        List<AdminDashBoardResponse.TodayProject> todayProjects = new ArrayList<>();
        todayProjects.addAll(exhibitions.stream().map(AdminDashBoardResponse.TodayProject::new).collect(Collectors.toList()));
        todayProjects.addAll(programs.stream().map(AdminDashBoardResponse.TodayProject::new).collect(Collectors.toList()));
        return new AdminDashBoardResponse(now, dashboardRepository.dashboard(now), todayProjects);
    }
}