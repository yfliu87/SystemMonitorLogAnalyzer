package com.SystemMonitor.Algorithm;

import java.util.Collections;
import java.util.Comparator;

class FrequencyComparitorinHeaderTable implements Comparator<FPTree>{

    public FrequencyComparitorinHeaderTable() {
    }

    public int compare(FPTree o1, FPTree o2) {
        if(o1.count>o2.count){
            return 1;
        }
        else if(o1.count < o2.count)
            return -1;
        else
            return 0;
    }

}
