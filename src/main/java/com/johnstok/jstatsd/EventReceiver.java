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
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.johnstok.utils.DatagramReceiver;


/**
 * A Statsd event processor that receives messages via UDP.
 *
 * @author Keith Webster Johnston.
 */
public class EventReceiver
    extends
        DatagramReceiver {

    private static final Pattern EVENT =
        Pattern.compile(
            "([^:]+):(-?\\d+(?:\\.\\d+)?)\\|(c|g|h|ms|s)(?:@(\\d+(?:\\.\\d+)?))?");

    private final Backend _backend;


    /**
     * Constructor.
     *
     * @param backend The back end used to process events.
     *
     * @throws SocketException If it isn't possible to open a UDP socket.
     */
    public EventReceiver(final Backend backend) throws SocketException {
        _backend = backend; // TODO: Check not null.
    }


    /** {@inheritDoc} */
    @Override
    protected void processPacket(final DatagramPacket receivePacket) {
        final String sentence =
            new String(receivePacket.getData(), StandardCharsets.UTF_8).trim();
        final String[] events = sentence.split("\n");
        for (final String event : events) {
            if (event.isEmpty()) { continue; }
            final Matcher m = EVENT.matcher(event.trim());
            if (m.matches()) {
                final String bucket = m.group(1);
                final BigDecimal i = new BigDecimal(m.group(2));
                final String eventType = m.group(3);
                switch (eventType) {
                    case "h":
                        _backend.histogram(bucket, i);
                        break;
                    case "c":
                        _backend.count(bucket, i);
                        break;
                    case "g":
                        _backend.gauge(bucket, i);
                        break;
                    case "ms":
                        _backend.time(bucket, i);
                        break;
                    case "s":
                        _backend.mark(bucket, i);
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
