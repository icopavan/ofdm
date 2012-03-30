/*
 * Copyright (C) 2009-2012 Oleksandr Natalenko aka post-factum
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Universal Program License as published by
 * Oleksandr Natalenko aka post-factum; see file COPYING for details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the Universal Program
 * License along with this program; if not, write to
 * pfactum@gmail.com
 */

package org.pf.ofdm;

public class PseudoRNG
{
	private static long seed = System.currentTimeMillis();
	private static final double mod = Math.pow(2, 63);
	
	public static double nextDouble()
	{
		seed ^= (seed << 21);
		seed ^= (seed >>> 35);
		seed ^= (seed << 4);
		return Math.abs((seed % mod) / mod);
	}
}
