package xyz.herberto.uptimeAgent.task;

import com.google.gson.Gson;
import com.sun.management.OperatingSystemMXBean;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.herberto.uptimeAgent.UptimeAgent;
import xyz.herberto.uptimeAgent.payload.UptimePayload;
import xyz.herberto.uptimeAgent.payload.UptimeSpecs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class UptimeTask extends BukkitRunnable {
    Gson gson = new Gson();

    @Override
    public void run() {

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();


            File serverRoot = new File(".");
            long totalSpace = serverRoot.getTotalSpace();
            long freeSpace = serverRoot.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            String os = System.getProperty("os.name");

            try (var reader = new BufferedReader(new FileReader("/etc/os-release"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("PRETTY_NAME")) {
                        os = line.split("=", 2)[1].replace("\"", "").trim();
                    }
                }
            } catch (IOException e) {
                os = "Linux";
            }

            String cpuModel = "Unknown Processor";

            try (var reader = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("model name")) {
                        cpuModel = line.split(":", 2)[1].trim();
                    }
                }
            }

            long ramTotalBytes = Runtime.getRuntime().totalMemory();
            long freeRamBytes = Runtime.getRuntime().freeMemory();
            long maxRamBytes = Runtime.getRuntime().maxMemory();
            double cpuUsagePercent = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getProcessCpuLoad();
            long uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(ManagementFactory.getRuntimeMXBean().getUptime());
            String hostname = InetAddress.getLocalHost().getHostName();
            String agentVersion = UptimeAgent.getAgentVersion();
            String cpuCores = String.valueOf(Runtime.getRuntime().availableProcessors());


            UptimePayload payload = new UptimePayload(
                    agentVersion,
                    hostname,
                    os,
                    String.valueOf(uptimeSeconds),
                    String.valueOf(cpuUsagePercent),
                    String.valueOf(maxRamBytes),
                    String.valueOf(ramTotalBytes - freeRamBytes),
                    String.valueOf(usedSpace),
                    String.valueOf(totalSpace),
                    new UptimeSpecs(
                            cpuModel,
                            cpuCores,
                            String.valueOf(ramTotalBytes)
                    )


            );
            String body = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://app.layeredy.com/api/servers/ingest"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + UptimeAgent.getUptimeToken())
                    .header("User-Agent", "layeredy-minecraft-server-agent/" + agentVersion)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                UptimeAgent.getInstance().getLogger().info("reported OK (200) | lyrdy.co/serveragen");
            } else if(statusCode == 403) {
                UptimeAgent.getInstance().getLogger().warning("ERROR 403: invalid token. Check 'layeredy-uptime-token' in your config.yml. Exiting. (lyrdy.co/server-agent-error)");
            } else if(statusCode == 429) {
                UptimeAgent.getInstance().getLogger().warning("rate limited (429) — reporting too frequently. Will retry next cycle. (lyrdy.co/server-agent-error)");
            } else {
                UptimeAgent.getInstance().getLogger().warning("unexpected response " + statusCode + ": " + response.body());
            }

        } catch (Exception e) {
            UptimeAgent.getInstance().getLogger().severe("report failed: " + e.getMessage() + " (lyrdy.co/server-agent-error)");
        }

    }

}
