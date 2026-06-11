package smartlib;

import org.junit.jupiter.api.Test;
import smartlib.domain.BorrowingPolicy;
import smartlib.domain.Fine;
import smartlib.domain.Member;
import smartlib.domain.MembershipType;
import smartlib.domain.PremiumPolicy;
import smartlib.domain.Reservation;
import smartlib.domain.StandardPolicy;
import smartlib.domain.StudentPolicy;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainBusinessRulesTest {
    @Test
    void borrowLimits_areEnforcedByMembershipPolicy() {
        Member standardMember = new Member("M-1", "Standard Member", "standard@test.com", MembershipType.STANDARD);
        standardMember.setActiveLoansCount(3);

        Member premiumMember = new Member("M-2", "Premium Member", "premium@test.com", MembershipType.PREMIUM);
        premiumMember.setActiveLoansCount(10);

        Member studentMember = new Member("M-3", "Student Member", "student@test.com", MembershipType.STUDENT);
        studentMember.setActiveLoansCount(5);

        assertFalse(new StandardPolicy().canBorrow(standardMember));
        assertFalse(new PremiumPolicy().canBorrow(premiumMember));
        assertFalse(new StudentPolicy().canBorrow(studentMember));
    }

    @Test
    void loanDurations_matchPolicyContract() {
        BorrowingPolicy standardPolicy = new StandardPolicy();
        BorrowingPolicy premiumPolicy = new PremiumPolicy();
        BorrowingPolicy studentPolicy = new StudentPolicy();

        assertEquals(14, standardPolicy.loanDurationDays());
        assertEquals(28, premiumPolicy.loanDurationDays());
        assertEquals(21, studentPolicy.loanDurationDays());
    }

    @Test
    void fineRate_isFiftyCentsPerOverdueDay() {
        Fine fine = Fine.fromOverdueDays("L-1", 7);
        assertEquals(3.50, fine.amount(), 0.0001);
    }

    @Test
    void reservation_expiresAfterThreeDays() {
        Reservation reservation = new Reservation("R-1", "M-1", "B-1", LocalDate.of(2026, 5, 1));
        assertFalse(reservation.isExpired(LocalDate.of(2026, 5, 4)));
        assertTrue(reservation.isExpired(LocalDate.of(2026, 5, 5)));
    }

    @Test
    void outstandingFineAboveTen_blocksNewBorrowing() {
        Member member = new Member("M-4", "Blocked Member", "blocked@test.com", MembershipType.STANDARD);
        member.setActiveLoansCount(0);
        member.setOutstandingFine(10.01);

        assertFalse(new StandardPolicy().canBorrow(member));
    }
}
