package smartlib.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Member {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final LocalDate joinDate;
    private MembershipType membershipType;
    private int activeLoansCount;
    private double outstandingFine;

    public Member(
            String id,
            String name,
            String email,
            String phone,
            MembershipType membershipType,
            LocalDate joinDate
    ) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.email = requireText(email, "email");
        this.phone = requireText(phone, "phone");
        this.membershipType = Objects.requireNonNull(membershipType, "membershipType must not be null");
        this.joinDate = Objects.requireNonNull(joinDate, "joinDate must not be null");
    }

    public Member(String id, String name, String email, MembershipType membershipType) {
        this(id, name, email, "000-000-0000", membershipType, LocalDate.now());
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public LocalDate joinDate() {
        return joinDate;
    }

    public MembershipType membershipType() {
        return membershipType;
    }

    public int activeLoansCount() {
        return activeLoansCount;
    }

    public double outstandingFine() {
        return outstandingFine;
    }

    public BorrowingPolicy borrowingPolicy() {
        return switch (membershipType) {
            case STANDARD -> new StandardPolicy();
            case PREMIUM -> new PremiumPolicy();
            case STUDENT -> new StudentPolicy();
        };
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = Objects.requireNonNull(membershipType, "membershipType must not be null");
    }

    public void setActiveLoansCount(int activeLoansCount) {
        if (activeLoansCount < 0) {
            throw new IllegalArgumentException("activeLoansCount must be >= 0");
        }
        this.activeLoansCount = activeLoansCount;
    }

    public void setOutstandingFine(double outstandingFine) {
        if (outstandingFine < 0) {
            throw new IllegalArgumentException("outstandingFine must be >= 0");
        }
        this.outstandingFine = outstandingFine;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
