package org.javastudy.learnquerydsl.controller;

import lombok.RequiredArgsConstructor;
import org.javastudy.learnquerydsl.dto.MemberSearchCondition;
import org.javastudy.learnquerydsl.dto.MemberTeamDto;
import org.javastudy.learnquerydsl.entity.Member;
import org.javastudy.learnquerydsl.repository.MemberJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;

    @GetMapping
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }
}
