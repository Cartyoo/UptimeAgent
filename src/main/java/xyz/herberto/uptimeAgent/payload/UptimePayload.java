package xyz.herberto.uptimeAgent.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UptimePayload {

    String agentVersion;
    String hostname;
    String os;
    String uptimeSeconds;
    String cpuUsagePercent;
    String memoryTotalBytes;
    String memoryUsedBytes;
    String diskUsedBytes;
    String diskTotalBytes;
    UptimeSpecs specs;

}
