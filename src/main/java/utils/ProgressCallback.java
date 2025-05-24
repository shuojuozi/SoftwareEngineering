// utils/ProgressCallback.java
package utils;
@FunctionalInterface
public interface ProgressCallback {
    /** 0.0 ~ 1.0 */
    void update(double progress);
}
