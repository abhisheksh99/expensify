package com.abhishek.expensify.service;

import com.abhishek.expensify.dto.ExpenseDto;
import com.abhishek.expensify.entity.ProfileEntity;
import com.abhishek.expensify.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${expensify.frontend.url}")
    private String frontendUrl;

    /**
     * ✅ Daily reminder to add income and expenses
     * Runs at 10 PM EST
     */
    @Scheduled(cron = "0 0 10 * * *", zone = "EST")
    public void sendDailyIncomeExpenseReminder() {
        log.info("Job started: sendDailyIncomeExpenseReminder()");

        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            String subject = "Daily reminder: Add your income and expenses";
            String body = "Hi " + profile.getFullName() + ",<br><br>"
                    + "This is a friendly reminder to log your income and expenses for today in Expensify.<br><br>"
                    + buildActionButton("Go to Expensify", frontendUrl)
                    + "<br><br>Best regards,<br><b>Expensify Team</b>";

            emailService.sendEmail(profile.getEmail(), subject, body);
            log.info("Reminder sent to user: {}", profile.getEmail());
        }

        log.info("Job completed: sendDailyIncomeExpenseReminder()");
    }

    /**
     * ✅ Daily summary of expenses
     * Runs at 11 PM IST
     */
    @Scheduled(cron = "0 0 11 * * *", zone = "EST")
    public void sendDailyExpenseSummary() {
        log.info("Job started: sendDailyExpenseSummary()");

        List<ProfileEntity> profiles = profileRepository.findAll();
        for (ProfileEntity profile : profiles) {
            List<ExpenseDto> todaysExpenses =
                    expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());

            if (!todaysExpenses.isEmpty()) {
                String subject = "Your Daily Expense Summary";
                String table = buildExpenseTable(todaysExpenses);
                String body = "Hi " + profile.getFullName() + ",<br/><br/>"
                        + "Here is a summary of your expenses for today:<br/><br/>"
                        + table
                        + "<br/><br/>Best regards,<br/><b>Expensify Team</b>";

                emailService.sendEmail(profile.getEmail(), subject, body);
                log.info("Summary sent to user: {}", profile.getEmail());
            }
        }

        log.info("Job completed: sendDailyExpenseSummary()");
    }

    /**
     * ✅ Helper: Builds styled button
     */
    private String buildActionButton(String text, String url) {
        return "<a href='" + url + "' "
                + "style='display:inline-block;padding:10px 20px;"
                + "background-color:#4CAF50;color:#fff;text-decoration:none;"
                + "border-radius:5px;font-weight:bold;'>"
                + text + "</a>";
    }

    /**
     * ✅ Helper: Builds expense table
     */
    private String buildExpenseTable(List<ExpenseDto> expenses) {
        StringBuilder table = new StringBuilder();
        table.append("<table style='border-collapse:collapse;width:100%;'>");
        table.append("<tr style='background-color:#f2f2f2;'>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>S.No</th>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>Name</th>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>Amount</th>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>Category</th>")
                .append("</tr>");

        int i = 1;
        for (ExpenseDto expense : expenses) {
            table.append("<tr>")
                    .append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>")
                    .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>")
                    .append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>")
                    .append("<td style='border:1px solid #ddd;padding:8px;'>")
                    .append(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A")
                    .append("</td>")
                    .append("</tr>");
        }
        table.append("</table>");
        return table.toString();
    }
}
