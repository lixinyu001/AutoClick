package com.example.autoclicker.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClickScript implements Serializable {
    private String name;
    private List<ClickStep> steps;
    private long repeatCount;
    private long clickInterval;
    private long randomDelay;
    private long clickDuration;
    private boolean isLoop;

    public ClickScript(String name) {
        this.name = name;
        this.steps = new ArrayList<>();
        this.repeatCount = 0;
        this.clickInterval = 1000;
        this.randomDelay = 100;
        this.clickDuration = 100;
        this.isLoop = false;
    }

    public void addStep(ClickStep step) {
        steps.add(step);
    }

    public void insertStep(int position, ClickStep step) {
        if (position >= 0 && position <= steps.size()) {
            steps.add(position, step);
        }
    }

    public void removeStep(int position) {
        if (position >= 0 && position < steps.size()) {
            steps.remove(position);
        }
    }

    public void updateStep(int position, ClickStep step) {
        if (position >= 0 && position < steps.size()) {
            steps.set(position, step);
        }
    }

    public ClickStep getStep(int position) {
        if (position >= 0 && position < steps.size()) {
            return steps.get(position);
        }
        return null;
    }

    public List<ClickStep> getSteps() {
        return steps;
    }

    public int getStepCount() {
        return steps.size();
    }

    public void clearSteps() {
        steps.clear();
    }

    public void swapSteps(int position1, int position2) {
        if (position1 >= 0 && position1 < steps.size() && 
            position2 >= 0 && position2 < steps.size()) {
            ClickStep temp = steps.get(position1);
            steps.set(position1, steps.get(position2));
            steps.set(position2, temp);
        }
    }

    public void moveStepUp(int position) {
        if (position > 0 && position < steps.size()) {
            swapSteps(position, position - 1);
        }
    }

    public void moveStepDown(int position) {
        if (position >= 0 && position < steps.size() - 1) {
            swapSteps(position, position + 1);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(long repeatCount) {
        this.repeatCount = repeatCount;
    }

    public long getClickInterval() {
        return clickInterval;
    }

    public void setClickInterval(long clickInterval) {
        this.clickInterval = clickInterval;
    }

    public long getRandomDelay() {
        return randomDelay;
    }

    public void setRandomDelay(long randomDelay) {
        this.randomDelay = randomDelay;
    }

    public long getClickDuration() {
        return clickDuration;
    }

    public void setClickDuration(long clickDuration) {
        this.clickDuration = clickDuration;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public static class ClickStep implements Serializable {
        private float x;
        private float y;
        private StepType type;
        private long delay;
        private String description;
        private int repeat;

        public ClickStep(float x, float y, StepType type, String description) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.delay = 0;
            this.description = description;
            this.repeat = 1;
        }

        public ClickStep(float x, float y, StepType type, long delay, String description) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.delay = delay;
            this.description = description;
            this.repeat = 1;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public StepType getType() {
            return type;
        }

        public void setType(StepType type) {
            this.type = type;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getRepeat() {
            return repeat;
        }

        public void setRepeat(int repeat) {
            this.repeat = repeat;
        }

        @Override
        public String toString() {
            return String.format("%s: (%.0f, %.0f) - %s", 
                type.name(), x, y, description);
        }
    }

    public enum StepType {
        CLICK("点击"),
        LONG_CLICK("长按"),
        SWIPE("滑动"),
        WAIT("等待"),
        SCROLL("滚动");

        private final String displayName;

        StepType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
