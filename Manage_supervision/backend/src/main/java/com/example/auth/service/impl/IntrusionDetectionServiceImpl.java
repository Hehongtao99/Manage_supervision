package com.example.auth.service.impl;

import com.example.auth.dto.IntrusionDetectionDTO;
import com.example.auth.entity.MonitorData;
import com.example.auth.repository.MonitorDataRepository;
import com.example.auth.service.IntrusionDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

/**
 * 入侵检测服务实现类 - 使用OSHI进行真实系统扫描
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntrusionDetectionServiceImpl implements IntrusionDetectionService {

    private final MonitorDataRepository monitorDataRepository;
    
    // 存储用户的检测历史
    private static final Map<Long, List<Map<String, Object>>> userDetectionHistory = new HashMap<>();
    
    // 系统信息实例
    private final SystemInfo systemInfo = new SystemInfo();
    
    @Override
    public List<MonitorData> detectIntrusion(IntrusionDetectionDTO detectionDTO) {
        // 真实系统扫描检测异常
        List<MonitorData> anomalies = new ArrayList<>();
        
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        try {
            HardwareAbstractionLayer hardware = systemInfo.getHardware();
            
            // 根据检测类型执行真实系统检测
            switch (detectionDTO.getType()) {
                case "full":
                    // 全面检测所有类型
                    // 1. 系统资源检测
                    detectSystemResources(detectionDTO.getUserId(), now, anomalies);
                    
                    // 2. 异常进程检测
                    detectSuspiciousProcesses(detectionDTO.getUserId(), now, anomalies);
                    
                    // 3. 网络连接检测
                    detectNetworkConnections(detectionDTO.getUserId(), now, anomalies);
                    
                    // 4. 文件系统检测
                    detectFileSystemChanges(detectionDTO.getUserId(), now, anomalies);
                    
                    // 5. 系统日志检测
                    detectSystemLogs(detectionDTO.getUserId(), now, anomalies);
                    
                    // 6. 端口扫描检测
                    detectPortScanning(detectionDTO.getUserId(), now, anomalies);
                    
                    // 7. 文件完整性检测
                    detectFileIntegrity(detectionDTO.getUserId(), now, anomalies);
                    
                    // 8. 用户活动检测
                    detectUserActivity(detectionDTO.getUserId(), now, anomalies);
                    
                    // 9. 系统服务检测
                    detectSystemServices(detectionDTO.getUserId(), now, anomalies);
                    
                    // 10. 注册表检测 (仅Windows系统)
                    detectRegistryChanges(detectionDTO.getUserId(), now, anomalies);
                    
                    // 11. 恶意软件检测
                    detectMalware(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "cpu":
                    // CPU使用情况检测
                    MonitorData cpuData = detectCpuUsage(detectionDTO.getUserId(), null, now);
                    if (cpuData != null) {
                        anomalies.add(cpuData);
                    }
                    break;
                    
                case "memory":
                    // 内存使用情况检测
                    MonitorData memoryData = detectMemoryUsage(detectionDTO.getUserId(), null, now);
                    if (memoryData != null) {
                        anomalies.add(memoryData);
                    }
                    break;
                    
                case "network":
                    // 网络使用情况检测
                    MonitorData networkData = detectNetworkUsage(detectionDTO.getUserId(), null, now);
                    if (networkData != null) {
                        anomalies.add(networkData);
                    }
                    
                    // 网络连接检测
                    detectNetworkConnections(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "disk":
                    // 磁盘使用情况检测
                    MonitorData diskData = detectDiskUsage(detectionDTO.getUserId(), null, now);
                    if (diskData != null) {
                        anomalies.add(diskData);
                    }
                    
                    // 文件系统检测
                    detectFileSystemChanges(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "process":
                    // 异常进程检测
                    detectSuspiciousProcesses(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "logs":
                    // 系统日志检测
                    detectSystemLogs(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "ports":
                    // 端口扫描检测
                    detectPortScanning(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "files":
                    // 文件完整性检测
                    detectFileIntegrity(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "users":
                    // 用户活动检测
                    detectUserActivity(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "services":
                    // 系统服务检测
                    detectSystemServices(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "registry":
                    // 注册表检测 (仅Windows系统)
                    detectRegistryChanges(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                case "malware":
                    // 恶意软件检测
                    detectMalware(detectionDTO.getUserId(), now, anomalies);
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("系统扫描过程中发生错误", e);
        }
        
        if (!anomalies.isEmpty()) {
            // 获取数据库中已存在的异常数据，用于去重
            List<MonitorData> existingAnomalies = monitorDataRepository.findByUserIdAndIsAnomalyTrue(
                detectionDTO.getUserId(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "recordTime")
            );
            
            // 过滤掉重复的异常数据
            List<MonitorData> newAnomalies = new ArrayList<>();
            for (MonitorData anomaly : anomalies) {
                // 检查是否是重复的异常
                boolean isDuplicate = false;
                for (MonitorData existing : existingAnomalies) {
                    // 比较关键字段：数据类型、值、描述等
                    if (existing.getDataType().equals(anomaly.getDataType()) 
                        && Math.abs(existing.getValue() - anomaly.getValue()) < 0.01 // 值几乎相同
                        && (existing.getDescription() != null && anomaly.getDescription() != null 
                            && existing.getDescription().equals(anomaly.getDescription()))) {
                        isDuplicate = true;
                        break;
                    }
                }
                
                // 如果不是重复的，添加到新异常列表
                if (!isDuplicate) {
                    newAnomalies.add(anomaly);
                }
            }
            
            // 保存非重复的新异常
            if (!newAnomalies.isEmpty()) {
                monitorDataRepository.saveAll(newAnomalies);
            }
            
            // 记录此次检测历史（仍然记录所有检测到的异常，包括重复的）
            Map<String, Object> detectionRecord = new HashMap<>();
            detectionRecord.put("detectionTime", now);
            detectionRecord.put("detectionType", detectionDTO.getType());
            detectionRecord.put("threshold", detectionDTO.getThreshold());
            detectionRecord.put("anomalyCount", anomalies.size());
            detectionRecord.put("newAnomalyCount", newAnomalies.size());
            
            // 保存检测历史
            if (!userDetectionHistory.containsKey(detectionDTO.getUserId())) {
                userDetectionHistory.put(detectionDTO.getUserId(), new ArrayList<>());
            }
            userDetectionHistory.get(detectionDTO.getUserId()).add(detectionRecord);
        }
        
        return anomalies;
    }
    
    @Override
    public List<Map<String, Object>> getRecentDetections(Long userId, Integer limit) {
        // 从数据库中获取用户的所有异常监控数据
        List<MonitorData> recentAnomalies;
        
        if (limit != null && limit > 0) {
            // 如果指定了 limit，则使用分页查询
            recentAnomalies = monitorDataRepository.findByUserIdAndIsAnomalyTrue(userId, 
                org.springframework.data.domain.PageRequest.of(0, limit, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "recordTime"))
            ).getContent();
        } else {
            // 如果没有指定 limit 或 limit <= 0，则获取所有记录
            recentAnomalies = monitorDataRepository.findByUserIdAndIsAnomalyTrue(userId, 
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "recordTime")
            );
        }
        
        // 将数据转换为Map格式返回
        return recentAnomalies.stream()
            .map(data -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", data.getId());
                map.put("dataType", data.getDataType());
                map.put("value", data.getValue());
                map.put("expectedMin", data.getExpectedMin());
                map.put("expectedMax", data.getExpectedMax());
                map.put("deviation", data.getDeviation());
                map.put("isAnomaly", data.getIsAnomaly());
                map.put("resolved", data.getResolved());
                map.put("recordTime", data.getRecordTime());
                map.put("description", data.getDescription());
                map.put("source", data.getSource());
                return map;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getDetectionStats(Long userId) {
        List<Map<String, Object>> history = userDetectionHistory.getOrDefault(userId, new ArrayList<>());
        
        // 计算总检测次数
        int totalDetections = history.size();
        
        // 计算检测到的总异常数
        int totalAnomalies = history.stream()
                .mapToInt(record -> (Integer) record.get("anomalyCount"))
                .sum();
        
        // 计算各类型检测次数
        Map<String, Integer> detectionsByType = new HashMap<>();
        history.forEach(record -> {
            String type = (String) record.get("detectionType");
            detectionsByType.put(type, detectionsByType.getOrDefault(type, 0) + 1);
        });
        
        // 最近一次检测时间
        LocalDateTime lastDetectionTime = history.isEmpty() ? null :
                history.stream()
                        .map(record -> (LocalDateTime) record.get("detectionTime"))
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDetections", totalDetections);
        stats.put("totalAnomalies", totalAnomalies);
        stats.put("detectionsByType", detectionsByType);
        stats.put("lastDetectionTime", lastDetectionTime);
        
        return stats;
    }
    
    /**
     * 检测系统资源使用情况
     */
    private void detectSystemResources(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        // CPU检测
        MonitorData cpuData = detectCpuUsage(userId, null, time);
        if (cpuData != null) {
            anomalies.add(cpuData);
        }
        
        // 内存检测
        MonitorData memoryData = detectMemoryUsage(userId, null, time);
        if (memoryData != null) {
            anomalies.add(memoryData);
        }
        
        // 网络检测
        MonitorData networkData = detectNetworkUsage(userId, null, time);
        if (networkData != null) {
            anomalies.add(networkData);
        }
        
        // 磁盘检测
        MonitorData diskData = detectDiskUsage(userId, null, time);
        if (diskData != null) {
            anomalies.add(diskData);
        }
    }
    
    /**
     * 检测CPU使用率
     */
    private MonitorData detectCpuUsage(Long userId, Double threshold, LocalDateTime time) {
        try {
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            
            // 获取CPU使用率需要两个时间点的数据
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            // 等待一段时间再次获取数据以计算使用率
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 获取第二次数据
            long[] ticks = processor.getSystemCpuLoadTicks();
            // 计算CPU使用率
            double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100; // 转为百分比
            
            // 检测异常的智能条件
            boolean isAnomaly = false;
            StringBuilder description = new StringBuilder();
            
            // 1. 如果CPU使用率持续超过85%，可能存在计算密集型恶意软件
            if (cpuUsage > 85) {
                isAnomaly = true;
                description.append("CPU使用率异常(").append(String.format("%.2f", cpuUsage))
                          .append("%)，持续高负载可能存在挖矿恶意程序");
            } 
            // 2. CPU使用率在70%-85%之间，需要关注
            else if (cpuUsage > 70) {
                isAnomaly = true;
                description.append("CPU使用率偏高(").append(String.format("%.2f", cpuUsage))
                          .append("%)，可能存在资源密集型进程");
            }
            // 3. 如果系统空闲但CPU使用率仍超过50%，可能有后台进程
            else if (cpuUsage > 50) {
                // 检查系统空闲状态 - 判断标准可以根据实际情况调整
                boolean isIdle = systemInfo.getOperatingSystem().getProcesses(null, 
                        oshi.software.os.OperatingSystem.ProcessSorting.CPU_DESC, 5).stream()
                        .filter(p -> p.getName().toLowerCase().contains("idle"))
                        .anyMatch(p -> p.getProcessCpuLoadCumulative() > 0.5);
                        
                if (isIdle) {
                    isAnomaly = true;
                    description.append("系统空闲但CPU使用率异常(").append(String.format("%.2f", cpuUsage))
                              .append("%)，可能存在隐藏的恶意进程");
                } else {
                    description.append("CPU使用率正常(").append(String.format("%.2f", cpuUsage)).append("%)");
                }
            } else {
                description.append("CPU使用率正常(").append(String.format("%.2f", cpuUsage)).append("%)");
            }
            
            double expectedMin = 0.0;
            double expectedMax = 70.0; // 基准参考值
            double midValue = (expectedMin + expectedMax) / 2;
            double deviation = Math.abs(cpuUsage - midValue) / midValue * 100;
            
            MonitorData data = new MonitorData();
            data.setUserId(userId);
            data.setDataType("CPU使用率");
            data.setValue(cpuUsage);
            data.setExpectedMin(expectedMin);
            data.setExpectedMax(expectedMax);
            data.setDeviation(deviation);
            data.setIsAnomaly(isAnomaly);
            data.setResolved(false);
            data.setRecordTime(time);
            data.setSource("入侵检测");
            data.setDescription(description.toString());
            
            return data;
        } catch (Exception e) {
            log.error("检测CPU使用率过程中发生错误", e);
            return null;
        }
    }
    
    /**
     * 检测内存使用率
     */
    private MonitorData detectMemoryUsage(Long userId, Double threshold, LocalDateTime time) {
        try {
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            
            double totalMemory = memory.getTotal();
            double availableMemory = memory.getAvailable();
            double usedMemory = totalMemory - availableMemory;
            
            // 计算内存使用率
            double memoryUsage = (usedMemory / totalMemory) * 100;
            
            // 智能判断内存使用是否异常
            boolean isAnomaly = false;
            StringBuilder description = new StringBuilder();
            
            // 1. 内存使用率超过90%，系统可能面临内存耗尽风险
            if (memoryUsage > 90) {
                isAnomaly = true;
                description.append("内存使用率严重异常(").append(String.format("%.2f", memoryUsage))
                          .append("%)，系统面临内存耗尽风险，可能存在内存泄漏或恶意软件");
            }
            // 2. 内存使用率超过85%，较高风险
            else if (memoryUsage > 85) {
                isAnomaly = true;
                description.append("内存使用率异常(").append(String.format("%.2f", memoryUsage))
                          .append("%)，可能存在内存泄漏或资源滥用");
            }
            // 3. 内存使用率超过75%，需要关注
            else if (memoryUsage > 75) {
                isAnomaly = true;
                description.append("内存使用率偏高(").append(String.format("%.2f", memoryUsage))
                          .append("%)，系统资源紧张");
            }
            else {
                description.append("内存使用率正常(").append(String.format("%.2f", memoryUsage)).append("%)");
            }
            
            double expectedMin = 0.0;
            double expectedMax = 75.0; // 基准参考值
            double midValue = (expectedMin + expectedMax) / 2;
            double deviation = Math.abs(memoryUsage - midValue) / midValue * 100;
            
            MonitorData data = new MonitorData();
            data.setUserId(userId);
            data.setDataType("内存使用率");
            data.setValue(memoryUsage);
            data.setExpectedMin(expectedMin);
            data.setExpectedMax(expectedMax);
            data.setDeviation(deviation);
            data.setIsAnomaly(isAnomaly);
            data.setResolved(false);
            data.setRecordTime(time);
            data.setSource("入侵检测");
            data.setDescription(description.toString());
            
            return data;
        } catch (Exception e) {
            log.error("检测内存使用率过程中发生错误", e);
            return null;
        }
    }
    
    /**
     * 检测网络使用率
     */
    private MonitorData detectNetworkUsage(Long userId, Double threshold, LocalDateTime time) {
        try {
            List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
            
            // 先获取一次数据
            Map<String, Long> prevBytesMap = new HashMap<>();
            for (NetworkIF networkIF : networkIFs) {
                networkIF.updateAttributes();
                prevBytesMap.put(networkIF.getName(), networkIF.getBytesRecv() + networkIF.getBytesSent());
            }
            
            // 等待一段时间
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 获取第二次数据
            double totalDataRate = 0.0;
            Map<String, Double> interfaceRates = new HashMap<>();
            for (NetworkIF networkIF : networkIFs) {
                networkIF.updateAttributes();
                long prevBytes = prevBytesMap.getOrDefault(networkIF.getName(), 0L);
                long currentBytes = networkIF.getBytesRecv() + networkIF.getBytesSent();
                
                // 计算每秒传输的MB数
                double dataRateMBps = (currentBytes - prevBytes) / (1024.0 * 1024.0);
                totalDataRate += dataRateMBps;
                
                // 记录每个接口的速率
                interfaceRates.put(networkIF.getName(), dataRateMBps);
            }
            
            // 智能判断网络流量是否异常
            boolean isAnomaly = false;
            StringBuilder description = new StringBuilder();
            
            // 网络异常检测更复杂，需要考虑多种情况
            // 1. 总流量是否异常高
            if (totalDataRate > 50) { // 超过50MB/s的总流量
                isAnomaly = true;
                description.append("网络总流量异常(").append(String.format("%.2f", totalDataRate))
                          .append("MB/s)，可能存在数据泄露或DDoS攻击");
            }
            // 2. 单个接口流量是否异常高
            else {
                for (Map.Entry<String, Double> entry : interfaceRates.entrySet()) {
                    if (entry.getValue() > 20) { // 单个接口超过20MB/s
                        isAnomaly = true;
                        description.append("网络接口 ").append(entry.getKey())
                                  .append(" 流量异常(").append(String.format("%.2f", entry.getValue()))
                                  .append("MB/s)，可能存在数据泄露");
                        break;
                    }
                }
                
                if (!isAnomaly) {
                    description.append("网络流量正常(").append(String.format("%.2f", totalDataRate)).append("MB/s)");
                }
            }
            
            double expectedMin = 0.0;
            double expectedMax = 20.0; // 基准参考值
            double midValue = (expectedMin + expectedMax) / 2;
            double deviation = Math.abs(totalDataRate - midValue) / midValue * 100;
            
            MonitorData data = new MonitorData();
            data.setUserId(userId);
            data.setDataType("网络流量(MB/s)");
            data.setValue(totalDataRate);
            data.setExpectedMin(expectedMin);
            data.setExpectedMax(expectedMax);
            data.setDeviation(deviation);
            data.setIsAnomaly(isAnomaly);
            data.setResolved(false);
            data.setRecordTime(time);
            data.setSource("入侵检测");
            data.setDescription(description.toString());
            
            return data;
        } catch (Exception e) {
            log.error("检测网络使用率过程中发生错误", e);
            return null;
        }
    }
    
    /**
     * 检测磁盘使用率
     */
    private MonitorData detectDiskUsage(Long userId, Double threshold, LocalDateTime time) {
        try {
            FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();
            
            // 计算所有磁盘的平均使用率
            double totalUsagePercent = 0.0;
            int diskCount = 0;
            StringBuilder diskDetails = new StringBuilder();
            boolean hasCriticalDisk = false;
            
            for (OSFileStore fs : fileStores) {
                long totalSpace = fs.getTotalSpace();
                long usableSpace = fs.getUsableSpace();
                
                if (totalSpace > 0) {
                    double usagePercent = ((double) (totalSpace - usableSpace) / totalSpace) * 100;
                    totalUsagePercent += usagePercent;
                    diskCount++;
                    
                    // 记录每个磁盘的使用情况
                    diskDetails.append(fs.getMount())
                              .append(": ")
                              .append(String.format("%.2f", usagePercent))
                              .append("%, ");
                    
                    // 检查是否有严重问题的磁盘
                    if (usagePercent > 90) {
                        hasCriticalDisk = true;
                    }
                }
            }
            
            // 如果有磁盘，计算平均使用率
            double avgDiskUsage = 0.0;
            if (diskCount > 0) {
                avgDiskUsage = totalUsagePercent / diskCount;
            }
            
            // 智能判断磁盘使用是否异常
            boolean isAnomaly = false;
            StringBuilder description = new StringBuilder();
            
            // 1. 有严重风险的磁盘
            if (hasCriticalDisk) {
                isAnomaly = true;
                description.append("磁盘使用率严重异常，存在使用率>90%的磁盘，平均使用率(")
                          .append(String.format("%.2f", avgDiskUsage))
                          .append("%)，磁盘详情: ")
                          .append(diskDetails);
            }
            // 2. 平均使用率高于80%
            else if (avgDiskUsage > 80) {
                isAnomaly = true;
                description.append("磁盘平均使用率异常(")
                          .append(String.format("%.2f", avgDiskUsage))
                          .append("%)，可能存在磁盘空间耗尽风险，磁盘详情: ")
                          .append(diskDetails);
            }
            // 3. 平均使用率高于70%
            else if (avgDiskUsage > 70) {
                isAnomaly = true;
                description.append("磁盘平均使用率偏高(")
                          .append(String.format("%.2f", avgDiskUsage))
                          .append("%)，磁盘详情: ")
                          .append(diskDetails);
            }
            else {
                description.append("磁盘使用率正常(")
                          .append(String.format("%.2f", avgDiskUsage))
                          .append("%)，磁盘详情: ")
                          .append(diskDetails);
            }
            
            double expectedMin = 0.0;
            double expectedMax = 70.0; // 基准参考值
            double midValue = (expectedMin + expectedMax) / 2;
            double deviation = Math.abs(avgDiskUsage - midValue) / midValue * 100;
            
            MonitorData data = new MonitorData();
            data.setUserId(userId);
            data.setDataType("磁盘使用率");
            data.setValue(avgDiskUsage);
            data.setExpectedMin(expectedMin);
            data.setExpectedMax(expectedMax);
            data.setDeviation(deviation);
            data.setIsAnomaly(isAnomaly);
            data.setResolved(false);
            data.setRecordTime(time);
            data.setSource("入侵检测");
            data.setDescription(description.toString());
            
            return data;
        } catch (Exception e) {
            log.error("检测磁盘使用率过程中发生错误", e);
            return null;
        }
    }
    
    /**
     * 检测异常进程
     */
    private void detectSuspiciousProcesses(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            List<oshi.software.os.OSProcess> processes = systemInfo.getOperatingSystem().getProcesses(null, oshi.software.os.OperatingSystem.ProcessSorting.CPU_DESC, 10);
            
            for (oshi.software.os.OSProcess process : processes) {
                // 检查CPU使用率异常的进程
                if (process.getProcessCpuLoadCumulative() > 0.7) {  // 70%以上CPU使用率视为异常
                    MonitorData data = new MonitorData();
                    data.setUserId(userId);
                    data.setDataType("异常进程");
                    data.setValue(process.getProcessCpuLoadCumulative() * 100);
                    data.setExpectedMin(0.0);
                    data.setExpectedMax(70.0);
                    data.setDeviation(((process.getProcessCpuLoadCumulative() * 100) - 35) / 35 * 100);
                    data.setIsAnomaly(true);
                    data.setResolved(false);
                    data.setRecordTime(time);
                    data.setSource("入侵检测");
                    data.setDescription("发现高CPU使用率进程: " + process.getName() + " (PID: " + process.getProcessID() + 
                            ", CPU: " + String.format("%.2f", process.getProcessCpuLoadCumulative() * 100) + 
                            "%, 内存: " + FormatUtil.formatBytes(process.getResidentSetSize()) + ")");
                    anomalies.add(data);
                }
                
                // 检查内存使用异常的进程
                if (process.getResidentSetSize() > 1024L * 1024 * 1024) {  // 1GB以上内存使用视为需要关注
                    MonitorData data = new MonitorData();
                    data.setUserId(userId);
                    data.setDataType("异常进程");
                    data.setValue(process.getResidentSetSize() / (1024.0 * 1024));
                    data.setExpectedMin(0.0);
                    data.setExpectedMax(1024.0);
                    data.setDeviation((process.getResidentSetSize() / (1024.0 * 1024) - 512) / 512 * 100);
                    data.setIsAnomaly(true);
                    data.setResolved(false);
                    data.setRecordTime(time);
                    data.setSource("入侵检测");
                    data.setDescription("发现高内存使用进程: " + process.getName() + " (PID: " + process.getProcessID() + 
                            ", CPU: " + String.format("%.2f", process.getProcessCpuLoadCumulative() * 100) + 
                            "%, 内存: " + FormatUtil.formatBytes(process.getResidentSetSize()) + ")");
                    anomalies.add(data);
                }
                
                // 检查可疑命令行参数
                String[] suspiciousCommands = {"mining", "miner", "cryptonight", "xmrig", "malware", "botnet", "ransomware"};
                String commandLine = process.getCommandLine() != null ? process.getCommandLine().toLowerCase() : "";
                
                for (String suspiciousCmd : suspiciousCommands) {
                    if (commandLine.contains(suspiciousCmd)) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("可疑进程");
                        data.setValue(1.0);  // 表示存在可疑进程
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("发现可疑进程名称或命令行: " + process.getName() + " (PID: " + process.getProcessID() + 
                                ", 命令行: " + (commandLine.length() > 100 ? commandLine.substring(0, 100) + "..." : commandLine) + ")");
                        anomalies.add(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测异常进程过程中发生错误", e);
        }
    }
    
    /**
     * 检测可疑网络连接
     */
    private void detectNetworkConnections(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            List<oshi.software.os.OSProcess> processes = systemInfo.getOperatingSystem().getProcesses(null, oshi.software.os.OperatingSystem.ProcessSorting.CPU_DESC, 20);
            List<oshi.software.os.InternetProtocolStats.IPConnection> connections = systemInfo.getOperatingSystem().getInternetProtocolStats().getConnections();
            
            // 检查网络连接数量是否异常
            if (connections.size() > 100) {  // 如果连接数超过100，可能有问题
                MonitorData data = new MonitorData();
                data.setUserId(userId);
                data.setDataType("网络连接");
                data.setValue((double) connections.size());
                data.setExpectedMin(0.0);
                data.setExpectedMax(100.0);
                data.setDeviation((connections.size() - 50) / 50.0 * 100);
                data.setIsAnomaly(true);
                data.setResolved(false);
                data.setRecordTime(time);
                data.setSource("入侵检测");
                data.setDescription("检测到异常网络连接数量: " + connections.size() + " 个活跃连接，可能存在网络扫描或DoS攻击");
                anomalies.add(data);
            }
            
            // 检查可疑端口连接
            int[] suspiciousPorts = {1080, 3128, 8080, 1723, 4444, 5554, 9001, 9030}; // 代理、后门、Tor等常用端口
            for (oshi.software.os.InternetProtocolStats.IPConnection conn : connections) {
                for (int port : suspiciousPorts) {
                    if (conn.getLocalPort() == port || conn.getForeignPort() == port) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("可疑网络连接");
                        data.setValue((double) port);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到可疑端口连接: 本地 " + conn.getLocalAddress() + ":" + conn.getLocalPort() + 
                                " -> 远程 " + conn.getForeignAddress() + ":" + conn.getForeignPort() + 
                                ", 状态: " + conn.getState());
                        anomalies.add(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测网络连接过程中发生错误", e);
        }
    }
    
    /**
     * 检测文件系统变化
     */
    private void detectFileSystemChanges(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 检查敏感系统目录的文件变化
            String[] sensitiveDirectories = {
                System.getProperty("user.home"),
                System.getProperty("java.io.tmpdir"),
                "C:\\Windows\\System32", // Windows
                "/etc", "/tmp", "/var/tmp", "/var/www" // Linux
            };
            
            for (String dir : sensitiveDirectories) {
                File directory = new File(dir);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    
                    if (files != null) {
                        // 检查最近修改的文件
                        long currentTime = System.currentTimeMillis();
                        long recentTimeThreshold = currentTime - (30 * 60 * 1000); // 30分钟内
                        
                        for (File file : files) {
                            if (file.lastModified() > recentTimeThreshold) {
                                // 检查可执行文件或脚本文件
                                String fileName = file.getName().toLowerCase();
                                if (fileName.endsWith(".exe") || fileName.endsWith(".dll") || 
                                    fileName.endsWith(".sh") || fileName.endsWith(".bat") ||
                                    fileName.endsWith(".ps1") || fileName.endsWith(".vbs")) {
                                    
                                    MonitorData data = new MonitorData();
                                    data.setUserId(userId);
                                    data.setDataType("文件系统变化");
                                    data.setValue(1.0);
                                    data.setExpectedMin(0.0);
                                    data.setExpectedMax(0.0);
                                    data.setDeviation(100.0);
                                    data.setIsAnomaly(true);
                                    data.setResolved(false);
                                    data.setRecordTime(time);
                                    data.setSource("入侵检测");
                                    data.setDescription("敏感目录中发现最近修改的可执行文件: " + file.getAbsolutePath() + 
                                            ", 最后修改时间: " + new Date(file.lastModified()));
                                    anomalies.add(data);
                                }
                            }
                        }
                    }
                }
            }
            
            // 检查磁盘空间突然变化
            FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();
            
            for (OSFileStore fs : fileStores) {
                // 检查磁盘空间是否在短时间内有大幅度变化（这需要与之前的数据做对比）
                // 由于没有历史数据，这里仅检查使用率是否超过阈值
                long totalSpace = fs.getTotalSpace();
                long usableSpace = fs.getUsableSpace();
                
                if (totalSpace > 0) {
                    double usagePercent = ((double) (totalSpace - usableSpace) / totalSpace) * 100;
                    
                    if (usagePercent > 95) { // 如果使用率超过95%，可能存在异常
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("磁盘使用异常");
                        data.setValue(usagePercent);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(95.0);
                        data.setDeviation((usagePercent - 47.5) / 47.5 * 100);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("磁盘 " + fs.getMount() + " 使用率异常: " + 
                                String.format("%.2f", usagePercent) + "%, 剩余空间: " + 
                                FormatUtil.formatBytes(usableSpace) + ", 可能存在日志爆炸或勒索软件加密");
                        anomalies.add(data);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测文件系统变化过程中发生错误", e);
        }
    }
    
    /**
     * 检测系统日志
     */
    private void detectSystemLogs(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 在Windows上读取事件日志或在Linux上读取syslog需要额外的库和权限
            // 这里使用简化的方法，检查系统日志文件的大小变化
            
            String[] logPaths = {
                "C:\\Windows\\System32\\winevt\\Logs", // Windows 事件日志
                "/var/log" // Linux 日志
            };
            
            for (String logPath : logPaths) {
                File logDir = new File(logPath);
                if (logDir.exists() && logDir.isDirectory()) {
                    File[] logFiles = logDir.listFiles();
                    
                    if (logFiles != null) {
                        // 检查大小异常的日志文件
                        for (File logFile : logFiles) {
                            // 检查是否是大于100MB的日志文件
                            if (logFile.isFile() && logFile.length() > 100 * 1024 * 1024) {
                                MonitorData data = new MonitorData();
                                data.setUserId(userId);
                                data.setDataType("系统日志异常");
                                data.setValue(logFile.length() / (1024.0 * 1024));
                                data.setExpectedMin(0.0);
                                data.setExpectedMax(100.0);
                                data.setDeviation((logFile.length() / (1024.0 * 1024) - 50) / 50 * 100);
                                data.setIsAnomaly(true);
                                data.setResolved(false);
                                data.setRecordTime(time);
                                data.setSource("入侵检测");
                                data.setDescription("发现异常大小的日志文件: " + logFile.getAbsolutePath() + 
                                        ", 大小: " + FormatUtil.formatBytes(logFile.length()) + 
                                        ", 可能存在日志爆炸或系统异常");
                                anomalies.add(data);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测系统日志过程中发生错误", e);
        }
    }
    
    /**
     * 检测端口扫描
     */
    private void detectPortScanning(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 获取当前活跃的网络连接
            List<oshi.software.os.InternetProtocolStats.IPConnection> connections = 
                systemInfo.getOperatingSystem().getInternetProtocolStats().getConnections();
            
            // 统计单位时间内来自同一IP的连接尝试数量
            Map<String, Integer> connectionsByIP = new HashMap<>();
            for (oshi.software.os.InternetProtocolStats.IPConnection conn : connections) {
                String foreignIP = Arrays.toString(conn.getForeignAddress());
                // 忽略本地回环地址和内部网络
                if (!foreignIP.equals("0.0.0.0") && !foreignIP.equals("127.0.0.1") && !foreignIP.startsWith("192.168.") 
                    && !foreignIP.startsWith("10.") && !foreignIP.startsWith("172.16.")) {
                    connectionsByIP.put(foreignIP, connectionsByIP.getOrDefault(foreignIP, 0) + 1);
                }
            }
            
            // 检查是否有IP短时间内尝试连接多个不同端口（端口扫描特征）
            for (Map.Entry<String, Integer> entry : connectionsByIP.entrySet()) {
                if (entry.getValue() > 10) { // 如果单个IP有10个以上的连接
                    MonitorData data = new MonitorData();
                    data.setUserId(userId);
                    data.setDataType("端口扫描检测");
                    data.setValue((double) entry.getValue());
                    data.setExpectedMin(0.0);
                    data.setExpectedMax(10.0);
                    data.setDeviation((entry.getValue() - 5.0) / 5.0 * 100);
                    data.setIsAnomaly(true);
                    data.setResolved(false);
                    data.setRecordTime(time);
                    data.setSource("入侵检测");
                    data.setDescription("检测到可能的端口扫描活动：IP " + entry.getKey() + 
                            " 同时尝试连接 " + entry.getValue() + " 个不同端口");
                    anomalies.add(data);
                }
            }
            
            // 检查常见漏洞利用端口
            int[] vulnerablePorts = {21, 22, 23, 25, 53, 445, 1433, 1434, 3306, 3389, 5432, 5900, 6379};
            Set<Integer> activeVulnerablePorts = new HashSet<>();
            
            for (oshi.software.os.InternetProtocolStats.IPConnection conn : connections) {
                for (int port : vulnerablePorts) {
                    if (conn.getLocalPort() == port) {
                        activeVulnerablePorts.add(port);
                    }
                }
            }
            
            // 如果有易受攻击的端口开放
            if (!activeVulnerablePorts.isEmpty()) {
                MonitorData data = new MonitorData();
                data.setUserId(userId);
                data.setDataType("敏感端口检测");
                data.setValue((double) activeVulnerablePorts.size());
                data.setExpectedMin(0.0);
                data.setExpectedMax(0.0);
                data.setDeviation(100.0);
                data.setIsAnomaly(true);
                data.setResolved(false);
                data.setRecordTime(time);
                data.setSource("入侵检测");
                data.setDescription("检测到敏感端口开放：" + activeVulnerablePorts + 
                        "，这些端口存在潜在安全风险，建议关闭不必要的服务");
                anomalies.add(data);
            }
        } catch (Exception e) {
            log.error("检测端口扫描过程中发生错误", e);
        }
    }
    
    /**
     * 检测文件完整性
     */
    private void detectFileIntegrity(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 关键系统文件路径
            String[] criticalFilePaths = {
                "C:\\Windows\\System32\\drivers",
                "C:\\Windows\\System32\\config",
                "/etc/passwd",
                "/etc/shadow",
                "/etc/hosts",
                "/boot",
                "/bin",
                "/sbin"
            };
            
            // 检查关键文件的修改时间
            for (String path : criticalFilePaths) {
                File file = new File(path);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null) {
                            // 检查最近修改的系统关键文件
                            long currentTime = System.currentTimeMillis();
                            long recentTimeThreshold = currentTime - (24 * 60 * 60 * 1000); // 24小时内
                            
                            for (File sysFile : files) {
                                if (sysFile.isFile() && sysFile.lastModified() > recentTimeThreshold) {
                                    MonitorData data = new MonitorData();
                                    data.setUserId(userId);
                                    data.setDataType("文件完整性");
                                    data.setValue(1.0);
                                    data.setExpectedMin(0.0);
                                    data.setExpectedMax(0.0);
                                    data.setDeviation(100.0);
                                    data.setIsAnomaly(true);
                                    data.setResolved(false);
                                    data.setRecordTime(time);
                                    data.setSource("入侵检测");
                                    data.setDescription("关键系统文件近期被修改：" + sysFile.getAbsolutePath() + 
                                            ", 最后修改时间: " + new Date(sysFile.lastModified()) + 
                                            "，这可能表明系统文件被篡改");
                                    anomalies.add(data);
                                    break; // 每个目录只报告一个异常，避免过多警报
                                }
                            }
                        }
                    } else if (file.isFile() && (System.currentTimeMillis() - file.lastModified()) < (24 * 60 * 60 * 1000)) {
                        // 对于单个文件，检查是否在24小时内被修改
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("文件完整性");
                        data.setValue(1.0);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("关键系统文件近期被修改：" + file.getAbsolutePath() + 
                                ", 最后修改时间: " + new Date(file.lastModified()) + 
                                "，这可能表明系统文件被篡改");
                        anomalies.add(data);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测文件完整性过程中发生错误", e);
        }
    }
    
    /**
     * 检测异常用户活动
     */
    private void detectUserActivity(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 获取当前活跃进程列表
            List<oshi.software.os.OSProcess> processes = systemInfo.getOperatingSystem().getProcesses(null, oshi.software.os.OperatingSystem.ProcessSorting.CPU_DESC, 50);
            
            // 检查是否有高权限进程
            for (oshi.software.os.OSProcess process : processes) {
                // 检查是否有以系统权限运行的可疑进程
                if ("SYSTEM".equals(process.getUser()) || "root".equals(process.getUser())) {
                    // 检查常见的可疑程序
                    String processName = process.getName().toLowerCase();
                    if (processName.contains("mimikatz") || processName.contains("psexec") || 
                        processName.contains("netcat") || processName.contains("wireshark") ||
                        processName.contains("powershell") && process.getCommandLine().toLowerCase().contains("-enc")) {
                        
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("可疑用户活动");
                        data.setValue(1.0);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到高权限可疑进程：" + process.getName() + 
                                " (PID: " + process.getProcessID() + "), 用户: " + process.getUser() + 
                                ", 命令行: " + (process.getCommandLine() != null ? process.getCommandLine() : "未知"));
                        anomalies.add(data);
                    }
                }
            }
            
            // 在Windows系统上检查登录事件
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // 检查特定文件夹的使用情况
                File recentFolder = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Recent");
                if (recentFolder.exists() && recentFolder.isDirectory()) {
                    File[] recentFiles = recentFolder.listFiles();
                    if (recentFiles != null && recentFiles.length > 100) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("用户活动异常");
                        data.setValue((double) recentFiles.length);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(100.0);
                        data.setDeviation((recentFiles.length - 50.0) / 50.0 * 100);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到异常的最近访问文件数量：" + recentFiles.length + 
                                "，这可能表明有自动化程序在访问大量文件");
                        anomalies.add(data);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测用户活动过程中发生错误", e);
        }
    }
    
    /**
     * 检测系统服务异常
     */
    private void detectSystemServices(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 获取当前运行的所有服务
            List<oshi.software.os.OSService> services = systemInfo.getOperatingSystem().getServices();
            
            // 检查可疑服务名称
            String[] suspiciousServiceNames = {
                "remote", "vnc", "proxy", "tunnel", "backdoor", "admin", "remote", "access", 
                "shell", "telnet", "ftp", "control", "hack", "exploit"
            };
            
            for (oshi.software.os.OSService service : services) {
                String serviceName = service.getName().toLowerCase();
                
                // 检查是否包含可疑关键词
                for (String keyword : suspiciousServiceNames) {
                    if (serviceName.contains(keyword)) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("可疑系统服务");
                        data.setValue(1.0);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到可疑系统服务：" + service.getName() + 
                                ", 状态: " + service.getState() + 
                                ", 这可能是未授权的远程访问服务");
                        anomalies.add(data);
                        break;
                    }
                }
                
                // 检查正常关闭后重新启动的服务
                if ("RUNNING".equals(service.getState())) {
                    // 这里只是一个示例，实际上需要与历史状态对比
                    // 由于没有历史数据，这个检测点在实际环境中不会生效
                }
            }
        } catch (Exception e) {
            log.error("检测系统服务过程中发生错误", e);
        }
    }
    
    /**
     * 检测注册表更改 (仅Windows系统)
     */
    private void detectRegistryChanges(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        // 仅在Windows系统下执行
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        
        try {
            // 关键注册表路径检查 - 由于Java不能直接访问Windows注册表，这里只做简单示例
            // 实际应用中可以使用JNA或其他库访问注册表，或使用外部命令
            
            // 检查自启动文件夹
            File startupFolder = new File(System.getProperty("user.home") + 
                    "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            
            if (startupFolder.exists() && startupFolder.isDirectory()) {
                File[] startupFiles = startupFolder.listFiles();
                if (startupFiles != null && startupFiles.length > 0) {
                    // 检查最近添加的自启动项
                    long currentTime = System.currentTimeMillis();
                    long recentTimeThreshold = currentTime - (7 * 24 * 60 * 60 * 1000); // 7天内
                    
                    for (File file : startupFiles) {
                        if (file.lastModified() > recentTimeThreshold) {
                            MonitorData data = new MonitorData();
                            data.setUserId(userId);
                            data.setDataType("注册表/启动项检测");
                            data.setValue(1.0);
                            data.setExpectedMin(0.0);
                            data.setExpectedMax(0.0);
                            data.setDeviation(100.0);
                            data.setIsAnomaly(true);
                            data.setResolved(false);
                            data.setRecordTime(time);
                            data.setSource("入侵检测");
                            data.setDescription("检测到新添加的启动项：" + file.getName() + 
                                    ", 路径: " + file.getAbsolutePath() + 
                                    ", 添加时间: " + new Date(file.lastModified()) + 
                                    "，请确认是否为授权添加");
                            anomalies.add(data);
                        }
                    }
                }
            }
            
            // 检查系统代理设置 (在Windows上通常存储在注册表中)
            // 这里使用简化的检测方法
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("reg", "query", 
                        "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", "/v", "ProxyEnable");
                Process process = processBuilder.start();
                java.io.InputStream inputStream = process.getInputStream();
                byte[] buffer = new byte[1024];
                int length = inputStream.read(buffer);
                String output = "";
                if (length > 0) {
                    output = new String(buffer, 0, length, java.nio.charset.StandardCharsets.UTF_8);
                }
                process.waitFor();
                
                if (output.contains("0x1")) {
                    MonitorData data = new MonitorData();
                    data.setUserId(userId);
                    data.setDataType("注册表/网络设置");
                    data.setValue(1.0);
                    data.setExpectedMin(0.0);
                    data.setExpectedMax(0.0);
                    data.setDeviation(100.0);
                    data.setIsAnomaly(true);
                    data.setResolved(false);
                    data.setRecordTime(time);
                    data.setSource("入侵检测");
                    data.setDescription("检测到系统代理设置已启用，这可能导致网络流量被截获或重定向");
                    anomalies.add(data);
                }
            } catch (Exception e) {
                log.error("检查系统代理设置出错", e);
            }
        } catch (Exception e) {
            log.error("检测注册表更改过程中发生错误", e);
        }
    }
    
    /**
     * 检测恶意软件特征
     */
    private void detectMalware(Long userId, LocalDateTime time, List<MonitorData> anomalies) {
        try {
            // 检查常见恶意软件路径
            String[] suspiciousPaths = {
                System.getProperty("java.io.tmpdir"),  // 临时文件夹
                System.getProperty("user.home") + "\\AppData\\Local\\Temp",  // Windows临时文件夹
                System.getProperty("user.home") + "\\AppData\\Roaming",      // Windows漫游文件夹
                "/tmp",  // Linux临时文件夹
                "/var/tmp"  // Linux临时文件夹
            };
            
            for (String path : suspiciousPaths) {
                File directory = new File(path);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null) {
                        // 检查可疑文件扩展名和名称
                        for (File file : files) {
                            if (file.isFile()) {
                                String fileName = file.getName().toLowerCase();
                                
                                // 检查可疑扩展名
                                if (fileName.endsWith(".vbs") || fileName.endsWith(".js") || 
                                    fileName.endsWith(".hta") || fileName.endsWith(".exe") && file.length() < 100 * 1024) {
                                    
                                    // 检查可疑文件名模式（随机字符等）
                                    if (fileName.matches("^[a-zA-Z0-9]{8,16}\\.(exe|dll|vbs|js|ps1)$") || 
                                        fileName.contains("backdoor") || fileName.contains("trojan") || 
                                        fileName.contains("hack") || fileName.contains("crack") || 
                                        fileName.contains("keylog") || fileName.contains("spy")) {
                                        
                                        MonitorData data = new MonitorData();
                                        data.setUserId(userId);
                                        data.setDataType("恶意软件检测");
                                        data.setValue(1.0);
                                        data.setExpectedMin(0.0);
                                        data.setExpectedMax(0.0);
                                        data.setDeviation(100.0);
                                        data.setIsAnomaly(true);
                                        data.setResolved(false);
                                        data.setRecordTime(time);
                                        data.setSource("入侵检测");
                                        data.setDescription("检测到疑似恶意软件：" + file.getName() + 
                                                ", 路径: " + file.getAbsolutePath() + 
                                                ", 大小: " + FormatUtil.formatBytes(file.length()) + 
                                                ", 创建时间: " + new Date(file.lastModified()));
                                        anomalies.add(data);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 检查异常的网络连接（可能是C&C服务器通信）
            List<oshi.software.os.InternetProtocolStats.IPConnection> connections = 
                systemInfo.getOperatingSystem().getInternetProtocolStats().getConnections();
            
            // 可疑的目标端口和IP前缀
            int[] suspiciousPorts = {6666, 4444, 8080, 1080, 31337, 9001};
            String[] suspiciousIPs = {"185.147.", "5.255.", "185.220.", "171.25."};  // 示例IP段，需要定期更新
            
            for (oshi.software.os.InternetProtocolStats.IPConnection conn : connections) {
                // 检查可疑端口
                for (int port : suspiciousPorts) {
                    if (conn.getForeignPort() == port) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("恶意通信检测");
                        data.setValue(1.0);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到可疑网络连接：本地 " + conn.getLocalAddress() + ":" + conn.getLocalPort() + 
                                " -> 远程 " + conn.getForeignAddress() + ":" + conn.getForeignPort() + 
                                ", 状态: " + conn.getState() + ", 可能是恶意软件C&C通信");
                        anomalies.add(data);
                        break;
                    }
                }
                
                // 检查可疑IP前缀
                String foreignIp = Arrays.toString(conn.getForeignAddress());
                for (String suspIP : suspiciousIPs) {
                    if (foreignIp.startsWith(suspIP)) {
                        MonitorData data = new MonitorData();
                        data.setUserId(userId);
                        data.setDataType("恶意通信检测");
                        data.setValue(1.0);
                        data.setExpectedMin(0.0);
                        data.setExpectedMax(0.0);
                        data.setDeviation(100.0);
                        data.setIsAnomaly(true);
                        data.setResolved(false);
                        data.setRecordTime(time);
                        data.setSource("入侵检测");
                        data.setDescription("检测到连接到可疑IP地址：本地 " + conn.getLocalAddress() + ":" + conn.getLocalPort() + 
                                " -> 远程 " + conn.getForeignAddress() + ":" + conn.getForeignPort() + 
                                ", 这可能是恶意软件C&C服务器");
                        anomalies.add(data);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("检测恶意软件过程中发生错误", e);
        }
    }
} 