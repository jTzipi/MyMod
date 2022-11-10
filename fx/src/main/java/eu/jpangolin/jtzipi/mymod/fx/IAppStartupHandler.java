package eu.jpangolin.jtzipi.mymod.fx;

/**
 * Handler for startup task.
 * <p>
 *     This can be used if you have a <i>long</i> startup task and
 *     want to display some splash screen or likewise.
 *
 *     Look <a href="https://gist.github.com/jewelsea/2305098#file-taskbasedsplash-java-L27">this</a>.
 *
 *
 * </p>
 * @author Jewelsea
 * @author jTzipi
 */
public interface IAppStartupHandler {

        /**
         * On task running.
         */
        void onRunning();

        /**
         * On task progress.
         */
        void onProgress();

        /**
         * On task completion.
         */
        void onCompletion();

        /**
         * On task
         */
        void onError();



}