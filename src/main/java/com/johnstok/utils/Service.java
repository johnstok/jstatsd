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
package com.johnstok.utils;


/**
 * A base class for services.
 *
 * @author Keith Webster Johnston.
 */
public abstract class Service {

    private volatile boolean _running = true;


    public final boolean isRunning() {
        return _running;
    }


    public final void stop() {
        _running = false;
        doStop();
    }


    /**
     * Perform any clean up as the service stops.
     */
    protected abstract void doStop();
}
