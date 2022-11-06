package flyway.wbidp;

import fi.nls.oskari.util.FlywayHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.util.List;

/**
 * Adds printout bundle to default and user views.
 */
public class V1_0_1__add_printout extends BaseJavaMigration {
    private static final String BUNDLE_ID = "printout";

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        final List<Long> views = FlywayHelper.getUserAndDefaultViewIds(connection);
        for (Long viewId : views) {
            if (FlywayHelper.viewContainsBundle(connection, BUNDLE_ID, viewId)) {
                continue;
            }
            FlywayHelper.addBundleWithDefaults(connection, viewId, BUNDLE_ID);
        }
    }
}