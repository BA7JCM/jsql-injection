package com.jsql.model.injection.strategy.blind;

import com.jsql.model.InjectionModel;
import com.jsql.model.exception.StoppedByUserSlidingException;
import com.jsql.model.injection.strategy.blind.callable.CallableBlindBit;
import com.jsql.util.LogLevelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static name.fraser.neil.plaintext.diff_match_patch.Diff;
import static name.fraser.neil.plaintext.diff_match_patch.Operation;

/**
 * A blind attack class using concurrent threads.
 */
public class InjectionBlindBit extends AbstractInjectionMonobit<CallableBlindBit> {

    /**
     * Log4j logger sent to view.
     */
    private static final Logger LOGGER = LogManager.getRootLogger();

    private String sourceReferencePage;  // Source code of the TRUE web page (usually ?id=1)

    /**
     * List of string differences found in all the FALSE queries, compared
     * to the reference page. Each FALSE pages should contain
     * at least one same string, which shouldn't be present in all
     * the TRUE queries.
     */
    private List<Diff> falseDiffs = new ArrayList<>();
    private List<Diff> trueDiffs = new ArrayList<>();

    /**
     * Create blind attack initialization.
     * If every false diffs are not in true diffs and every true diffs are in
     * true diffs, then Blind attack is confirmed.
     */
    public InjectionBlindBit(InjectionModel injectionModel, BlindOperator blindOperator) {
        super(injectionModel, blindOperator);

        List<String> falsys = this.injectionModel.getMediatorVendor().getVendor().instance().getFalsyBit();
        if (falsys.isEmpty() || this.injectionModel.isStoppedByUser()) {
            return;
        }
        
        // Call the SQL request which must be TRUE (usually ?id=1)
        this.sourceReferencePage = this.callUrl(StringUtils.EMPTY, "bit#ref:"+ blindOperator.toString().toLowerCase());

        // Concurrent calls to the FALSE statements,
        // it will use inject() from the model
        ExecutorService taskExecutor = this.injectionModel.getMediatorUtils().getThreadUtil().getExecutor("CallableGetBlindBitTagFalse");
        Collection<CallableBlindBit> callablesFalsys = new ArrayList<>();
        for (String falsy: falsys) {
            callablesFalsys.add(new CallableBlindBit(
                falsy,
                injectionModel,
                this,
                blindOperator,
                "bit#falsy"
            ));
        }
        
        // Delete junk from the results of FALSE statements,
        // keep only diffs found in each and every FALSE pages.
        // Allow the user to stop the loop
        try {
            List<Future<CallableBlindBit>> futuresFalsys = taskExecutor.invokeAll(callablesFalsys);
            this.injectionModel.getMediatorUtils().getThreadUtil().shutdown(taskExecutor);
            for (Future<CallableBlindBit> futureFalsy: futuresFalsys) {
                if (this.injectionModel.isStoppedByUser()) {
                    return;
                }
                if (this.falseDiffs.isEmpty()) {
                    this.falseDiffs = futureFalsy.get().getDiffsWithReference();  // Init diffs
                } else {
                    this.falseDiffs.retainAll(futureFalsy.get().getDiffsWithReference());  // Clean un-matching diffs
                }
            }
        } catch (ExecutionException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        } catch (InterruptedException e) {
            LOGGER.log(LogLevelUtil.IGNORE, e, e);
            Thread.currentThread().interrupt();
        }

        if (this.injectionModel.isStoppedByUser()) {
            return;
        }
        
        this.cleanTrueDiffs(injectionModel, blindOperator);
    }

    private void cleanTrueDiffs(InjectionModel injectionModel, BlindOperator blindOperator) {
        ExecutorService taskExecutor = this.injectionModel.getMediatorUtils().getThreadUtil().getExecutor("CallableGetBlindBitTagTrue");
        Collection<CallableBlindBit> callablesTruthys = new ArrayList<>();
        List<String> truthys = this.injectionModel.getMediatorVendor().getVendor().instance().getTruthyBit();
        for (String truthy: truthys) {
            callablesTruthys.add(new CallableBlindBit(
                truthy,
                injectionModel,
                this,
                blindOperator,
                "bit#truthy"
            ));
        }

        // Remove TRUE diffs in the FALSE diffs as FALSE statement shouldn't contain any TRUE diff.
        try {
            List<Future<CallableBlindBit>> futuresTruthys = taskExecutor.invokeAll(callablesTruthys);
            this.injectionModel.getMediatorUtils().getThreadUtil().shutdown(taskExecutor);
            for (Future<CallableBlindBit> futureTruthy: futuresTruthys) {
                if (this.injectionModel.isStoppedByUser()) {
                    return;
                }
                if (this.trueDiffs.isEmpty()) {
                    this.trueDiffs = futureTruthy.get().getDiffsWithReference();  // Init diffs
                } else {
                    this.trueDiffs.retainAll(futureTruthy.get().getDiffsWithReference());  // Clean un-matching diffs
                }
                this.falseDiffs.removeAll(futureTruthy.get().getDiffsWithReference());
            }
        } catch (ExecutionException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        } catch (InterruptedException e) {
            LOGGER.log(LogLevelUtil.IGNORE, e, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public CallableBlindBit getCallableBitTest(String sqlQuery, int indexChar, int bit) {
        return new CallableBlindBit(
            sqlQuery,
            indexChar,
            bit,
            this.injectionModel,
            this,
            this.blindOperator,
            "bit#" + indexChar + "~" + bit
        );
    }

    @Override
    public boolean isInjectable() throws StoppedByUserSlidingException {
        if (this.injectionModel.isStoppedByUser()) {
            throw new StoppedByUserSlidingException();
        }
        var callable = new CallableBlindBit(
            this.injectionModel.getMediatorVendor().getVendor().instance().sqlBlindConfirm(),
            this.injectionModel,
            this,
            this.blindOperator,
            "bit#confirm"
        );
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        }
        return callable.isTrue()
            // when insertionChar = true then pages ref == truthy == falsy == confirm => falsy cleaned empty, truthy with opcode EQUAL not reliable
            && this.trueDiffs.stream().anyMatch(diff -> !Operation.EQUAL.equals(diff.operation))
            || this.falseDiffs.stream().anyMatch(diff -> !Operation.EQUAL.equals(diff.operation));
    }

    @Override
    public String getInfoMessage() {
        return "- Strategy Blind bit: query True when Diffs are matching " + this.falseDiffs + "\n\n";
    }
    
    
    // Getter and setter

    public String getSourceReferencePage() {
        return this.sourceReferencePage;
    }
    
    public List<Diff> getFalseDiffs() {
        return this.falseDiffs;
    }

    public List<Diff> getTrueDiffs() {
        return this.trueDiffs;
    }
}
