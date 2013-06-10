/**
 * StatsdClient.java
 *
 * (C) 2011 Meetup, Inc.
 * Author: Andrew Gwozdziewycz <andrew@meetup.com>, @apgwoz
 *
 *
 *
 * Example usage:
 *
 *    StatsdClient client = new StatsdClient("statsd.example.com", 8125);
 *    // increment by 1
 *    client.increment("foo.bar.baz");
 *    // increment by 10
 *    client.increment("foo.bar.baz", 10);
 *    // sample rate
 *    client.increment("foo.bar.baz", 10, .1);
 *    // increment multiple keys by 1
 *    client.increment("foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *    // increment multiple keys by 10 -- yeah, it's "backwards"
 *    client.increment(10, "foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *    // multiple keys with a sample rate
 *    client.increment(10, .1, "foo.bar.baz", "foo.bar.boo", "foo.baz.bar");
 *
 *    // To enable multi metrics (aka more than 1 metric in a UDP packet) (disabled by default)
 *    client.enableMultiMetrics(true);  //disable by passing in false
 *    // To fine-tune udp packet buffer size (default=1500)
 *    client.setBufferSize((short) 1500);
 *    // To force flush the buffer out (good idea to add to your shutdown path)
 *    client.flush();
 *
 *
 * Note: For best results, and greater availability, you'll probably want to
 * create a wrapper class which creates a static client and proxies to it.
 *
 * You know... the "Java way."
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsdClient extends TimerTask {
        private ByteBuffer sendBuffer;
        private Timer flushTimer;
        private boolean multi_metrics = false;

	private static final Random RNG = new Random();
	private static final Logger log = Logger.getLogger(StatsdClient.class.getName());

	private final InetSocketAddress _address;
	private final DatagramChannel _channel;

	public StatsdClient(final String host, final int port) throws UnknownHostException, IOException {
		this(InetAddress.getByName(host), port);
	}

	public StatsdClient(final InetAddress host, final int port) throws IOException {
		_address = new InetSocketAddress(host, port);
		_channel = DatagramChannel.open();
                setBufferSize((short) 1500);
	}

        @Override
        protected void finalize() {
                flush();
        }

        public synchronized void setBufferSize(final short packetBufferSize) {
                if(sendBuffer != null) {
                        flush();
                }
                sendBuffer = ByteBuffer.allocate(packetBufferSize);
        }

        public synchronized void enableMultiMetrics(final boolean enable) {
                multi_metrics = enable;
        }

        public synchronized boolean startFlushTimer(long period) {
                if(flushTimer == null) {
                        // period is in msecs
                        if(period <= 0) { period = 2000; }
                        flushTimer = new Timer();

                        // We pass this object in as the TimerTask (which calls run())
                        flushTimer.schedule(this, period, period);
                        return true;
                }
                return false;
        }

        public synchronized void stopFlushTimer() {
                if(flushTimer != null) {
                        flushTimer.cancel();
                        flushTimer = null;
                }
        }

        @Override
        public void run() {     // used by Timer, we're a Runnable TimerTask
                flush();
        }


	public boolean timing(final String key, final int value) {
		return timing(key, value, 1.0);
	}

	public boolean timing(final String key, final int value, final double sampleRate) {
		return send(sampleRate, String.format(Locale.ENGLISH, "%s:%d|ms", key, value));
	}

	public boolean decrement(final String key) {
		return increment(key, -1, 1.0);
	}

	public boolean decrement(final String key, final int magnitude) {
		return decrement(key, magnitude, 1.0);
	}

	public boolean decrement(final String key, int magnitude, final double sampleRate) {
		magnitude = magnitude < 0 ? magnitude : -magnitude;
		return increment(key, magnitude, sampleRate);
	}

	public boolean decrement(final String... keys) {
		return increment(-1, 1.0, keys);
	}

	public boolean decrement(int magnitude, final String... keys) {
		magnitude = magnitude < 0 ? magnitude : -magnitude;
		return increment(magnitude, 1.0, keys);
	}

	public boolean decrement(int magnitude, final double sampleRate, final String... keys) {
		magnitude = magnitude < 0 ? magnitude : -magnitude;
		return increment(magnitude, sampleRate, keys);
	}

	public boolean increment(final String key) {
		return increment(key, 1, 1.0);
	}

	public boolean increment(final String key, final int magnitude) {
		return increment(key, magnitude, 1.0);
	}

	public boolean histogram(final String key, final int magnitude) {
		final String stat = String.format(Locale.ENGLISH, "%s:%s|h", key, magnitude);
		return send(1.0, stat);
	}

	public boolean increment(final String key, final int magnitude, final double sampleRate) {
	    final String stat = String.format(Locale.ENGLISH, "%s:%s|c", key, magnitude);
	    return send(sampleRate, stat);
	}

	public boolean increment(final int magnitude, final double sampleRate, final String... keys) {
		final String[] stats = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			stats[i] = String.format(Locale.ENGLISH, "%s:%s|c", keys[i], magnitude);
		}
		return send(sampleRate, stats);
	}

	public boolean gauge(final String key, final double magnitude){
		return gauge(key, magnitude, 1.0);
	}

	public boolean gauge(final String key, final double magnitude, final double sampleRate){
		final String stat = String.format(Locale.ENGLISH, "%s:%s|g", key, magnitude);
		return send(sampleRate, stat);
	}

	private boolean send(final double sampleRate, final String... stats) {

		boolean retval = false; // didn't send anything
		if (sampleRate < 1.0) {
			for (String stat : stats) {
				if (RNG.nextDouble() <= sampleRate) {
					stat = String.format(Locale.ENGLISH, "%s|@%f", stat, sampleRate);
					if (doSend(stat)) {
						retval = true;
					}
				}
			}
		} else {
			for (final String stat : stats) {
				if (doSend(stat)) {
					retval = true;
				}
			}
		}

		return retval;
	}

	private synchronized boolean doSend(final String stat) {
                try {
                        final byte[] data = stat.getBytes("utf-8");

                        // If we're going to go past the threshold of the buffer then flush.
                        // the +1 is for the potential '\n' in multi_metrics below
                        if(sendBuffer.remaining() < (data.length + 1)) {
                                flush();
                        }

                        if(sendBuffer.position() > 0) {         // multiple metrics are separated by '\n'
                                sendBuffer.put( (byte) '\n');
                        }

                        sendBuffer.put(data);   // append the data

                        if(! multi_metrics) {
                                flush();
                        }

                        return true;

		} catch (final IOException e) {
			log.log(Level.WARNING,
					String.format("Could not send stat %s to host %s:%d", sendBuffer.toString(), _address.getHostName(),
							_address.getPort()), e);
			return false;
		}
        }

        public synchronized boolean flush() {
		try {
                        final int sizeOfBuffer = sendBuffer.position();

                        if(sizeOfBuffer <= 0) { return false; } // empty buffer

                        // send and reset the buffer
                        sendBuffer.flip();
			final int nbSentBytes = _channel.send(sendBuffer, _address);
                        sendBuffer.limit(sendBuffer.capacity());
                        sendBuffer.rewind();

			if (sizeOfBuffer == nbSentBytes) {
				return true;
			} else {
				log.warning(String.format(
						"Could not send entirely stat %s to host %s:%d. Only sent %d bytes out of %d bytes", sendBuffer.toString(),
						_address.getHostName(), _address.getPort(), nbSentBytes, sizeOfBuffer));
				return false;
			}

		} catch (final IOException e) {
			log.log(Level.WARNING,
					String.format("Could not send stat %s to host %s:%d", sendBuffer.toString(), _address.getHostName(),
							_address.getPort()), e);
			return false;
		}
	}
}
