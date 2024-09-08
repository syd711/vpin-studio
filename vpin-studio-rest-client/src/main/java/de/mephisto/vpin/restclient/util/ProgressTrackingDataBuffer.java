package de.mephisto.vpin.restclient.util;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

public class ProgressTrackingDataBuffer {
    public static Flux<DataBuffer> wrapWithProgress(Flux<DataBuffer> dataBufferFlux, long totalBytes, FileUploadProgressListener listener) {
        final long[] bytesWritten = {0};

        return dataBufferFlux.doOnNext(dataBuffer -> {
            int written = dataBuffer.readableByteCount();
            bytesWritten[0] += written;
            listener.process(((double) bytesWritten[0] / totalBytes) * 100);
        });
    }
}
