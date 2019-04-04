package org.apache.http.cta;

import dalvik.system.PathClassLoader;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;

import java.lang.reflect.Method;

public final class CtaAdapter {
    /**
    * M: Support MoM MMS checking.
    */
    private static Method ctaCheckRequestIsMmsAndEmailSendingPermitted;
    private static Method ctaReturnBadHttpResponse;

    public static boolean isSendingPermitted(HttpRequest request, HttpParams defaultParams) {
        System.out.println("apache-http CtaAdapter isSendingPermitted");
        try {
            if (ctaCheckRequestIsMmsAndEmailSendingPermitted == null) {
                String jarPath = "system/framework/mediatek-cta.jar";
                ClassLoader classLoader = new PathClassLoader(jarPath, CtaAdapter.class.getClassLoader());
                String className = "com.mediatek.cta.CtaHttp";

                Class<?> cls = Class.forName(className, false, classLoader);
                ctaCheckRequestIsMmsAndEmailSendingPermitted =
                        cls.getMethod("isMmsAndEmailSendingPermitted",
                        HttpRequest.class, HttpParams.class);
            }
            return (Boolean) ctaCheckRequestIsMmsAndEmailSendingPermitted.invoke(null,
                    request, defaultParams);
        } catch (ReflectiveOperationException e) {
            System.out.println("apache-http e:" + e);
            e.printStackTrace();
            if (e.getCause() instanceof SecurityException) {
                throw new SecurityException(e.getCause());
            } else if (e.getCause() instanceof ClassNotFoundException) {
                // for project not include CTA feature
            }
        } catch (Exception ee) {
            System.out.println("ee:" + ee);
        }
        return true;
    }

    public static HttpResponse returnBadHttpResponse() {
        try {
            if (ctaReturnBadHttpResponse == null) {
                String jarPath = "system/framework/mediatek-cta.jar";
                ClassLoader classLoader = new PathClassLoader(jarPath, CtaAdapter.class.getClassLoader());
                String className = "com.mediatek.cta.CtaHttp";

                Class<?> cls = Class.forName(className, false, classLoader);
                ctaReturnBadHttpResponse =
                        cls.getMethod("returnBadResponse");
            }
            return (HttpResponse) ctaReturnBadHttpResponse.invoke(null);
        } catch (ReflectiveOperationException e) {
            if (e.getCause() instanceof SecurityException) {
                throw new SecurityException(e.getCause());
            } else if (e.getCause() instanceof ClassNotFoundException) {
                // for project not include CTA feature
            }
        } catch (Exception ee) {
            System.out.println("ee:" + ee);
        }
        return null;
    }
}



