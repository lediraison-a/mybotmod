package net.sombrage.testmod;

public class TestModSettings {


    // ignoreHandBar is used to ignore the hand bar when sorting
    public boolean ignoreHandBar;

    // exploreUnknownFilters is used to explore unknown filters before sorting
    public boolean exploreUnknownFilters;

    // endAtStartPosition is used to end the sort at the start position
    public boolean endAtStartPosition;

    // repeatSort is used to repeat the sort
    public boolean repeatSort;

    public TestModSettings() {
        ignoreHandBar = true;
        exploreUnknownFilters = true;
        endAtStartPosition = true;
        repeatSort = false;
    }
}
