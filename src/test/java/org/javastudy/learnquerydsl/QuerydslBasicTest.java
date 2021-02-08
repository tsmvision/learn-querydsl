package org.javastudy.learnquerydsl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.javastudy.learnquerydsl.entity.Member;
import org.javastudy.learnquerydsl.entity.QMember;
import org.javastudy.learnquerydsl.entity.Team;
import org.javastudy.learnquerydsl.repository.MemberRepository;
import org.javastudy.learnquerydsl.repository.TeamRepository;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Autowired MemberRepository memberRepository;

    @Autowired TeamRepository teamRepository;

    @BeforeEach
       public void beforeEach() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() {
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertEquals(findMember.getUsername(), "member1");
    }

    @Test
    public void startQuertdsl() {
        QMember member = QMember.member;

        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertEquals(member1.getUsername(), "member1");
    }
}
