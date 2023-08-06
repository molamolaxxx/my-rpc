package com.mola.rpc.common.entity;

import com.mola.rpc.common.utils.JSONUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.math.BigDecimal;

/**
 * @author : molamola
 * @Project: my-rpc
 * @Description: 系统负载信息
 * @date : 2022-10-16 19:32
 **/
public class SystemInfo {

    /**
     * 进程负载
     */
    private BigDecimal processCpuLoad;

    /**
     * 系统cpu负载
     */
    private BigDecimal systemCpuLoad;

    /**
     * 系统cpu平均负载
     */
    private BigDecimal systemLoadAverage;

    /**
     * 总内存
     */
    private BigDecimal totalPhysicalMemorySize;

    /**
     * 剩余内存
     */
    private BigDecimal freePhysicalMemorySize;

    /**
     * full gc频率 /h
     */
    private BigDecimal fullGcFrequency;


    public static SystemInfo get() {
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        SystemInfo systemInfo = JSONUtil.parseObject(JSONUtil.toJSONString(operatingSystemMXBean), SystemInfo.class);
        return systemInfo;
    }

    public BigDecimal getProcessCpuLoad() {
        return processCpuLoad;
    }

    public void setProcessCpuLoad(BigDecimal processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    public BigDecimal getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public void setSystemCpuLoad(BigDecimal systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    public BigDecimal getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public void setSystemLoadAverage(BigDecimal systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    public BigDecimal getFullGcFrequency() {
        return fullGcFrequency;
    }

    public void setFullGcFrequency(BigDecimal fullGcFrequency) {
        this.fullGcFrequency = fullGcFrequency;
    }

    public BigDecimal getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(BigDecimal totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    public BigDecimal getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(BigDecimal freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }
}
