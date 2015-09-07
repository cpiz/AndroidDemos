package com.cpiz.android.playground.OkHttpSpeedLimit;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.internal.Util;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by caijw on 2015/9/7.
 */
public class LimitRequestBody {
    private static final String TAG = "LimitRequestBody";

    private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE
    private static final int MAX_BYTES_PER_SECOND = 20 * 1024;

    /**
     * Returns a new request body that transmits the content of {@code file}.
     */
    public static RequestBody create(final MediaType contentType, final File file) {
        if (file == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            /**
             * 限速处理
             * @param sink
             * @throws IOException
             */
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file);
                    // 流控变量
                    long read;
                    long nowTime = 0;
                    long lastStatTime = 0;      // 上次统计时间
                    long lastBatchWritten = 0;  // 上次统计至当前时间内累计写入的数据量
                    long expectSpendTime;
                    long actualSpendTime;
                    long sleepTime;

                    while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                        /**
                         * 限速的原理是每次写数据时，计算一次根据限速的预期时间，
                         * 如果长于实际用时，则将线程休眠相应时间进行等待
                         */
                        nowTime = System.currentTimeMillis();
                        if (nowTime - lastStatTime > 0) {
                            expectSpendTime = (lastBatchWritten * 1000) / MAX_BYTES_PER_SECOND;
                            actualSpendTime = nowTime - lastStatTime;
                            sleepTime = expectSpendTime - actualSpendTime;
                            lastStatTime = nowTime;
                            if (sleepTime > 0) {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                lastStatTime += sleepTime;
                            }
                            lastBatchWritten = 0;
                        }

                        lastBatchWritten += read;
                        sink.flush();
//                        Log.v(TAG, String.format("progress = %d/%d", written, total));
                    }
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
