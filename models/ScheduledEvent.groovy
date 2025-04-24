package models

/**
 * Represents a scheduled event trigger using cron syntax in GitHub Actions
 */
class ScheduledEvent implements Serializable {
    List<String> cron
    
    ScheduledEvent() {
        cron = []
    }
}