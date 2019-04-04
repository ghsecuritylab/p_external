package com.squareup.okhttp.internal.cta;

import dalvik.system.PathClassLoader;

import com.squareup.okhttp.Connection;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.lang.Byte;
import java.lang.ClassLoader;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;

public final class CtaAdapter {
    /**
    * M: Support MoM MMS checking.
    */
    private static Method ctaCheckPduIsMmsAndEmailSendingPermitted;
    private static Method ctaCheckRequestIsMmsAndEmailSendingPermitted;
    private static Method ctaGetBadHttpResponse;

    public static boolean isSendingPermitted(byte[] pdu) {
        try {
            if (ctaCheckPduIsMmsAndEmailSendingPermitted == null) {
                String jarPath = "system/framework/mediatek-cta.jar";
                ClassLoader classLoader = new PathClassLoader(jarPath,CtaAdapter.class.getClassLoader());
                String className = "com.mediatek.cta.CtaHttp";

                Class<?> cls = Class.forName(className, false, classLoader);
                ctaCheckPduIsMmsAndEmailSendingPermitted =
                        cls.getMethod("isMmsAndEmailSendingPermitted", new Class[]{Byte[].class});
            }
            return (Boolean) ctaCheckPduIsMmsAndEmailSendingPermitted.invoke(null, new Object[]{pdu});
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof SecurityException) {
                throw new SecurityException(e.getCause());
            } else if (e.getCause() instanceof ClassNotFoundException) {
                // for project not include CTA feature
            }
        } catch (Throwable ee) {
            if (ee instanceof NoClassDefFoundError) {
                System.out.println("ee:" + ee);
            }
        }
        return true;
    }

    public static boolean isSendingPermitted(Request request) {
        try {
            if (ctaCheckRequestIsMmsAndEmailSendingPermitted == null) {
                String jarPath = "system/framework/mediatek-cta.jar";
                ClassLoader classLoader = new PathClassLoader(jarPath,CtaAdapter.class.getClassLoader());
                String className = "com.mediatek.cta.CtaHttp";

                Class<?> cls = Class.forName(className, false, classLoader);
                ctaCheckRequestIsMmsAndEmailSendingPermitted =
                        cls.getMethod("isMmsAndEmailSendingPermitted", Request.class);
            }
            return (Boolean) ctaCheckRequestIsMmsAndEmailSendingPermitted.invoke(null, request);
        } catch (ReflectiveOperationException e) {
            System.out.println("e:" + e);
            if (e.getCause() instanceof SecurityException) {
                throw new SecurityException(e.getCause());
            } else if (e.getCause() instanceof ClassNotFoundException) {
                // for project not include CTA feature
            }
        } catch (Throwable ee) {
            if (ee instanceof NoClassDefFoundError) {
                System.out.println("ee:" + ee);
            }
        }
        return true;
    }

    public static Response getBadHttpResponse() {
        try {
            if (ctaGetBadHttpResponse == null) {
                String jarPath = "system/framework/mediatek-cta.jar";
                ClassLoader classLoader = new PathClassLoader(jarPath,CtaAdapter.class.getClassLoader());
                String className = "com.mediatek.cta.CtaHttp";

                Class<?> cls = Class.forName(className, false, classLoader);
                ctaGetBadHttpResponse =
                        cls.getMethod("getBadResponse");
            }
            return (Response) ctaGetBadHttpResponse.invoke(null);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof SecurityException) {
                throw new SecurityException(e.getCause());
            } else if (e.getCause() instanceof ClassNotFoundException) {
                // for project not include CTA feature
            }
        } catch (Throwable ee) {
            if (ee instanceof NoClassDefFoundError) {
                System.out.println("ee:" + ee);
            }
        }
        return null;
    }

    public static void updateMmsBufferSize(Request request, Connection conn) {
        int bufferSize = hasSocketBufferSize();
        if (bufferSize > 0) {
            try {
                Socket s = conn.getSocket();
                if (s != null) {
                    System.out.println("Configure MMS buffer size:" + bufferSize);
                    s.setSendBufferSize(bufferSize);
                    s.setReceiveBufferSize(bufferSize * 2);
                }
            } catch (Exception e) {
                System.out.println("Socket Buffer size:" + e);
            }
        }
    }

    public static int hasSocketBufferSize() {
        String bufferSize = System.getProperty("socket.buffer.size", "");
        if (bufferSize.length() > 0) {
            try {
                return Integer.parseInt(bufferSize);
            } catch (Exception e) {
                System.out.println("hasMmsBufferSize:" + e);
            }
        }
        return 0;
    }

    public static boolean isMoMMS(Request request) {
        final String mimetype = "application/vnd.wap.mms-message";

        if ("POST".equals(request.method())) {
            String userAgent = request.header("User-Agent");
            if (userAgent != null && userAgent.indexOf("MMS") != -1) {
                return true;
            } else {
                String contentType = request.header("Content-Type");
                if (contentType != null) {
                    if (contentType.indexOf(mimetype) != -1) {
                        return true;
                    }
                }
                String acceptType = request.header("Accept");
                if (acceptType != null) {
                    if (acceptType.indexOf(mimetype) != -1) {
                        return true;
                    }
                }
                List<String> contentTypes = request.headers().values("Content-Type");
                for (String value : contentTypes) {
                    if (value.indexOf(mimetype) != -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

