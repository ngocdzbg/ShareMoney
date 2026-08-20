package com.ngocnb.sharemoney.helper;

import com.ngocnb.sharemoney.entity.Expense;
import com.ngocnb.sharemoney.entity.ExpenseParticipant;
import com.ngocnb.sharemoney.entity.GroupMember;
import com.ngocnb.sharemoney.entity.GroupName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ExpenseHelper {

    private ExpenseHelper() {
        // Utility class
    }

    /**
     * Tạo Expense và tự động chia đều số tiền cho danh sách người tham gia.
     *
     * Phần dư do làm tròn sẽ được cộng vào người đầu tiên,
     * đảm bảo tổng shareAmount luôn bằng totalAmount.
     */
    public static Expense createExpenseWithEqualSplit(
            GroupName group,
            String description,
            BigDecimal totalAmount,
            LocalDate expenseDate,
            GroupMember paidBy,
            List<GroupMember> participants
    ) {
        Expense expense = Expense.builder()
                .group(group)
                .description(description)
                .amount(totalAmount)
                .expenseDate(expenseDate)
                .paidBy(paidBy)
                .participants(new ArrayList<>())
                .build();

        int count = participants.size();

        if (count == 0) {
            throw new IllegalArgumentException(
                    "Participants must not be empty"
            );
        }

        BigDecimal countBD = BigDecimal.valueOf(count);

        // 1. Chia đều tiền, làm tròn xuống 2 chữ số thập phân
        BigDecimal baseShare = totalAmount.divide(
                countBD,
                2,
                RoundingMode.DOWN
        );

        // 2. Tính phần tiền còn dư
        BigDecimal totalAssigned = baseShare.multiply(countBD);

        BigDecimal remainder = totalAmount.subtract(totalAssigned);

        // 3. Tạo ExpenseParticipant
        // Phần dư được cộng vào thành viên đầu tiên
        for (int i = 0; i < count; i++) {
            GroupMember member = participants.get(i);

            BigDecimal shareAmount = (i == 0)
                    ? baseShare.add(remainder)
                    : baseShare;

            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expense(expense)
                    .groupMember(member)
                    .shareAmount(shareAmount)
                    .build();

            expense.getParticipants().add(participant);
        }

        return expense;
    }
}