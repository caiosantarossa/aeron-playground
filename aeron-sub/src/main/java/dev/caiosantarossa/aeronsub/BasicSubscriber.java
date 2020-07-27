package dev.caiosantarossa.aeronsub;


import io.aeron.Aeron;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.samples.SampleConfiguration;
import io.aeron.samples.SamplesUtil;
import org.agrona.CloseHelper;
import org.agrona.concurrent.SigInt;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicSubscriber {
    private static final int STREAM_ID = SampleConfiguration.STREAM_ID;
    private static final String CHANNEL = SampleConfiguration.CHANNEL;
    private static final int FRAGMENT_COUNT_LIMIT = SampleConfiguration.FRAGMENT_COUNT_LIMIT;
    private static final boolean EMBEDDED_MEDIA_DRIVER = SampleConfiguration.EMBEDDED_MEDIA_DRIVER;

    public static void main(final String[] args) {
        System.out.println("Subscribing to " + CHANNEL + " on stream id " + STREAM_ID);

        final MediaDriver driver = EMBEDDED_MEDIA_DRIVER ? MediaDriver.launchEmbedded() : null;
        final Aeron.Context ctx = new Aeron.Context()
                .availableImageHandler(SamplesUtil::printAvailableImage)
                .unavailableImageHandler(SamplesUtil::printUnavailableImage);

        if (EMBEDDED_MEDIA_DRIVER) {
            ctx.aeronDirectoryName(driver.aeronDirectoryName());
        }

        final FragmentHandler fragmentHandler = SamplesUtil.printStringMessage(STREAM_ID);
        final AtomicBoolean running = new AtomicBoolean(true);

        SigInt.register(() -> running.set(false));

        try (Aeron aeron = Aeron.connect(ctx);
             Subscription subscription = aeron.addSubscription(CHANNEL, STREAM_ID)) {
            SamplesUtil.subscriberLoop(fragmentHandler, FRAGMENT_COUNT_LIMIT, running).accept(subscription);

            System.out.println("Shutting down...");
        }

        CloseHelper.quietClose(driver);
    }
}