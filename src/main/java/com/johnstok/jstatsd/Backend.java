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
 * API for a statistics backend.
 *
 * @author Keith Webster Johnston.
 */
public interface Backend {

    void count(String bucket, BigDecimal i);

    void gauge(String bucket, BigDecimal i);

    void time(String bucket, BigDecimal i);
}
