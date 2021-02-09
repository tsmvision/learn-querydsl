package org.javastudy.learnquerydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.javastudy.learnquerydsl.dto.MemberDto;
import org.javastudy.learnquerydsl.dto.QMemberDto;
import org.javastudy.learnquerydsl.dto.UserDto;
import org.javastudy.learnquerydsl.entity.Member;
import org.javastudy.learnquerydsl.entity.QMember;
import org.javastudy.learnquerydsl.entity.QTeam;
import org.javastudy.learnquerydsl.entity.Team;
import org.javastudy.learnquerydsl.repository.MemberRepository;
import org.javastudy.learnquerydsl.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

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

    @Test
    public void search() {
        QMember member = QMember.member;

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10))).fetchOne();

        assertEquals(findMember.getUsername(), "member1");
    }

    @Test
    public void searchAndParam() {
        QMember member = QMember.member;

        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertEquals(findMember.getUsername(), "member1");
    }

    @Test
    public void resultFetch() {

        QMember member = QMember.member;

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // javax.persistence.NonUniqueResultException: query did not return a unique result:
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        // Pagination query
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        long count = results.getTotal();

        assertEquals(count, 5);
        List<Member> content = results.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

        assertEquals(total, 5);
    }

    @Test
    public void sort() {
        QMember member = QMember.member;

        memberRepository.save(new Member(null, 100));
        memberRepository.save(new Member("member6", 100));
        memberRepository.save(new Member("member7", 100));

        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(
                        member.age.desc(), member.username.asc().nullsLast()
                )
                .fetch();

        assertEquals(members.get(0).getUsername(), "member6");
        assertEquals(members.get(1).getUsername(), "member7");
    }

    @Test
    public void paging1() {
        QMember member = QMember.member;
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
    }

    @Test
    public void paging2() {
        QMember member = QMember.member;
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertEquals(result.getTotal(), 5);
        assertEquals(result.getResults().size(), 2);
        assertEquals(result.getOffset(), 1);
        assertEquals(result.getLimit(), 2);
    }

    @Test
    public void aggregation() {

        QMember member = QMember.member;
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertEquals(tuple.get(member.count()), 5);
//        assertEquals(tuple.get(member., 4));
    }

    @Test
    public void group() throws Exception {

        QMember member = QMember.member;
        QTeam team = QTeam.team;
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team)
                .fetch();

        Tuple team1 = result.get(0);
        Tuple team2 = result.get(1);

        assertEquals(team1.get(team.name), "team1");
        assertEquals(team2.get(team.name), "team2");
    }

    @Test
    public void join() {

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("team1"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2", "member3");
    }

    /**
     * check if there are members who have team name same as his/her name. (his/her name == team name)
     * theta join
     */
    @Test
    public void join1() {

        memberRepository.save(new Member("team1", 10));
        memberRepository.save(new Member("team2", 10));

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("team1", "team2");
    }

    @Test
    public void join_on_filtering() {

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("team1"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_with_not_related_entity() {

        memberRepository.save(new Member("team1", 10));
        memberRepository.save(new Member("team2", 10));

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .leftJoin(member)
                .on(member.username.eq(team.name))
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("team1", "team2");
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void withoutFetchJoin() {
        em.flush();
        em.clear();

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isTeamLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(isTeamLoaded).isFalse();
    }

    @Test
    public void withFetchJoin() {
        em.flush();
        em.clear();

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isTeamLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(isTeamLoaded).isTrue();
    }

    @Test
    public void subQuery() {
        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(50);
    }

    @Test
    public void subQuerytGoe() {
        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertEquals(result.size(), 3);
    }

    @Test
    public void subQueryIn() {
        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(30))
                ))
                .fetch();

        assertEquals(result.size(), 3);
    }

    @Test
    public void subQueryInSelect() {
        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(
                        member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // JPA doesn't support subquery from "from" statement as well as queryDSLs
    // Hibernate implementation support subquery at "select" statement,
    // so queryDSL with Hibernate support subquery at "select" statement too.

    @Test
    public void caseStatement() {

        QMember member = QMember.member;

        List<String> result = queryFactory
                .select(member.age
                    .when(10).then("10-year-old")
                    .when(20).then("20-year-old")
                    .otherwise("something-else")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCaseStatement() {

        QMember member = QMember.member;

        List<String> result = queryFactory
                .select(
                        new CaseBuilder()
                            .when(member.age.between(0, 20)).then("from-0-to-20-year-old")
                            .when(member.age.between(21, 30)).then("from-21-to-30-year-old")
                            .otherwise("something-else")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void constant() {
        QMember member = QMember.member;

        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void concat() {
        QMember member = QMember.member;

        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("S = " + s);
        }
    }

    @Test
    public void simpleProjection() {
        QMember member = QMember.member;
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() {
        QMember member = QMember.member;

        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple.get(member.username) + " " + tuple.get(member.age));
        }
    }

    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result =
                em.createQuery("select new org.javastudy.learnquerydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                        .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " +memberDto);
        }
    }

    @Test
    public void findDtoByQueryDSLSetter() {

        QMember member = QMember.member;

        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQueryDSLField() {

        QMember member = QMember.member;

        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByQueryDSLConstructor() {

        QMember member = QMember.member;

        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDtoByQueryDSLField() {

        QMember member = QMember.member;

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDto() {

        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub),
                                "age"
                        )
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findUserDto2() {

        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username.as("name"),
                        member.age)
                )
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection() {
         QMember member = QMember.member;

         List<MemberDto> result = queryFactory
                 .select(new QMemberDto(member.username, member.age))
                 .from(member)
                 .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
}
