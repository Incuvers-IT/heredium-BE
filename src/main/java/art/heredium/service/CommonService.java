package art.heredium.service;

import art.heredium.domain.common.model.dto.response.*;
import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.notice.repository.NoticeRepository;
import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.common.model.dto.request.GetUserCommonSearchRequest;
import art.heredium.domain.common.repository.CommonRepository;
import art.heredium.domain.event.entity.Event;
import art.heredium.domain.event.repository.EventRepository;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.popup.repository.PopupRepository;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.repository.ProgramRepository;
import art.heredium.domain.slide.entity.Slide;
import art.heredium.domain.slide.repository.SlideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CommonService {

    private final CommonRepository commonRepository;
    private final PopupRepository popupRepository;
    private final SlideRepository slideRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ProgramRepository programRepository;
    private final CoffeeRepository coffeeRepository;
    private final EventRepository eventRepository;
    private final NoticeRepository noticeRepository;

    public List<GetUserCommonSearchIndexResponse> searchIndex(String text) {
        return commonRepository.searchIndex(text);
    }

    public Page<GetUserCommonSearchContentResponse> searchContent(GetUserCommonSearchRequest dto, Pageable pageable) {
        return commonRepository.searchContent(dto, pageable);
    }


    public GetUserCommonHomeResponse home() {
        List<Popup> popups = popupRepository.findPostingByUser();
        List<Slide> slides = slideRepository.findPostingByUser();
        List<Exhibition> exhibitions = exhibitionRepository.searchByHome();
        List<Program> programs = programRepository.searchByHome();
        List<Event> events = eventRepository.home();
        List<Notice> notices = noticeRepository.home();
        return new GetUserCommonHomeResponse(popups, slides, exhibitions, programs, events, notices);
    }

    public GetUserCommonHomeAppResponse homeApp() {
        List<Popup> popups = popupRepository.findPostingByUser();
        List<Slide> slides = slideRepository.findPostingByUser();
        return new GetUserCommonHomeAppResponse(popups, slides);
    }

    public List<GetAdminAppProjectResponse> projects() {
        List<Exhibition> exhibitions = exhibitionRepository.findAllByProgress();
        List<Program> programs = programRepository.findAllByProgress();
        List<Coffee> coffees = coffeeRepository.findAllByProgress();

        List<GetAdminAppProjectResponse> response = new ArrayList<>();
        response.addAll(exhibitions.stream().map(GetAdminAppProjectResponse::new).collect(Collectors.toList()));
        response.addAll(programs.stream().map(GetAdminAppProjectResponse::new).collect(Collectors.toList()));
        response.addAll(coffees.stream().map(GetAdminAppProjectResponse::new).collect(Collectors.toList()));
        return response;
    }
}