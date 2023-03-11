package com.runningwith.domain.main;

import com.runningwith.domain.study.StudyEntity;
import com.runningwith.domain.study.StudyRepository;
import com.runningwith.domain.users.CurrentUser;
import com.runningwith.domain.users.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static com.runningwith.infra.utils.WebUtils.URL_ROOT;
import static com.runningwith.infra.utils.WebUtils.VIEW_INDEX;

@Controller
@RequiredArgsConstructor
public class MainController {

    public static final String URL_LOGIN = "/login";
    public static final String PAGE_LOGIN = "login";
    public static final String URL_SEARCH_STUDY = "/search/study";
    public static final String VIEW_SEARCH = "search";
    private final StudyRepository studyRepository;

    @GetMapping(URL_ROOT)
    public String index(@CurrentUser UsersEntity usersEntity, Model model) {
        if (usersEntity != null) {
            model.addAttribute("user", usersEntity);
        }
        return VIEW_INDEX;
    }

    @GetMapping(URL_LOGIN)
    public String login(@CurrentUser UsersEntity usersEntity) {
        if (usersEntity != null) {
            return VIEW_INDEX;
        }
        return PAGE_LOGIN;
    }

    @GetMapping(URL_SEARCH_STUDY)
    public String searchStudy(String keyword, Model model) {
        List<StudyEntity> studyList = studyRepository.findByKeyword(keyword);

        model.addAttribute("studyList", studyList);
        model.addAttribute("keyword", keyword);

        return VIEW_SEARCH;
    }
}
