package com.caiths.echoapi.;

import com.alibaba.excel.util.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import com.caiths.echoapi.utils.ImgUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageUrlProcessorTest {

    private URL mockUrl;
    private HttpURLConnection mockConnection;
    private InputStream mockInputStream;

    @BeforeEach
    void setUp() throws IOException {
        mockUrl = mock(URL.class);
        mockConnection = mock(HttpURLConnection.class);
        mockInputStream = mock(InputStream.class);

        when(mockUrl.openConnection()).thenReturn(mockConnection);
    }

    @Test
    void testHttpUrlWithSuccessResponse() throws IOException {
        String testUrl = "http://example.com/image.jpg";
        byte[] mockBytes = "test image data".getBytes();
        byte[] compressedBytes = "compressed data".getBytes();

        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockUrl.openStream()).thenReturn(new ByteArrayInputStream(mockBytes));

        try (MockedStatic<IoUtils> ioUtilsMock = Mockito.mockStatic(IoUtils.class);
             MockedStatic<ImgUtil> imgUtilMock = Mockito.mockStatic(ImgUtil.class)) {
            ioUtilsMock.when(() -> IoUtils.toByteArray(any(InputStream.class))).thenReturn(mockBytes);
            imgUtilMock.when(() -> ImgUtil.compressPicForScale(eq(mockBytes), eq(200L), anyString()))
                    .thenReturn(compressedBytes);

            String result = Test.test(testUrl); // 明确调用 com.caiths.echoapi.Test.test()

            assertEquals("success", result);
            verify(mockConnection).getResponseCode();
            verify(mockUrl).openStream();
        }
    }

    @Test
    void testHttpUrlWithNon200Response() throws IOException {
        String testUrl = "http://example.com/image.jpg";

        when(mockConnection.getResponseCode()).thenReturn(404);

        String result = Test.test(testUrl);

        assertEquals(testUrl, result);
        verify(mockConnection).getResponseCode();
        verify(mockUrl, never()).openStream();
    }

    @Test
    void testNonHttpString() throws IOException {
        String testValue = "just a plain string";

        String result = Test.test(testValue);

        assertEquals(testValue, result);
    }

    @Test
    void testHttpUrlWithException() throws IOException {
        String testUrl = "http://example.com/image.jpg";

        when(mockConnection.getResponseCode()).thenThrow(new IOException("Connection failed"));

        String result = Test.test(testUrl);

        assertEquals(testUrl, result);
        verify(mockConnection).getResponseCode();
    }

    @Test
    void testInputStreamClosedProperly() throws IOException {
        String testUrl = "http://example.com/image.jpg";
        byte[] mockBytes = "test image data".getBytes();
        byte[] compressedBytes = "compressed data".getBytes();

        InputStream realInputStream = spy(new ByteArrayInputStream(mockBytes));
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockUrl.openStream()).thenReturn(realInputStream);

        try (MockedStatic<IoUtils> ioUtilsMock = Mockito.mockStatic(IoUtils.class);
             MockedStatic<ImgUtil> imgUtilMock = Mockito.mockStatic(ImgUtil.class)) {
            ioUtilsMock.when(() -> IoUtils.toByteArray(any(InputStream.class))).thenReturn(mockBytes);
            imgUtilMock.when(() -> ImgUtil.compressPicForScale(eq(mockBytes), eq(200L), anyString()))
                    .thenReturn(compressedBytes);

            String result = Test.test(testUrl);

            assertEquals("success", result);
            verify(realInputStream).close();
        }
    }
}