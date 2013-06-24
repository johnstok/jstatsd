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


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


/**
 * A service that receives UDP packets.
 *
 * @author Keith Webster Johnston.
 */
public abstract class DatagramReceiver
    extends
        RunnableService {

    protected final DatagramSocket _serverSocket;


    /**
     * Constructor.
     *
     * @throws SocketException If it isn't possible to open a UDP socket.
     */
    public DatagramReceiver() throws SocketException {
        _serverSocket = new DatagramSocket(7111);
    }


    /** {@inheritDoc} */
    @Override
    public final void run() {
        while (isRunning()) {
            try {
                final byte[] receiveData = new byte[1024];
                final DatagramPacket receivePacket =
                new DatagramPacket(receiveData, receiveData.length);
                _serverSocket.receive(receivePacket);
                processPacket(receivePacket);
            } catch (IOException | RuntimeException e) {
                System.err.println("Error receiving packet: "+e.getMessage());
            }
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void doStop() {
        _serverSocket.close();
    }


    /**
     * Process a received packet.
     *
     * @param receivePacket The packet to process.
     */
    protected abstract void processPacket(DatagramPacket receivePacket);
}