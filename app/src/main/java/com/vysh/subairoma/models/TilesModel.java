package com.vysh.subairoma.models;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TilesModel {
    String type, title, description;
    int tileId, tileOrder;
    float percentComplete = 0;

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getType() {
        return type;
    }

    public int getTileOrder() {
        return tileOrder;
    }

    public void setTileOrder(int tileOrder) {
        this.tileOrder = tileOrder;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTileId() {
        return tileId;
    }

    public void setTileId(int tileId) {
        this.tileId = tileId;
    }
}
