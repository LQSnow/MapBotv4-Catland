package me.maplef.mapbotv4.managers;

import me.maplef.mapbotv4.loops.*;
import me.maplef.mapbotv4.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.Objects;

public class LoopJobManager {
    ConfigManager configManager = new ConfigManager();
    FileConfiguration config = configManager.getConfig();

    public void register(){
        try{
            CronExpression morning_cron = new CronExpression(Objects.requireNonNull(config.getString("daily-greetings.morning.cron")));
            CronExpression night_cron = new CronExpression(Objects.requireNonNull(config.getString("daily-greetings.night.cron")));
            CronExpression tpsCheck_cron = new CronExpression(String.format("0 0/%d * * * ?", config.getInt("tps-check.interval")));

            if(config.getBoolean("tps-check.enable"))
                Scheduler.registerJob("tpsCheck", String.valueOf(tpsCheck_cron), TPSCheck.class);
            if(config.getBoolean("daily-greetings.morning.enable"))
                Scheduler.registerJob("Morning", String.valueOf(morning_cron), GoodMorning.class);
            if(config.getBoolean("daily-greetings.night.enable"))
                Scheduler.registerJob("Night", String.valueOf(night_cron), GoodNight.class);
            if(config.getBoolean("cat-images.upload-image.cat-detect.enable"))
                Scheduler.registerJob("UpdateBaiduAccessToken", "0 0 0 /10 * ? *", BaiduAccessTokenUpdate.class);

            Scheduler.scheduler.start();
        } catch (SchedulerException exception){
            Bukkit.getLogger().warning(exception.getClass() + ": " + exception.getMessage());
            try{
                Scheduler.scheduler.shutdown();
            } catch (SchedulerException exception1){
                Bukkit.getLogger().warning(exception1.getClass() + ": " + exception1.getMessage());
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
    }
}
