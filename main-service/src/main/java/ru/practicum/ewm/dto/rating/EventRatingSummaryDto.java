package ru.practicum.ewm.dto.rating;

public class EventRatingSummaryDto {

    private long likes;
    private long dislikes;
    private long score;

    public EventRatingSummaryDto() {
    }

    public EventRatingSummaryDto(long likes, long dislikes, long score) {
        this.likes = likes;
        this.dislikes = dislikes;
        this.score = score;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public long getDislikes() {
        return dislikes;
    }

    public void setDislikes(long dislikes) {
        this.dislikes = dislikes;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}
