package art.heredium.controller.user;

import art.heredium.domain.common.model.dto.request.GetUserCommonSearchRequest;
import art.heredium.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/common")
public class UserCommonController {

    private final CommonService commonService;

    @GetMapping("/search/index")
    public ResponseEntity searchIndex(@RequestParam(value = "text", required = false) String text) {
        return ResponseEntity.ok(commonService.searchIndex(text));
    }

    @GetMapping("/search/content")
    public ResponseEntity searchContent(@Valid GetUserCommonSearchRequest dto, Pageable pageable) {
        return ResponseEntity.ok(commonService.searchContent(dto, pageable));
    }

    @GetMapping("/home")
    public ResponseEntity home() {
        return ResponseEntity.ok(commonService.home());
    }

    @GetMapping("/home/app")
    public ResponseEntity homeApp() {
        return ResponseEntity.ok(commonService.homeApp());
    }
}