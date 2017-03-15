package de.axelspringer.ideas.team.mood;

import de.axelspringer.ideas.team.mood.mail.MailSender;
import de.axelspringer.ideas.team.mood.mail.MailTemplate;
import de.axelspringer.ideas.team.mood.moods.TeamMood;
import de.axelspringer.ideas.team.mood.moods.entity.TeamMoodResponse;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class TeamMoodApplication {

    private final static Logger LOG = LoggerFactory.getLogger(TeamMoodApplication.class);

//    private final static LocDatT

    public static void main(String[] args) throws Exception {
        checkParameters();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();

        JobDetail jobDetail = newJob(HelloJob.class).build();

        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    private static void checkParameters() {
        // TODO
    }

    public static class HelloJob implements Job {

        private final TeamMood teamMood;
        private final MailSender mailSender;
        private final TeamMoodProperties teamMoodProperties;

        public HelloJob() {
            this.teamMoodProperties = TeamMoodProperties.INSTANCE;
            this.mailSender = new MailSender(this.teamMoodProperties.getUsername(), this.teamMoodProperties.getPassword());
            this.teamMood = new TeamMood();
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            LOG.info("HelloJob executed");

            TeamMoodResponse teamMoodResponse = teamMood.loadTeamMoodForLastSevenDays("01770399-e272-4032-ad1e-c96e0dc90e58");
            LOG.info("Loaded data from TeamMood for team '{}'", teamMoodResponse.teamName);

            String innerValue = teamMoodResponse.teamName;
            String body = MailTemplate.fromClasspath("email.html",
                    param("asdf", innerValue),
                    param("title", "TeamMood "));
            String subject = "TeamMood for Ideas: KW" + getCalendarWeek();
            mailSender.send("sebastian.waschnick@asideas.de", subject, body);
        }

        private String getCalendarWeek() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            return "" + cal.get(Calendar.WEEK_OF_YEAR);
        }

        private MailTemplate.ViewParameter param(String key, String innerValue) {
            return new MailTemplate.ViewParameter(key, innerValue);
        }


    }
}