/*
 * Copyright (c) 2006-07, The Trustees of Stanford University.  All
 * rights reserved.
 * Licensed under the terms of the GNU GPL; see COPYING for details.
 */

/**
 * @author Sixiang Ma
 */
public class NoNullPointer {
	public static void main(String[] args) {
                String str = "Hello";
                System.out.println(str);

                Object a = new Object();
                a.toString();
	}
}


