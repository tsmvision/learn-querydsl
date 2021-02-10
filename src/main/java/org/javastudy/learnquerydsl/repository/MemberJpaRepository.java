package org.javastudy.learnquerydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.javastudy.learnquerydsl.dto.MemberSearchCondition;
import org.javastudy.learnquerydsl.dto.MemberTeamDto;
import org.javastudy.learnquerydsl.dto.QMemberTeamDto;
import org.javastudy.learnquerydsl.entity.Member;
import org.javastudy.learnquerydsl.entity.QMember;
import org.javastudy.learnquerydsl.entity.QTeam;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {

        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {

        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_Querydsl() {

        QMember member = QMember.member;
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {

        QMember member = QMember.member;
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .where(builder)
                .leftJoin(member.team, team)
                .fetch();

    }

    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        QMember member = QMember.member;
        QTeam team = QTeam.team;

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .leftJoin(member.team, team)
                .fetch();
    }

    private BooleanExpression ageBetween(Integer ageLoe, Integer ageGoe) {

        if (ageLoe != null && ageGoe != null) {
            return ageLoe(ageLoe).and(ageGoe(ageGoe));
        }
        else if (ageLoe != null) {
            return ageLoe(ageLoe);
        }
        return ageGoe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {

        QMember member = QMember.member;
        return (ageLoe != null) ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {

        QMember member = QMember.member;
        return (ageGoe != null) ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {

        QMember member = QMember.member;
        return (teamName == null || teamName.length() == 0) ? null : member.team.name.eq(teamName);
    }

    private BooleanExpression usernameEq(String username) {

        QMember member = QMember.member;
        return ( username == null || username.length() == 0) ? null : member.username.eq(username);
    }
}
