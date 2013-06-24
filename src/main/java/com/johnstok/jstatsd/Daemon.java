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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A daemon that listens for Statsd messages over UDP.
 *
 * @author Keith Webster Johnston.
 */
public class Daemon {

    public static void main(final String args[]) throws Exception {
        final Backend backend = new YammerBackend();
        final DatagramSocket serverSocket = new DatagramSocket(7111);
        while (true) {
            final byte[] receiveData = new byte[1024];
            final DatagramPacket receivePacket =
                new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            final String sentence = new String(receivePacket.getData(), "UTF-8"); // TODO: Confirm charset.
            final String[] events = sentence.split("\n");
            for (final String event : events) {
                final Matcher m = Pattern.compile("([^:]+):(-?\\d+(?:\\.\\d+)?)\\|(c|g|h|ms|s)(?:@(\\d+(?:\\.\\d+)?))?").matcher(event.trim());
                if (m.matches()) {
                    final String bucket = m.group(1);
                    final BigDecimal i = new BigDecimal(m.group(2));
                    final String eventType = m.group(3);
                    switch (eventType) {
                        case "h":
                            backend.histogram(bucket, i);
                            break;
                        case "c":
                            backend.count(bucket, i);
                            break;
                        case "g":
                            backend.gauge(bucket, i);
                            break;
                        case "ms":
                            backend.time(bucket, i);
                            break;
                        case "s":
                            backend.mark(bucket, i);
                            break;
                        default:
                            System.err.println("Bad event type: "+eventType);
                    }
                } else {
                    System.err.println("Bad event: " + event);
                }
            }
        }
    }
}
