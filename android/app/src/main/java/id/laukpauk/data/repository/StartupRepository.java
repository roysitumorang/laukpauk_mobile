package id.laukpauk.data.repository;

/** What the splash screen needs from the stored flags: first run, pending OTP confirmation. */
public class StartupRepository extends BaseRepository {

    public static final int GO_INTRO = 0;
    public static final int GO_HOME = 1;
    public static final int GO_OTP_REGISTRASI = 2;

    public StartupRepository(RepositoryDeps deps) {
        super(deps);
    }

    /** Legacy SplashScreen routing: intro on the first run, the OTP screen while registration is unconfirmed. */
    public int decideDestination() {
        boolean hasAppStarted = prefs.getBool("isAppStarted", false);
        if (!hasAppStarted) {
            return GO_INTRO;
        }
        boolean otpHasSent = prefs.getBool("otpHasSent", false);
        boolean otpHasConfirmed = prefs.getBool("otpHasConfirmed", false);
        boolean passed = session.isOTPRegistrasiPassed();
        if (otpHasSent && (!otpHasConfirmed || passed)) {
            return passed ? GO_HOME : GO_OTP_REGISTRASI;
        }
        return GO_HOME;
    }

    /** The intro's "Belanja Sekarang". */
    public void markAppStarted() {
        prefs.setBool("isAppStarted", true);
    }
}
