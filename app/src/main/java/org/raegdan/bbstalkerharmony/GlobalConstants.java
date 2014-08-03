package org.raegdan.bbstalkerharmony;

public class GlobalConstants {
    //Logical pages
    public static final int ERRORNEOUS_PAGE = -1;        //Should never occur
    public static final int PAGE_HOME = 0;
    public static final int PAGE_ALL_FIGURES = 1;
    public static final int PAGE_ALL_WAVES = 2;
    public static final int PAGE_COLLECTION = 3;
    public static final int PAGE_WISHLIST = 4;
    public static final int PAGE_DETECTOR = 5;
    public static final int PAGE_HELP = 6;
    public static final int PAGE_CONFIG = 7;
    public static final int PAGE_SEARCH = 8;
    public static final int PAGE_WAVE = 9;
    public static final int PAGE_DETECTOR_RESULTS = 10;

    //FiguresListFragment calling modes
    public static final int QUERY_NO_QUERY = -1;
    public static final int QUERY_ALL_FIGURES = 0;
    public static final int QUERY_WAVE = 1;
    public static final int QUERY_SEARCH = 2;
    public static final int QUERY_COLLECTION = 3;
    public static final int QUERY_WISHLIST = 4;
    public static final int QUERY_DETECTOR = 5;

    //FiguresListFragment arg names
    public static final String ARG_MODE = "mode";
    public static final String ARG_QUERY = "query";

}
