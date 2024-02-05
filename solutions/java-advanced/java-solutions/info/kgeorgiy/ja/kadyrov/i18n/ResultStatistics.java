package info.kgeorgiy.ja.kadyrov.i18n;

public class ResultStatistics<T> {
    int numberOfEntries = 0;
    int numberOfDistinctEntries = 0;
    T minimumValue = null;
    T maximumValue = null;
    T averageValue = null;
    int minimumLength = 0;
    int maximumLength = 0;
    int averageLength = 0;

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public void setNumberOfEntries(int numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }

    public int getNumberOfDistinctEntries() {
        return numberOfDistinctEntries;
    }

    public void setNumberOfDistinctEntries(int numberOfDistinctEntries) {
        this.numberOfDistinctEntries = numberOfDistinctEntries;
    }

    public T getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(T minimumValue) {
        this.minimumValue = minimumValue;
    }

    public T getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(T maximumValue) {
        this.maximumValue = maximumValue;
    }

    public T getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(T averageValue) {
        this.averageValue = averageValue;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    public int getAverageLength() {
        return averageLength;
    }

    public void setAverageLength(int averageLength) {
        this.averageLength = averageLength;
    }
}
