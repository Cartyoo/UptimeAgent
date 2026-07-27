package xyz.herberto.uptimeAgent.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UptimeSpecs {

    String cpuModel;
    String cpuCores;
    String ramTotalBytes;

}
