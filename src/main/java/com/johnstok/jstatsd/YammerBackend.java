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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;


/**
 * A backend that sends events to Yammer Metrics.
 *
 * @author Keith Webster Johnston.
 */
public class YammerBackend
    implements
        Backend {

    private static final class BigDecimalGauge
        implements
            Gauge<BigDecimal> {

        private volatile BigDecimal _i;


        BigDecimalGauge(final BigDecimal i) {
            _i = i;
        }


        @Override
        public BigDecimal getValue() {
            return _i;
        }


        public void setValue(final BigDecimal i) {
            _i = i;
        }
    }


    private final ConcurrentMap<String, BigDecimalGauge> _gauges =
        new ConcurrentHashMap<>();
    private final MetricRegistry _metrics = new MetricRegistry();
    private final ConsoleReporter reporter =
        ConsoleReporter
            .forRegistry(_metrics)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();


    /**
     * Constructor.
     */
    public YammerBackend() {
        reporter.start(10, TimeUnit.SECONDS);
    }

    /** {@inheritDoc} */
    @Override
    public void count(final String bucket, final BigDecimal i) {
        _metrics.counter(bucket).inc(i.longValue()); // FIXME: Long conversion can lose / corrupt information.
    }


    /** {@inheritDoc} */
    @Override
    public void gauge(final String bucket, final BigDecimal i) {
        final BigDecimalGauge g = new BigDecimalGauge(i);
        final BigDecimalGauge previous = _gauges.putIfAbsent(bucket, g);
        if (null==previous) {
            _metrics.register(bucket, g);
        } else {
            previous.setValue(i);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void time(final String bucket, final BigDecimal i) {
        _metrics.timer(bucket).update(i.longValue(), TimeUnit.MILLISECONDS);
    }
}
