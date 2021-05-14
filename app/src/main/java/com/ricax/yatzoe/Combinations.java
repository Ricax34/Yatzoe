package com.ricax.yatzoe;

import java.util.ArrayList;
import java.util.List;

public class Combinations {
    private final int [] input;
    private final int k;                             // sequence length
    private final int n;

    List<int[]> subsets = new ArrayList<>();
    /*
        public Combinations (int my_n, int my_k, int[] my_input) {
            this.k = my_k;
            this.n = my_n;
            this.input = new int[my_n];
            for (int i = 0; i< my_input.length; i++)
                this.input[i]=my_input[i];
        }
    */
    public Combinations (int my_k, List<Integer> my_input) {
        this.k = my_k;
        this.n = my_input.size();
        this.input = new int[n];
        for (int i = 0; i< my_input.size(); i++)
            this.input[i]=my_input.get(i);
    }

    public void Combinate() {
        int[] s = new int[k];                  // here we'll keep indices pointing to elements in input array
        if (k <= input.length) {
            // first index sequence: 0, 1, 2, ...
            for (int i = 0; (s[i] = i) < k - 1; i++);
            subsets.add(getSubset(input, s));
            for(;;) {
                int i;
                // find position of item that can be incremented
                for (i = k - 1; i >= 0 && s[i] == input.length - k + i; i--);
                if (i < 0) {
                    break;
                }
                s[i]++;                    // increment this item
                for (++i; i < k; i++) {    // fill up remaining items
                    s[i] = s[i - 1] + 1;
                }
                subsets.add(getSubset(input, s));
            }
        }
    }
    // generate actual subset by index sequence
    private int[] getSubset(int[] input, int[] subset) {
        int[] result = new int[subset.length];
        for (int i = 0; i < subset.length; i++)
            result[i] = input[subset[i]];
        return result;
    }

    public List<int[]> getSubsets(){
        return this.subsets;
    }
}
