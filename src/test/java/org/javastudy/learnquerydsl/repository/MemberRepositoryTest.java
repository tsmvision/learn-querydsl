package org.javastudy.learnquerydsl.repository;

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
@Transactional // automatically rollback
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    public void testFindAll() {

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

        assertEquals(memberRepository.count(), 5);
        assertEquals(teamRepository.count(), 2);

        List<Member> findMember = memberRepository.findByUsername("member1");

        for (Member member : findMember) {
            System.out.println("Team Name = " + member.getTeam().getName());
        }
    }
}