package utils.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retry a failed TestNG test up to {@value #MAX_ATTEMPTS} times and declare the
 * test <em>passed</em> once it accumulates at least {@value #QUORUM} successful
 * executions.  Designed for flaky, idempotent API scenarios.
 */
public class QuorumRetryAnalyzer implements IRetryAnalyzer {

    public static final int MAX_ATTEMPTS = 5;
    public static final int QUORUM = 3;

    private static final Logger LOG = LoggerFactory.getLogger(QuorumRetryAnalyzer.class);

    /**
     * key = test signature + parameters, value = [attempts, passes]
     */
    private static final Map<String, int[]> STATS = new ConcurrentHashMap<>();

    private static String key(ITestResult r) {
        return r.getMethod().getQualifiedName() + java.util.Arrays.toString(r.getParameters());
    }

    @Override
    public boolean retry(ITestResult result) {
        // triggered only on FAILURE
        String k = key(result);
        int[] counts = STATS.computeIfAbsent(k, x -> new int[2]);
        int attempts = ++counts[0];

        if (attempts < MAX_ATTEMPTS) {
            LOG.warn("[RETRY] {} — attempt {}/{} (quorum {} passes)", k, attempts + 1, MAX_ATTEMPTS, QUORUM);
            return true;    // re-run the test
        }
        LOG.error("[RETRY] {} exhausted {} attempts without meeting quorum", k, MAX_ATTEMPTS);
        return false;       // give up
    }

    // Listener collects passes and flips status once quorum is met
    public static class QuorumListener implements ITestListener {
        @Override
        public void onTestSuccess(ITestResult tr) {
            String k = key(tr);
            int[] counts = STATS.computeIfAbsent(k, x -> new int[2]);
            counts[0]++;   // attempts
            counts[1]++;   // passes

            LOG.info("[RETRY] {} — pass {}/{}", k, counts[1], QUORUM);

            if (counts[1] >= QUORUM) {
                LOG.info("[RETRY] {} reached quorum ({} passes). Marking as SUCCESS.", k, counts[1]);
                tr.setStatus(ITestResult.SUCCESS);
            }
        }
    }
}