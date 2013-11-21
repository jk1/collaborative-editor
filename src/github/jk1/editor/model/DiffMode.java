package github.jk1.editor.model;

/**
 * @author Evgeny Naumenko
 */
public enum DiffMode {

    DELTA {
        @Override
        void applyToView(View view, MobWriteMessage message, Diff diff) {
            view.processDelta(message, diff);
        }
    },
    RAW {
        @Override
        void applyToView(View view, MobWriteMessage message, Diff diff) {
            view.processRaw(message, diff);
        }
    };

    abstract void applyToView(View view, MobWriteMessage message, Diff diff);
}
