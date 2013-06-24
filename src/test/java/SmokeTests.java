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

import java.util.Random;
import org.junit.Test;



/**
 * A trivial test to check the server works.
 *
 * @author Keith Webster Johnston.
 */
public class SmokeTests {

    @Test
    public void  connectAndSendMessage() throws Exception {
        final Random random = new Random();
        final StatsdClient client = new StatsdClient("localhost", 7111);
        client.increment("foo", random.nextInt(20));
        client.timing("bar", random.nextInt(1000));
        client.gauge("baz", random.nextDouble());
        client.histogram("blat", random.nextInt(100));
        client.histogram("blat", random.nextInt(100));
        client.histogram("blat", random.nextInt(100));
        client.mark("on.your.marks", 1);
        client.flush();
    }
}
