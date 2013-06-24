/*-----------------------------------------------------------------------------
 * Copyright © 2013 Keith Webster Johnston.
 * All rights reserved.
 *
 * This file is part of jstatsd.
 *
 * jstatsd is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * jstatsd is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jstatsd. If not, see <http://www.gnu.org/licenses/>.
 *---------------------------------------------------------------------------*/
package com.johnstok.jstatsd;

import java.math.BigDecimal;


/**
 * A backend that writes events to the console.
 *
 * @author Keith Webster Johnston.
 */
public class ConsoleBackend
    implements
        Backend {


    /** {@inheritDoc} */
    @Override
    public void histogram(final String bucket, final BigDecimal i) {
        System.out.println(bucket+":"+i+"|h");
    }


    /** {@inheritDoc} */
    @Override
    public void count(final String bucket, final BigDecimal i) {
        System.out.println(bucket+":"+i+"|c");
    }


    /** {@inheritDoc} */
    @Override
    public void gauge(final String bucket, final BigDecimal i) {
        System.out.println(bucket+":"+i+"|g");
    }


    /** {@inheritDoc} */
    @Override
    public void time(final String bucket, final BigDecimal i) {
        System.out.println(bucket+":"+i+"|ms");
    }


    /** {@inheritDoc} */
    @Override
    public void mark(final String bucket, final BigDecimal i) {
        System.out.println(bucket+":"+i+"|s");
    }
}
