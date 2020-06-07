package server;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import server.plans.Plan;

import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class PlanManager {

    private final Scheduler scheduler;
    int id;
    private final ExecutorService parseExecutor;
    private final ThreadPoolExecutor downloadExecutor;
    private final BlockingQueue<XmlDownload> queue;

    public PlanManager(int threads) throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();

        parseExecutor = Executors.newSingleThreadExecutor();
        queue = new LinkedBlockingDeque<>();
        Consumer<XmlFile> consumer = (file) -> parseExecutor.execute(() -> System.out.println("Parsing " + file.getType()));

        // prepare threads
        downloadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            downloadExecutor.execute(new DownloaderThread<>(queue, consumer));
        }
    }

    public void addPlan(Plan plan) throws SchedulerException {
        plan.setDownloadQueue(queue);
        JobDetail job = newJob(PlanJob.class)
                .withIdentity("job" + id, "plans")
                .build();
        job.getJobDataMap().put("plan", plan);

        Trigger trigger = newTrigger()
                .withIdentity("trigger" + id++, "plans")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInHours(24)
                        //.withIntervalInSeconds(6)
                        .repeatForever())
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public void start() throws SchedulerException {
        scheduler.start();
    }

    public void shutdown() throws SchedulerException {
        downloadExecutor.shutdown();
        parseExecutor.shutdown();
        scheduler.shutdown();
    }

    public boolean isStarted() throws SchedulerException {
        return scheduler.isStarted();
    }

    public boolean isShutdown() throws SchedulerException {
        return scheduler.isShutdown();
    }
}
