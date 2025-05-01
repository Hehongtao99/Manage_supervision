package com.example.auth.service;

import com.example.auth.entity.SystemProcess;

import java.util.List;

/**
 * 系统进程服务接口
 */
public interface SystemProcessService {
    
    /**
     * 获取系统当前运行的进程列表
     * @return 进程信息列表
     */
    List<SystemProcess> getProcessList();
    
    /**
     * 获取系统当前运行的进程列表，不强制刷新缓存
     * @return 进程信息列表
     */
    List<SystemProcess> getProcessListWithoutRefresh();
    
    /**
     * 获取系统当前运行的进程列表（分页版本）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 进程信息列表
     */
    List<SystemProcess> getProcessListPaged(int page, int size);
    
    /**
     * 获取系统当前运行的进程列表（限制数量）
     * @param limit 最大返回数量
     * @param sortByCpu 是否按CPU使用率排序（true为按CPU使用率降序，false为按内存使用量降序）
     * @return 进程信息列表
     */
    List<SystemProcess> getTopProcesses(int limit, boolean sortByCpu);
    
    /**
     * 根据进程ID获取进程详细信息
     * @param pid 进程ID
     * @return 进程详细信息
     */
    SystemProcess getProcessById(Long pid);
    
    /**
     * 根据进程名称搜索进程
     * @param name 进程名称（支持模糊搜索）
     * @return 进程信息列表
     */
    List<SystemProcess> searchProcessByName(String name);
} 