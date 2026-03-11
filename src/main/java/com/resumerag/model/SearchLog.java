package com.resumerag.model;

import java.time.LocalDateTime;

public class SearchLog {
    private int logId;
    private String searchKeywords;
    private String skillIds;
    private int resultCount;
    private LocalDateTime searchTime;
    private Integer userId;  // 谁搜索的

    public SearchLog() {}

    // Getter 和 Setter
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public String getSearchKeywords() { return searchKeywords; }
    public void setSearchKeywords(String searchKeywords) { this.searchKeywords = searchKeywords; }

    public String getSkillIds() { return skillIds; }
    public void setSkillIds(String skillIds) { this.skillIds = skillIds; }

    public int getResultCount() { return resultCount; }
    public void setResultCount(int resultCount) { this.resultCount = resultCount; }

    public LocalDateTime getSearchTime() { return searchTime; }
    public void setSearchTime(LocalDateTime searchTime) { this.searchTime = searchTime; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}