/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */

/**
 * @author Sixiang Ma
 */
public class Liveness {
	public static void main(String[] args) {
                int a, b, c;
         
                a = b = c = 0;
                a ++;
                b = a + 2;
                c = b + 3;

                System.out.println(c);
                a ++;
                System.out.println(a+b);
	}
}


