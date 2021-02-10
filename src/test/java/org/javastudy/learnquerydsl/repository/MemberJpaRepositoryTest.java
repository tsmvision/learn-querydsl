package org.javastudy.learnquerydsl.repository;

import org.javastudy.learnquerydsl.dto.MemberSearchCondition;
import org.javastudy.learnquerydsl.dto.MemberTeamDto;
import org.javastudy.learnquerydsl.entity.Member;
import org.javastudy.learnquerydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertEquals(member, findMember);

        List<Member> result1 = memberJpaRepository.findAll();
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0), member);
    }

    @Test
    public void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertEquals(member, findMember);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertEquals(result1.size(), 1);
        assertEquals(result1.get(0), member);
    }

    @Test
    public void searchText() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        Member member3 = new Member("member3", 30);
        Member member4 = new Member("member4", 40);
        Member member5 = new Member("member5", 50);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);

        Team team1 = new Team("team1");
        Team team2 = new Team("team2");

        teamRepository.save(team1);
        teamRepository.save(team2);

        member1.changeTeam(team1);
        member2.changeTeam(team1);
        member3.changeTeam(team1);
        member4.changeTeam(team2);
        member5.changeTeam(team2);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(45);
        condition.setTeamName("team2");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        assertEquals(result.get(0).getUsername(), "member4");
    }

    @Test
    public void searchWhereDynamicQueries() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 20);
        Member member3 = new Member("member3", 30);
        Member member4 = new Member("member4", 40);
        Member member5 = new Member("member5", 50);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);

        Team team1 = new Team("team1");
        Team team2 = new Team("team2");

        teamRepository.save(team1);
        teamRepository.save(team2);

        member1.changeTeam(team1);
        member2.changeTeam(team1);
        member3.changeTeam(team1);
        member4.changeTeam(team2);
        member5.changeTeam(team2);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(45);
        condition.setTeamName("team2");

        List<MemberTeamDto> result = memberJpaRepository.search(condition);

        assertEquals(result.get(0).getUsername(), "member4");
    }
}