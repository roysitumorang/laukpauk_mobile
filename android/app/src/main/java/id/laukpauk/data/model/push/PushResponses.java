package id.laukpauk.data.model.push;

/** Responses of {@link id.laukpauk.data.remote.PushApi}. */
public final class PushResponses {
    private PushResponses() {}

    public static class Register {
        public boolean statusRegister;
    }

    public static class Unregister {
        public boolean statusUnregister;
    }
}
