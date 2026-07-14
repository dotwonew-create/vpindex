package me.vihnya.vpindex.logger;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger {
    private FileWriter writer;

    private final Plugin plugin;

    public FileLogger(Plugin plugin) {
        this.plugin = plugin;

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String fileName = "log_" + dateFormat.format(new Date()) + ".txt";
        File logFile = new File(logsFolder, fileName);

        try {
            logFile.createNewFile();
            writer = new FileWriter(logFile, true);
            plugin.getLogger().info("Логгер активирован. Логи будут записываться в файл: " + logFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Не удалось создать файл логов: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void log(String message, String admin) {
        try {
            String logMessage = String.format("[%s] [%s] %n",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
                    message + " - " + admin);
            writer.write(logMessage);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Ошибка при записи лога: " + e.getMessage());
        }
    }
}
